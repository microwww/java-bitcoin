package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.net.BitcoinNetDecode;
import com.github.microwww.bitcoin.net.BitcoinNetEncode;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

@Component
public class PeerConnection implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(PeerConnection.class);
    private static final int TIME_OUT_SECONDS = 5;
    private static EventLoopGroup executors = new NioEventLoopGroup();

    @Autowired
    LocalBlockChain localBlockChain;
    @Autowired
    PeerChannelInboundHandler peerChannelInboundHandlerEventPublisher;
    @Autowired
    CChainParams params;

    private Queue<URI> waiting = new ConcurrentLinkedDeque<>();
    private Map<URI, Peer> connections = new LinkedHashMap<>();

    /**
     * Not block
     *
     * @param uri
     */
    public synchronized void connection(URI uri) throws ExecutionException, InterruptedException, TimeoutException {
        int max = params.settings.getMaxPeers();
        logger.info("Ready connection {}, max: {}, success: {}, waiting: {}", uri, max, connections.size(), waiting.size());
        if (connections.size() > max) {
            waiting.add(uri);
            return;
        }
        Bootstrap bootstrap = new Bootstrap()
                .group(executors)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ch.pipeline()
                                .addLast(new BitcoinNetEncode(localBlockChain.getChainParams()))
                                .addLast(new BitcoinNetDecode(localBlockChain.getChainParams()))
                                .addLast(peerChannelInboundHandlerEventPublisher);
                    }
                });
        // connection
        Peer peer = new Peer(localBlockChain, uri.getHost(), uri.getPort());
        try {
            ChannelFuture channelFuture = bootstrap.connect(peer.getHost(), peer.getPort()).addListener((DefaultChannelPromise e) -> {
                if (e.isSuccess()) {
                    Channel ch = e.channel();
                    ch.attr(Peer.PEER).set(peer);
                    InetSocketAddress address = (InetSocketAddress) ch.localAddress();
                    peer.setLocalAddress(address);
                    logger.info("Connection FROM: " + ch.localAddress() + ", TO: " + ch.remoteAddress());
                    connections.put(uri, peer);
                }
            });
            channelFuture.get(TIME_OUT_SECONDS, TimeUnit.SECONDS);
            ChannelFuture closeFuture = channelFuture.channel().closeFuture();
            closeFuture.addListener(e -> {
                logger.info("CLOSE connection, Peer {}:{}", peer.getHost(), peer.getPort());
                this.restart(uri);
                logger.debug("重新加入, 极端情况会导致 死循环, 所以这个里有个等待时间, 为了逻辑简单不使用 延迟队列 <DelayedConnection>");
                waiting.add(uri);
            });
            Thread.sleep(TIME_OUT_SECONDS * 1_000);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            connections.remove(uri);
            throw e;
        }
    }

    private void restart(URI uri) throws ExecutionException, InterruptedException, TimeoutException {
        connections.remove(uri);
        if (connections.size() < params.settings.getMaxPeers()) {
            URI r = waiting.poll();// block
            if (r != null) {
                this.connection(r);
            }
        }
    }

    @Override
    public void close() throws IOException {
        executors.shutdownGracefully();
    }
}
