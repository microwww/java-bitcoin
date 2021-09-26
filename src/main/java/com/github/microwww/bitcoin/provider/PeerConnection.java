package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.net.BitcoinNetDecode;
import com.github.microwww.bitcoin.net.BitcoinNetEncode;
import com.github.microwww.bitcoin.net.PeerChannelClientProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

@Component
public class PeerConnection implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(PeerConnection.class);
    private static final int TIME_OUT_SECONDS = 5;
    private static EventLoopGroup executors = new NioEventLoopGroup();
    private final CChainParams params;
    private final TaskManager<URI> taskManager;
    private final Timer timer = new Timer();

    @Autowired
    LocalBlockChain localBlockChain;
    @Autowired
    PeerChannelClientProtocol peerChannelClientProtocol;

    public PeerConnection(CChainParams params) {
        taskManager = new TaskManager<>(params.settings.getMaxPeers(), e -> {
            this.connection(e);
        });
        this.params = params;
    }

    public void addPeer(URI... uris) {
        int max = params.settings.getMaxPeers();
        for (URI u : uris) {
            logger.debug("Add new peer {}, max: {}, connect: {}, waiting: {}", u, max, taskManager.doing(), taskManager.waiting());
            taskManager.add(u);
        }
    }

    /**
     * Block to connection ... one by one !
     * InterruptedException, will all connection exit
     *
     * @param uri
     */
    public void connection(URI uri) {
        logger.info("Connection to {}, connect: {}, waiting: {}", uri, taskManager.doing(), taskManager.waiting());
        try {
            start(uri);
        } catch (TimeoutException e) {
            this.errorLogger(uri, e);
        } catch (InterruptedException e) {
            return;
        } catch (RuntimeException | ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof IOException) {// simple log
                logger.error("Connection to peer error : {}", cause.getMessage());
            } else {
                logger.error("Connection {} ERROR !", uri, cause);
            }
        } finally {
            if (Thread.currentThread().isInterrupted()) {
                Thread.interrupted();
                logger.error("Exit, Thread interrupted !");
                return;
            }
            if (taskManager.isEmpty()) {
                retryConnections();
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
        ChannelHandler handler = new PeerChannelClientHandler(peerChannelClientProtocol);
        Bootstrap bootstrap = new Bootstrap()
                .group(executors)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2 * TIME_OUT_SECONDS)
                .option(ChannelOption.SO_REUSEADDR, true)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ch.pipeline()
                                .addLast(new BitcoinNetEncode(localBlockChain.getChainParams()))
                                .addLast(new BitcoinNetDecode(localBlockChain.getChainParams()))
                                .addLast(handler);
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
            taskManager.remove(uri);
            logger.info("Remove {}, connect: {}, waiting: {}", uri, taskManager.doing(), taskManager.waiting());
            throw e;
        }
    }

    private void restart(URI uri) throws ExecutionException, InterruptedException, TimeoutException {
        taskManager.remove(uri);
        logger.info("Restart {}, connect: {}, waiting: {}", uri, taskManager.doing(), taskManager.waiting());
        addPeer(uri);
    }

    private void retryConnections() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (timer) {
                    if (taskManager.isEmpty()) {
                        logger.info("Retry all seed address");
                        for (URI peer : params.settings.peers()) {
                            taskManager.add(peer);
                        }
                        for (URI u : params.getEnvParams().seedsURI()) {
                            taskManager.add(u);
                        }
                    }
                }
            }
        }, 60_000);
    }

    @Override
    public void close() throws IOException {
        executors.shutdownGracefully();
    }

    private void errorLogger(URI uri, Exception ex) {
        logger.error("Connection error [ {} ] : {}", ex.getClass().getSimpleName(), uri);
    }
}
