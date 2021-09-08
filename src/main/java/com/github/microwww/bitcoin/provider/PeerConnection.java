package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.event.BitcoinAddPeerEvent;
import com.github.microwww.bitcoin.net.BitcoinNetDecode;
import com.github.microwww.bitcoin.net.BitcoinNetEncode;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.*;

@Component
public class PeerConnection implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(PeerConnection.class);
    private static final int TIME_OUT_SECONDS = 5;
    private static EventLoopGroup executors = new NioEventLoopGroup();
    private final CChainParams params;

    @Autowired
    LocalBlockChain localBlockChain;
    @Autowired
    PeerChannelInboundHandler peerChannelInboundHandlerEventPublisher;
    @Autowired
    ApplicationEventPublisher publisher;

    private BlockingQueue<URI> waiting = new LinkedBlockingQueue<>();
    private Map<URI, Peer> connections = new ConcurrentHashMap<>();

    public PeerConnection(CChainParams params) {
        this.params = params;
        init();
    }

    private void init() {
        int max = params.settings.getMaxPeers();
        executors.next().execute(() -> {
            while (true) try {// 循环获取
                if (connections.size() > max) {
                    Thread.sleep(TIME_OUT_SECONDS * 1000);
                    continue;
                }
                URI uri = waiting.poll(TIME_OUT_SECONDS, TimeUnit.SECONDS);
                if (uri != null) {
                    connection(uri);
                } else {
                    if (connections.isEmpty()) {
                        logger.warn("No peer to connect ,RESTART by system seed [{}], waiting....",
                                params.getEnvParams().seedsURI().size());
                        for (URI u : params.getEnvParams().seedsURI()) {
                            publisher.publishEvent(new BitcoinAddPeerEvent(u));
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.interrupted();
            } catch (RuntimeException ex) {
                logger.error("IGNORE ! Peer listener thread error.", ex);
            }
        });
    }

    public void addPeer(URI... uris) {
        int max = params.settings.getMaxPeers();
        for (URI u : uris) {
            logger.debug("Add new peer {}, max: {}, success: {}, waiting: {}", u, max, connections.size(), waiting.size());
            waiting.add(u);
        }
    }

    /**
     * Not block
     *
     * @param uri
     */
    public void connection(URI uri) {
        logger.info("Connection to {}, success: {}, waiting: {}", uri, connections.size(), waiting.size());
        try {
            start(uri);
        } catch (TimeoutException e) {
            this.errorLogger(uri, e);
        } catch (InterruptedException e) {
            Thread.interrupted();
            this.errorLogger(uri, e);
        } catch (RuntimeException | ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof IOException) {// simple log
                logger.error("Connection to peer error : {}", cause.getMessage());
            } else {
                logger.error("Connection {} ERROR !", uri, cause);
            }
        }
    }

    /**
     * 注意 synchronized 非阻塞, 有 5S 的默认等待时间, 为了简单每次只允许一个
     *
     * @param uri
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    private synchronized void start(URI uri) throws ExecutionException, InterruptedException, TimeoutException {
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
            });
            logger.debug("重新加入, 极端情况会导致 死循环, 所以这个里有个等待时间, 为了逻辑简单不使用 延迟队列 <DelayedConnection>");
            Thread.sleep(TIME_OUT_SECONDS * 1_000);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            connections.remove(uri);
            throw e;
        }
    }

    private void restart(URI uri) throws ExecutionException, InterruptedException, TimeoutException {
        connections.remove(uri);
        addPeer(uri);
    }

    @Override
    public void close() throws IOException {
        executors.shutdownGracefully();
    }

    private void errorLogger(URI uri, Exception ex) {
        logger.error("Connection error [ {} ] : {}", ex.getClass().getSimpleName(), uri);
    }
}
