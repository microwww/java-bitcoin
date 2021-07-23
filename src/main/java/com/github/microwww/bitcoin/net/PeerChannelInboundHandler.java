package com.github.microwww.bitcoin.net;

import com.github.microwww.bitcoin.chain.BlockChainContext;
import com.github.microwww.bitcoin.conf.Config;
import com.github.microwww.bitcoin.net.protocol.AbstractProtocol;
import com.github.microwww.bitcoin.net.protocol.UnsupportedNetProtocolException;
import com.github.microwww.bitcoin.net.protocol.Version;
import com.github.microwww.bitcoin.provider.PeerChannelProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PeerChannelInboundHandler extends SimpleChannelInboundHandler<MessageHeader> {
    private static final Logger logger = LoggerFactory.getLogger(PeerChannelInboundHandler.class);

    @Autowired
    Config config;
    @Autowired
    PeerChannelProtocol peerChannelProtocol;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.write(Version.builder(BlockChainContext.getPeer(ctx), config.getBitcoin()));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageHeader header) throws Exception {
        logger.info("Parse data to : {}", header.getClass().getSimpleName());
        try {
            NetProtocol netProtocol = header.getNetProtocol();
            logger.debug("Get a command : {}", netProtocol.cmd());
            AbstractProtocol parse = netProtocol.parse(BlockChainContext.getPeer(ctx), header.getPayload());
            logger.info("Parse data to : {}", parse.getClass().getSimpleName());

            peerChannelProtocol.doAction(ctx, parse);

        } catch (UnsupportedOperationException ex) {
            logger.warn("UnsupportedOperationException service: {}", header.getCommand());
        } catch (UnsupportedNetProtocolException ex) {
            logger.warn("UnsupportedNetProtocolException : {}", header.getCommand());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Connection Error !", cause);
    }

}