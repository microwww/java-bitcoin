package com.github.microwww.bitcoin.provider;

import cn.hutool.cache.impl.FIFOCache;
import com.github.microwww.bitcoin.chain.BlockHeader;
import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.event.AddBlockEvent;
import com.github.microwww.bitcoin.event.AddTxEvent;
import com.github.microwww.bitcoin.event.HeadersEvent;
import com.github.microwww.bitcoin.event.InvEvent;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.net.PeerChannelServerProtocol;
import com.github.microwww.bitcoin.net.protocol.*;
import com.github.microwww.bitcoin.util.TimeQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InvMessageListener {
    private static final Logger logger = LoggerFactory.getLogger(InvMessageListener.class);

    protected final FIFOCache<Uint256, GetData.Message> fifoCache = new FIFOCache(1_000, 60_000);
    @Autowired
    PeerChannelServerProtocol peerChannelServerProtocol;
    TimeQueue timeQueue; // = new TimeQueue<>(this::consumer, 100, 5_000);

    public InvMessageListener(PeerChannelServerProtocol peerChannelServerProtocol) {
        this.peerChannelServerProtocol = peerChannelServerProtocol;
        timeQueue = new TimeQueue<>(peerChannelServerProtocol::publishInv, 100, 5_000);
    }

    @Bean
    public ApplicationListener<InvEvent> invEventListener() {
        return new ApplicationListener<InvEvent>() {

            @Override
            public void onApplicationEvent(InvEvent event) {
                Inv inv = event.getBitcoinSource();
                GetData.Message[] data = inv.getData();
                for (GetData.Message ms : data) {
                    fifoCache.put(ms.getHashIn(), ms);
                }
            }
        };
    }

    @Bean
    public ApplicationListener<AddTxEvent> addTxEventListener() {
        return new ApplicationListener<AddTxEvent>() {

            @Override
            public void onApplicationEvent(AddTxEvent event) {
                Tx request = event.getBitcoinSource();
                RawTransaction tr = request.getTransaction();
                if (tr.isWitness()) {
                    InvMessageListener.this.loading(tr.whash());
                } else {
                    InvMessageListener.this.loading(tr.hash());
                }
            }
        };
    }

    @Bean
    public ApplicationListener<HeadersEvent> headersEventListener() {
        return new ApplicationListener<HeadersEvent>() {

            @Override
            public void onApplicationEvent(HeadersEvent event) {
                Headers request = event.getBitcoinSource();
                BlockHeader[] bks = request.getChainBlocks();
                if (bks.length <= 2) { // TODO: simple, time
                    for (BlockHeader bk : bks) {
                        //Date tm = bk.header.getDateTime();
                        Uint256 hash = bk.hash();
                        fifoCache.put(hash, new GetData.Message(GetData.Type.MSG_WITNESS_BLOCK, hash));
                    }
                }
            }
        };
    }

    @Bean
    public ApplicationListener<AddBlockEvent> addBlockEventListener() {
        return new ApplicationListener<AddBlockEvent>() {

            @Override
            public void onApplicationEvent(AddBlockEvent event) {
                Block request = event.getBitcoinSource();
                if (request.getChainBlock().header.isInTwoHours()) {
                    InvMessageListener.this.loading(request.getChainBlock().hash());
                }
            }
        };
    }

    public void loading(Uint256 hash) {
        GetData.Message msg = fifoCache.get(hash);
        if (msg != null) {
            logger.debug("Add new HASH: {}", msg.getHashIn());
            fifoCache.remove(hash);
            timeQueue.add(msg);
        }
    }
}
