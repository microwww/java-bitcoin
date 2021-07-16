package com.github.microwww.bitcoin.net;

import com.github.microwww.bitcoin.conf.BlockInfo;
import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.net.protocol.AbstractProtocol;
import com.github.microwww.bitcoin.net.protocol.Version;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Date;

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
                        ch.pipeline().addLast(new ChannelOutboundHandlerAdapter() {
                        }).addLast(new PrintInputChannel());
                    }
                });
        // connection
        Peer peer = new Peer(settings, "192.168.1.246", 18444);
        bootstrap.connect(peer.getHost(), peer.getPort())
                .addListener((DefaultChannelPromise e) -> {
                    BlockInfo.getInstance().addPeers((InetSocketAddress) e.channel().localAddress(), peer);
                    logger.info("Connection FROM: " + e.channel().localAddress() + ", TO: " + e.channel().remoteAddress());
                })
                .sync().channel().closeFuture()
                .sync()
                .addListener(e -> {
                    executors.shutdownGracefully();
                });
    }

    public static class PrintInputChannel extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ByteBuf payload = Unpooled.buffer();
            // buffer.skipBytes(MessageHeader.HEADER_SIZE);
            Date date = new Date(40L * 365 * 24 * 60 * 60 * 1000); // 1970 + 40年
            new Version(settings, date).setNonce(1234567890123456789L).write(payload);
            int i = payload.readableBytes();
            byte[] byts = new byte[i];
            payload.readBytes(byts);
            ByteBuf bf = Unpooled.buffer();
            new MessageHeader(settings.getMagic(), NetProtocol.VERSION).setPayload(byts).writer(bf);
            bf.writeBytes(payload);
            ctx.writeAndFlush(bf);
            Thread.sleep(5000);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Peer peer = BlockInfo.getInstance().getPeer(ctx);
            ByteBuf buf = (ByteBuf) msg;
            while (buf.readableBytes() > 0) { // TODO :: 这里有半包的问题
                MessageHeader header = MessageHeader.read(buf);
                try {
                    NetProtocol netProtocol = header.getNetProtocol();
                    logger.debug("Get a command : {}", netProtocol.cmd());
                    AbstractProtocol parse = netProtocol.parse(peer, header.getPayload());
                    logger.info("Parse data to : {}", parse.getClass().getSimpleName());
                    ctx.executor().execute(() -> {
                        parse.service(ctx);
                    });
                } catch (UnsupportedOperationException ex) {
                    logger.warn("UnsupportedOperationException : {}", header.getCommand());
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
        }
    }

}