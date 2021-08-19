package com.github.microwww.bitcoin.net.server;

import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.net.BitcoinNetDecode;
import com.github.microwww.bitcoin.net.BitcoinNetEncode;
import com.github.microwww.bitcoin.net.PeerChannelInboundHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Consumer;

@Component
public class ServerStarter implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(ServerStarter.class);
    public static final String DEFAULT_HOST = "0.0.0.0";
    private static final EventLoopGroup executors = new NioEventLoopGroup();

    @Autowired
    CChainParams chainParams;
    @Autowired
    PeerChannelInboundHandler handler;

    private Channel server;

    public Channel newThreadSTART(Consumer<DefaultChannelPromise> bindingListener, Consumer<DefaultChannelPromise> stopListener) {
        executors.next().execute(() -> {
            start(bindingListener, stopListener);
        });
        return server;
    }

    public void start() {
        start(null, null);
    }

    public void start(Consumer<DefaultChannelPromise> bindingListener, Consumer<DefaultChannelPromise> stopListener) {
        String host = DEFAULT_HOST;
        int port = chainParams.getEnvParams().getDefaultPort();
        String bind = chainParams.settings.getBind();
        if (bind != null && bind.trim().length() > 0) {
            String[] sp = bind.trim().split(":");
            host = sp[0];
            if (sp.length > 1) {
                port = Integer.valueOf(sp[1]);
            }
        }
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(executors)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ch.pipeline()
                                .addLast(new BitcoinNetEncode(chainParams))
                                .addLast(new BitcoinNetDecode(chainParams))
                                .addLast(handler);// TODO:: 这是需要修改重点
                    }
                });
        // connection
        try {
            String hp = host + ":" + port;
            ChannelFuture sync = bootstrap.bind(host, port)
                    .addListener((DefaultChannelPromise e) -> {
                        if (e.isSuccess()) {
                            logger.info("Server Bind In: {}", e.channel().localAddress());
                        } else {
                            logger.error("Server Bind ERROR : {}", hp);
                            executors.shutdownGracefully();
                        }
                        // client can run
                        if (bindingListener != null) bindingListener.accept(e);
                    }).sync();
            this.server = sync.channel();
            server.closeFuture().addListener((DefaultChannelPromise e) -> {
                if (stopListener != null) stopListener.accept(e);
                logger.debug("STOP server, close server-channel EventLoopGroup : {}", e.getClass());
            }).sync();
        } catch (InterruptedException e) {
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
