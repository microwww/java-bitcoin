package com.github.microwww.bitcoin.net;

import com.github.microwww.bitcoin.chain.BlockHeader;
import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.net.protocol.*;
import com.github.microwww.bitcoin.provider.Peer;
import com.github.microwww.bitcoin.store.HeightBlock;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class PeerChannelServerProtocol extends PeerChannelClientProtocol {
    private static final Logger logger = LoggerFactory.getLogger(PeerChannelServerProtocol.class);

    @Autowired
    CChainParams chainParams;
    private final ReentrantLock lock = new ReentrantLock();
    private final Map<Peer, ChannelHandlerContext> channels = new ConcurrentHashMap<>();

    public void publishInv(Queue<GetData.Message> queue) {
        if (queue.isEmpty()) {
            return;
        }
        if (lock.tryLock()) {
            logger.debug("Send Inv Message: {}", queue.size());
            try {
                List<GetData.Message> list = new ArrayList<>();
                for (int i = 0; i < 100 && !queue.isEmpty(); i++) {
                    GetData.Message qr = queue.poll();
                    list.add(qr);
                }
                channels.values().forEach(e -> {
                    Peer peer = e.channel().attr(Peer.PEER).get();
                    Inv data = new Inv(peer).setData(list.toArray(new GetData.Message[]{}));
                    logger.debug("Inv send to peer {}, Length: {}", peer.getURI(), data.getData().length);
                    e.channel().writeAndFlush(data);
                });
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public void service(ChannelHandlerContext ctx, Version version) {
        Peer peer = ctx.channel().attr(Peer.PEER).get();
        ctx.executor().execute(() -> {
            Version ver = Version.builder(peer, chainParams);
            ctx.write(ver);
            ctx.write(new WtxidRelay(peer));
            ctx.write(new SendAddrV2(peer));
            ctx.writeAndFlush(new VerACK(peer));
        });
    }

    @Override
    public void service(ChannelHandlerContext ctx, VerACK ack) {
        Peer peer = ctx.channel().attr(Peer.PEER).get();
        peer.setRemoteReady(true);
        ctx.executor().execute(() -> {
            ctx.write(new SendHeaders(peer));
            ctx.write(new SendCmpct(peer).setVal(new Uint32(2)));
            ctx.write(new SendCmpct(peer));
            ctx.write(new Ping(peer));
            // getheaders
            this.sendGetHeaderNow(ctx);
            ctx.writeAndFlush(new FeeFilter(peer));
        });
        channels.put(peer, ctx);
    }

    public void service(ChannelHandlerContext ctx, GetAddr request) {
    }

    public void service(ChannelHandlerContext ctx, GetData request) {
        for (GetData.Message msg : request.getMessages()) {
            Optional<GetDataType> select = msg.select();
            if (!select.isPresent()) {
                logger.warn("Not support type: {}", msg.getTypeIn());
                continue;
            }
            select.ifPresent(e -> {
                if (e.name().contains("BLOCK")) {
                    Uint256 hash = msg.getHashIn();
                    chain.getDiskBlock().getChinBlock(hash).ifPresent(k -> {
                        Block block = new Block(request.getPeer());
                        block.setChainBlock(k.getBlock());
                        ctx.writeAndFlush(block);
                    });
                } else if (e.name().contains("TX")) {
                    Uint256 hash = msg.getHashIn();
                    chain.getTransactionStore().findTransaction(hash).ifPresent(k -> {
                        Tx tx = new Tx(request.getPeer());
                        tx.setTransaction(k.getTransaction());
                        ctx.writeAndFlush(tx);
                    });
                } else {
                    logger.warn("Not support type: {}", e.name());
                }
            });
        }
    }

    @Override
    public void service(ChannelHandlerContext ctx, GetHeaders request) {
        List<Uint256> list = request.getStarting();
        if (list.size() > GetHeaders.MAX_UN_CONNECTING_HEADERS) {
            return;
        }
        int from = -1;
        for (Uint256 uint256 : list) {
            from = chain.getDiskBlock().getHeight(uint256);
            if (from > 0) {
                break;
            }
        }
        from++;
        Uint256 stopping = request.getStopping();
        if (from >= 0) {
            List<BlockHeader> blocks = new ArrayList<>();
            for (int i = 0; i < GetHeaders.MAX_HEADERS_RESULTS; i++) {
                Optional<Uint256> hash = chain.getDiskBlock().getHash(from + i);
                if (hash.isPresent()) {
                    Optional<HeightBlock> cb = chain.getDiskBlock().readBlock(hash.get());
                    Assert.isTrue(cb.isPresent(), "This hash in height , but not in local file");
                    ChainBlock fd = cb.get().getBlock();
                    blocks.add(fd.header);
                    if (fd.hash().equals(stopping)) {
                        break;
                    }
                } else break;
            }
            if (!blocks.isEmpty()) {
                Headers headers = new Headers(request.getPeer());
                headers.setChainBlocks(blocks.toArray(new BlockHeader[]{}));
                ctx.writeAndFlush(headers);
            } else {
                logger.debug("Not block find, GetHeaders empty");
            }
        }
    }

    @Override
    public void channelClose(Peer peer) {
        super.channelClose(peer);
        channels.remove(peer);
    }
}
