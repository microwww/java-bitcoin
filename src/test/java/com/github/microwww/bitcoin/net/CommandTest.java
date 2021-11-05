package com.github.microwww.bitcoin.net;

import com.github.microwww.bitcoin.AbstractEnv;
import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.net.protocol.AbstractProtocol;
import com.github.microwww.bitcoin.net.protocol.Version;
import com.github.microwww.bitcoin.provider.Peer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandTest extends AbstractEnv {
    private static final Logger logger = LoggerFactory.getLogger(CommandTest.class);

    public CommandTest() {
        super(CChainParams.Env.REG_TEST);
    }

    @Test
    @Disabled
    public void sendCommand() throws Exception {
        Peer peer = new Peer(localBlockChain, "192.168.1.246", 18444);
        EventLoopGroup executors = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap()
                .group(executors)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ch.attr(Peer.PEER).set(peer);
                        ch.pipeline()
                                .addLast(new BitcoinNetEncode(localBlockChain.getChainParams()))
                                .addLast(new BitcoinNetDecode(localBlockChain.getChainParams()))
                                .addLast(new PrintInputChannel());
                    }
                });
        // connection
        bootstrap.connect(peer.getHost(), peer.getPort())
                .addListener((DefaultChannelPromise e) -> {
                    logger.info("Connection FROM: " + e.channel().localAddress() + ", TO: " + e.channel().remoteAddress());
                })
                .sync().channel().closeFuture()
                .sync();
        System.in.read();
        executors.shutdownGracefully();
    }

    public class PrintInputChannel extends SimpleChannelInboundHandler<MessageHeader> {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            Peer peer = ctx.channel().attr(Peer.PEER).get();
            ctx.writeAndFlush(Version.builder(peer, peer.getLocalBlockChain().getChainParams()));
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, MessageHeader header) throws Exception {
            Peer peer = ctx.channel().attr(Peer.PEER).get();
            logger.info("Parse data to : {}", header.getClass().getSimpleName());
            try {
                NetProtocol netProtocol = header.getNetProtocol();
                logger.debug("Get a command : {}", netProtocol.command());
                AbstractProtocol parse = netProtocol.parse(peer, header.getPayload());
                logger.info("Parse data to : {}", parse.getClass().getSimpleName());
                ctx.executor().execute(() -> {
                    new PeerChannelClientProtocol(null, null).doAction(ctx, parse);
                });
            } catch (UnsupportedOperationException ex) {
                logger.warn("UnsupportedOperationException : {}", header.getCommand());
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
        }
    }

}