package com.github.microwww.bitcoin.net;

import com.github.microwww.bitcoin.chain.BlockChainContext;
import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.net.protocol.AbstractProtocol;
import com.github.microwww.bitcoin.net.protocol.Version;
import com.github.microwww.bitcoin.provider.PeerChannelProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class CommandTest {
    private static final Logger logger = LoggerFactory.getLogger(CommandTest.class);

    private static Settings settings = new Settings();

    @Test
    @Disabled
    public void sendCommand() throws InterruptedException {
        EventLoopGroup executors = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap()
                .group(executors)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ch.pipeline()
                                .addLast(new BitcoinNetEncode())
                                .addLast(new BitcoinNetDecode(settings))
                                .addLast(new PrintInputChannel());
                    }
                });
        // connection
        Peer peer = new Peer("192.168.2.18", 18444);
        bootstrap.connect(peer.getHost(), peer.getPort())
                .addListener((DefaultChannelPromise e) -> {
                    BlockChainContext.get().addPeers((InetSocketAddress) e.channel().localAddress(), peer);
                    logger.info("Connection FROM: " + e.channel().localAddress() + ", TO: " + e.channel().remoteAddress());
                })
                .sync().channel().closeFuture()
                .sync()
                .addListener(e -> {
                    executors.shutdownGracefully();
                });
    }

    public static class PrintInputChannel extends SimpleChannelInboundHandler<MessageHeader> {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.write(Version.builder(BlockChainContext.getPeer(ctx), settings));
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, MessageHeader header) throws Exception {
            Peer peer = BlockChainContext.get().getPeer(ctx);
            logger.info("Parse data to : {}", header.getClass().getSimpleName());
            try {
                NetProtocol netProtocol = header.getNetProtocol();
                logger.debug("Get a command : {}", netProtocol.cmd());
                AbstractProtocol parse = netProtocol.parse(peer, header.getPayload());
                logger.info("Parse data to : {}", parse.getClass().getSimpleName());
                ctx.executor().execute(() -> {
                    new PeerChannelProtocol().doAction(ctx, parse);
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