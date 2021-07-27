package com.github.microwww.bitcoin.net;

import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.net.protocol.AbstractProtocol;
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

@Component
public class PeerChannelInboundHandler extends SimpleChannelInboundHandler<MessageHeader> {
    private static final Logger logger = LoggerFactory.getLogger(PeerChannelInboundHandler.class);

    @Autowired
    CChainParams config;
    @Autowired
    PeerChannelProtocol peerChannelProtocol;
    @Autowired
    PeerConnection connection;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.write(Version.builder(connection.getPeer(ctx), config));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageHeader header) throws Exception {
        try {
            NetProtocol netProtocol = header.getNetProtocol();
            logger.debug("Get a command : {}, length : {}", netProtocol.cmd(), header.getPayload().length);
            AbstractProtocol parse = netProtocol.parse(connection.getPeer(ctx), header.getPayload());
            logger.info("Parse command: {},  data : {}", netProtocol.cmd(), parse.getClass().getSimpleName());

            peerChannelProtocol.doAction(ctx, parse);

        } catch (UnsupportedOperationException ex) {
            logger.warn("UnsupportedOperationException service: {}", header.getCommand());
        } catch (UnsupportedNetProtocolException ex) {
            logger.warn("UnsupportedNetProtocolException : {}", header.getCommand());
        } catch (RuntimeException ex) {
            logger.error("Request data error {}: \n{}\n", ex.getMessage(), ByteUtil.hex(header.getPayload()));
            throw ex;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Connection Error !", cause);
    }

}