package com.github.microwww.bitcoin.net;

import com.github.microwww.bitcoin.net.protocol.AbstractProtocol;
import com.github.microwww.bitcoin.net.protocol.Reject;
import com.github.microwww.bitcoin.net.protocol.UnsupportedNetProtocolException;
import com.github.microwww.bitcoin.net.protocol.Version;
import com.github.microwww.bitcoin.provider.PeerChannelProtocol;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class PeerChannelInboundHandler extends SimpleChannelInboundHandler<MessageHeader> {
    private static final Logger logger = LoggerFactory.getLogger(PeerChannelInboundHandler.class);

    @Autowired
    PeerChannelProtocol peerChannelProtocol;

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
                logger.debug("Get a command : {} \n{}", netProtocol.cmd(), ByteUtil.hex(header.getPayload()));
            }
            AbstractProtocol parse = netProtocol.parse(peer, header.getPayload());
            logger.info("Parse command: {},  data : {}", netProtocol.cmd(), parse.getClass().getSimpleName());

            peerChannelProtocol.doAction(ctx, parse);

        } catch (UnsupportedOperationException ex) {
            logger.warn("UnsupportedOperation class [{}].service: {}", peerChannelProtocol.getClass().getName(), header.getCommand());
            ctx.writeAndFlush(reject(peer, header, ex));
        } catch (UnsupportedNetProtocolException ex) {
            logger.warn("Net-protocol class [{}] unsupported  {}", NetProtocol.class.getName(), header.getCommand());
            ctx.writeAndFlush(reject(peer, header, ex));
        } catch (RuntimeException ex) {
            logger.error("Request data error {}: \n{}\n", ex.getMessage(), ByteUtil.hex(header.getPayload()));
            throw ex;
        }
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
        return reject;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Connection Error !", cause);
    }

}