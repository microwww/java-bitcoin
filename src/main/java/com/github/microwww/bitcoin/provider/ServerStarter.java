package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.net.BitcoinNetDecode;
import com.github.microwww.bitcoin.net.BitcoinNetEncode;
import com.github.microwww.bitcoin.net.PeerChannelServerProtocol;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
public class ServerStarter implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(ServerStarter.class);
    private static final EventLoopGroup executors = new NioEventLoopGroup();

    public static final String DEFAULT_HOST = "0.0.0.0";
    public static final int IDLE_SECONDS = 60; // 分钟的心跳
    public static final int TIME_OUT_SECONDS = 5;

    @Autowired
    CChainParams chainParams;
    @Autowired
    PeerChannelServerProtocol peerChannelServerProtocol;
    @Autowired
    LocalBlockChain localBlockChain;

    private Channel server;

    public void newThreadSTART(Consumer<DefaultChannelPromise> bindingListener, Consumer<DefaultChannelPromise> stopListener) {
        executors.next().execute(() -> {
            start(bindingListener, stopListener);
        });
    }

    public void start() {
        start(null, null);
    }

    public void start(Consumer<DefaultChannelPromise> bindingListener, Consumer<DefaultChannelPromise> stopListener) {
        String host = DEFAULT_HOST;
        int port = chainParams.settings.getPort();
        if(port <= 0){
            port = chainParams.getEnvParams().getDefaultPort();
        }
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(executors)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        PeerChannelServerHandler handler = new PeerChannelServerHandler(peerChannelServerProtocol);
                        InetSocketAddress addr = ch.remoteAddress();
                        ch.attr(Peer.PEER).set(new Peer(localBlockChain, addr.getAddress().getHostAddress(), addr.getPort()));
                        ch.pipeline()
                                .addLast(new IdleStateHandler(0, 0, IDLE_SECONDS))
                                .addLast(new BitcoinNetEncode(chainParams))
                                .addLast(new BitcoinNetDecode(chainParams))
                                .addLast(handler);
                    }
                });
        // connection
        try {
            String hp = host + ":" + port;
            ChannelFuture sync = bootstrap.bind(host, port)
                    .addListener((DefaultChannelPromise e) -> {
                        if (e.isSuccess()) {
                            logger.info("Server listener : {}", e.channel().localAddress());
                        } else {
                            logger.error("Server Bind ERROR : {}", hp);
                            executors.shutdownGracefully();
                        }
                        // client can run
                        if (bindingListener != null) bindingListener.accept(e);
                    });
            this.server = sync.channel();
            server.closeFuture().addListener((DefaultChannelPromise e) -> {
                if (stopListener != null) stopListener.accept(e);
                logger.warn("STOP server {}, close server-channel EventLoopGroup : {}", hp, e.getClass());
            }).await(TIME_OUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.interrupted();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        logger.info("Close server , and shutdown EventLoopGroup");
        if (server != null) {
            server.close();
        }
        executors.shutdownGracefully();
    }
}
