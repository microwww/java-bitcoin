package com.github.microwww.bitcoin.net;

import cn.hutool.cache.impl.FIFOCache;
import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.net.protocol.*;
import com.github.microwww.bitcoin.provider.Peer;
import com.github.microwww.bitcoin.util.TimeQueue;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * `net_processing.cpp`
 */
@Component
public class PeerChannelServerProtocol extends PeerChannelProtocol {
    private static final Logger logger = LoggerFactory.getLogger(PeerChannelProtocol.class);

    @Autowired
    CChainParams chainParams;
    private final FIFOCache<Uint256, GetData.Message> fifoCache = new FIFOCache(1_000, 5000);
    private final TimeQueue<GetData.Message> timeQueue;
    private final ReentrantLock lock = new ReentrantLock();
    private final Map<Peer, ChannelHandlerContext> channels = new ConcurrentHashMap<>();

    public PeerChannelServerProtocol() {
        this.timeQueue = new TimeQueue<>(this::consumer, 100, 5_000);
    }

    private void consumer(Queue<GetData.Message> queue) {
        if (lock.tryLock()) {
            try {
                List<GetData.Message> list = new ArrayList<>();
                for (int i = 0; i < 100 && !queue.isEmpty(); i++) {
                    GetData.Message qr = queue.poll();
                    list.add(qr);
                }
                channels.values().forEach(e -> {
                    Peer peer = e.channel().attr(Peer.PEER).get();
                    Inv data = new Inv(peer).setData(list.toArray(new GetData.Message[]{}));
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

    @Override
    public void service(ChannelHandlerContext ctx, GetHeaders request) {
        super.service(ctx, request);
    }

    @Override
    public void service(ChannelHandlerContext ctx, Block request) {
        super.service(ctx, request);
        this.loading(request.getChainBlock().hash());
    }

    public void loading(Uint256 hash) {
        GetData.Message msg = fifoCache.get(hash);
        if (msg != null) {
            fifoCache.remove(hash);
            timeQueue.add(msg);
        }
    }

    @Override
    public void service(ChannelHandlerContext ctx, Tx request) {
        super.service(ctx, request);
        this.loading(request.getTransaction().hash());
    }

    @Override
    public boolean service(ChannelHandlerContext ctx, Inv request) {
        boolean res = super.service(ctx, request);
        if (res) {
            GetData.Message[] data = request.getData();
            for (GetData.Message ms : data) {
                fifoCache.put(ms.getHashIn(), ms);
            }
        }
        return res;
    }

    @Override
    public void channelClose(Peer peer) {
        super.channelClose(peer);
        channels.remove(peer);
    }
}
