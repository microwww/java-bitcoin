package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.net.PeerChannelProtocol;
import com.github.microwww.bitcoin.net.protocol.Ping;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class PeerChannelServerHandler extends PeerChannelClientHandler {
    private static final Logger logger = LoggerFactory.getLogger(PeerChannelServerHandler.class);

    public PeerChannelServerHandler(PeerChannelProtocol peerChannelProtocol) {
        super(peerChannelProtocol);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Peer attr = ctx.channel().attr(Peer.PEER).get();
        Assert.isTrue(null != attr, "Not init peer in channel");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            Peer peer = ctx.channel().attr(Peer.PEER).get();
            if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                logger.debug("Send Heartbeat [ping] to client :{}", peer.getURI());
                ctx.writeAndFlush(new Ping(peer)).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        }
        super.userEventTriggered(ctx, evt);
    }
}