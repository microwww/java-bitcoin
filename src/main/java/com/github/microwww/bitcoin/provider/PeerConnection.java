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
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@Component
public class PeerConnection implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(PeerConnection.class);
    private static EventLoopGroup executors = new NioEventLoopGroup();

    @Autowired
    LocalBlockChain localBlockChain;
    @Autowired
    PeerChannelInboundHandler peerChannelInboundHandlerEventPublisher;
    @Autowired
    CChainParams params;

    private DelayQueue<DelayedConnection> connections = new DelayQueue<>();
    private Map<URI, Peer> peers = new LinkedHashMap<>();

    /**
     * Not block
     *
     * @param uri
     */
    public synchronized void connection(URI uri) {
        int max = params.settings.getMaxPeers();
        logger.info("Ready connection {}, max: {}, success: {}, waiting: {}", max, uri, peers.size(), connections.size());
        if (peers.size() > max) {
            connections.add(new DelayedConnection(uri, 0));
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
                    peers.put(uri, peer);
                }
            });
            ChannelFuture closeFuture = channelFuture.sync().channel().closeFuture();
            closeFuture.addListener(e -> {
                logger.info("CLOSE connection, Peer {}:{}", peer.getHost(), peer.getPort());
                this.restart(uri);
                connections.add(new DelayedConnection(uri));
            }).await(5_000);
        } catch (InterruptedException e) {
            logger.error("Peer error : {}", peer, e);
            peers.remove(uri);
        }
    }

    private void restart(URI uri) {
        peers.remove(uri);
        if (peers.size() < params.settings.getMaxPeers()) {
            while (true) {
                try {
                    DelayedConnection poll = connections.take();// block
                    if (poll != null) {
                        this.connection(poll.uri);
                    }
                } catch (InterruptedException e) {
                }
                return;
            }
        }
    }

    @Override
    public void close() throws IOException {
        executors.shutdownGracefully();
    }

    public static class DelayedConnection implements Delayed {
        public static final int TimeMillis = 10 * 1000;
        private final long start = System.currentTimeMillis();
        public final URI uri;
        public final int waitingMillis;

        public DelayedConnection(URI uri, int waitingMillis) {
            Assert.isTrue(uri != null, "Not null");
            this.uri = uri;
            this.waitingMillis = waitingMillis;
        }

        public DelayedConnection(URI uri) {
            this(uri, TimeMillis);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(start + waitingMillis - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            if (this == o) {
                return 0;
            }
            if (o instanceof DelayedConnection) {
                return uri.compareTo(((DelayedConnection) o).uri);
            }
            throw new UnsupportedOperationException();
        }
    }

}
