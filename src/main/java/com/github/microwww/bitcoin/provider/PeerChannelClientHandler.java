package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.net.MessageHeader;
import com.github.microwww.bitcoin.net.NetProtocol;
import com.github.microwww.bitcoin.net.PeerChannelClientProtocol;
import com.github.microwww.bitcoin.net.protocol.*;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.io.Closeable;
import java.io.IOException;

public class PeerChannelClientHandler extends SimpleChannelInboundHandler<MessageHeader> implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(PeerChannelClientHandler.class);

    @Autowired
    PeerChannelClientProtocol peerChannelClientProtocol;

    public PeerChannelClientHandler(PeerChannelClientProtocol peerChannelClientProtocol) {
        this.peerChannelClientProtocol = peerChannelClientProtocol;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Peer attr = ctx.channel().attr(Peer.PEER).get();
        Assert.isTrue(null != attr, "Not init peer in channel");
        ctx.write(Version.builder(attr, attr.getLocalBlockChain().getChainParams()));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageHeader header) {
        Peer peer = ctx.channel().attr(Peer.PEER).get();
        try {
            NetProtocol netProtocol = header.getNetProtocol();
            if (logger.isDebugEnabled()) {
                logger.debug("Get a command : {} \n 0x{}", netProtocol.cmd(), ByteUtil.hex(header.getPayload()));
            }
            AbstractProtocol parse = netProtocol.parse(peer, header.getPayload());
            if (parse instanceof AbstractProtocolAdapter) {
                ((AbstractProtocolAdapter<?>) parse).setPayload(header.getPayload());
            }
            logger.debug("Command: {}, parse to : {}", netProtocol.cmd(), parse.getClass().getSimpleName());

            peerChannelClientProtocol.doAction(ctx, parse);

        } catch (UnsupportedOperationException ex) {
            logger.warn("UnsupportedOperation class [{}].service: {} \n {}", peerChannelClientProtocol.getClass().getName(),
                    header.getCommand(), ByteUtil.hex(header.getPayload()));
            ctx.writeAndFlush(reject(peer, header, ex));
        } catch (UnsupportedNetProtocolException ex) {
            logger.warn("Net-protocol class [{}] unsupported PROTOCOL: {} \n {}", NetProtocol.class.getName(), header.getCommand(),
                    ByteUtil.hex(header.getPayload()));
            ctx.writeAndFlush(reject(peer, header, ex));
        } catch (RuntimeException ex) {
            logger.error("Server internal error {}: \n{}\n", ex.getMessage(), ByteUtil.hex(header.getPayload()), ex);
            throw ex;
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        Peer peer = ctx.channel().attr(Peer.PEER).get();
        if (peer != null) {
            peerChannelClientProtocol.channelClose(peer);
        }
        super.channelUnregistered(ctx);
    }

    public Reject reject(Peer peer, MessageHeader header, Exception ex) {
        Reject reject = new Reject(peer);
        reject.setMessage(header.getCommand());
        reject.setCode(Reject.Code.REJECT_INVALID.code);
        String er = ex.getMessage();
        if (er == null) {
            er = "error parsing message";
        }
        reject.setMessage(er);
        reject.setReason(ex.getClass().getName());
        return reject;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Attribute<Peer> attr = ctx.channel().attr(Peer.PEER);
        Peer peer = attr.get();
        logger.warn("Parse Request OR execute ERROR, peer {}", peer.getURI(), cause);
    }

    @Override
    public void close() throws IOException {
        peerChannelClientProtocol.close();
    }
}