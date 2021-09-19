package com.github.microwww.bitcoin.provider;

import cn.hutool.cache.impl.FIFOCache;
import com.github.microwww.bitcoin.event.AddBlockEvent;
import com.github.microwww.bitcoin.event.AddTxEvent;
import com.github.microwww.bitcoin.event.InvEvent;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.net.PeerChannelServerProtocol;
import com.github.microwww.bitcoin.net.protocol.Block;
import com.github.microwww.bitcoin.net.protocol.GetData;
import com.github.microwww.bitcoin.net.protocol.Inv;
import com.github.microwww.bitcoin.net.protocol.Tx;
import com.github.microwww.bitcoin.util.TimeQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InvMessageListener {
    protected final FIFOCache<Uint256, GetData.Message> fifoCache = new FIFOCache(1_000, 5000);
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
                InvMessageListener.this.loading(request.getTransaction().hash());
            }
        };
    }

    @Bean
    public ApplicationListener<AddBlockEvent> addBlockEventListener() {
        return new ApplicationListener<AddBlockEvent>() {

            @Override
            public void onApplicationEvent(AddBlockEvent event) {
                Block request = event.getBitcoinSource();
                InvMessageListener.this.loading(request.getChainBlock().hash());
            }
        };
    }

    public void loading(Uint256 hash) {
        GetData.Message msg = fifoCache.get(hash);
        if (msg != null) {
            fifoCache.remove(hash);
            timeQueue.add(msg);
        }
    }
}
