package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.event.BitcoinAddPeerEvent;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultProgressivePromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.channels.FileLock;
import java.util.List;

@Component
public class BitcoinStarter implements ApplicationListener<ApplicationReadyEvent>, Closeable {
    private static final Logger logger = LoggerFactory.getLogger(BitcoinStarter.class);
    private static FileLock fileLock;

    private EventLoopGroup executors = new NioEventLoopGroup();
    private DefaultProgressivePromise<Void> future = new DefaultProgressivePromise(executors.next());

    @Autowired
    LocalBlockChain localBlockChain;
    @Autowired
    ApplicationEventPublisher publisher;
    @Autowired
    CChainParams chainParams;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        File file = chainParams.settings.lockupRootDirectory();
        lockFile(file);
        executors.execute(() -> {
            logger.debug("scan local block-link data");
            try {
                Settings conf = event.getApplicationContext().getBean(Settings.class);

                localBlockChain.getDiskBlock().init();

                // 启动server
                ServerStarter sv = event.getApplicationContext().getBean(ServerStarter.class);
                sv.newThreadSTART(f -> {
                    future.setSuccess(null);
                }, null);

                List<URI> peers = conf.toPeers();
                if (peers.isEmpty()) {
                    peers = chainParams.getEnvParams().seedsURI();
                    logger.warn("No find PEER, you can set it : `conf.bitcoin.peers`, START with system params.seeds {}", peers.size());
                }
                peers.forEach(e -> {
                    this.addPeer(new Peer(localBlockChain, e.getHost(), e.getPort()));
                });
            } catch (RuntimeException e) {
                logger.error("Start bitcoin-server error !", e);
                future.setFailure(e);
            }
        });
    }

    public void addPeer(Peer peer) {
        future.addListener((e) -> {
            logger.debug("find peer : {}:{}", peer.getHost(), peer.getPort());
            publisher.publishEvent(new BitcoinAddPeerEvent(Peer.uri(peer.getHost(), peer.getPort())));
        });
    }

    public void addPeer(String host, int port) {
        this.addPeer(new Peer(localBlockChain, host, port));
    }

    private synchronized void lockFile(File root) {
        if (fileLock == null) {
            try {
                File lock = new File(root, ".lock");
                lock.createNewFile();
                fileLock = new RandomAccessFile(lock, "rw").getChannel().lock();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public void close() throws IOException {
        fileLock.close();
    }
}
