package com.github.microwww.bitcoin.net;

import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.net.protocol.*;
import com.github.microwww.bitcoin.provider.Peer;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * `net_processing.cpp`
 */
@Component
public class PeerChannelServerProtocol extends PeerChannelProtocol {
    private static final Logger logger = LoggerFactory.getLogger(PeerChannelProtocol.class);

    @Autowired
    CChainParams chainParams;

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
    }

    public void service(ChannelHandlerContext ctx, GetAddr request) {
    }

    @Override
    public void service(ChannelHandlerContext ctx, GetHeaders request) {
        super.service(ctx, request);
    }
}
