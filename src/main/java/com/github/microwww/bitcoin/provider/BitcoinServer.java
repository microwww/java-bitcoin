package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.chain.BlockChainContext;
import com.github.microwww.bitcoin.conf.Config;
import com.github.microwww.bitcoin.event.BitcoinAddPeerEvent;
import com.github.microwww.bitcoin.net.Peer;
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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

@Component
public class BitcoinServer implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger logger = LoggerFactory.getLogger(BitcoinServer.class);

    private EventLoopGroup executors = new NioEventLoopGroup();
    private DefaultProgressivePromise<Void> future = new DefaultProgressivePromise(executors.next());

    @Autowired
    Config conf;
    @Autowired
    ApplicationEventPublisher publisher;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        executors.execute(() -> {
            logger.debug("scan local block-link data");
            try {
                Config conf = event.getApplicationContext().getBean(Config.class);
                BlockChainContext context = BlockChainContext.get();
                File file = loadLocalFile(conf);
                context.setSettings(conf.getBitcoin());
                // TODO: 暂时未存储, 直接在内存中
                context.setDataDir(file.toPath());
                context.init();
                future.setSuccess(null);

                // TODO: 启动server
                logger.info("start bitcoin server");

                conf.getBitcoin().toPeers().forEach(e -> {
                    this.addPeer(e);
                });
            } catch (RuntimeException e) {
                logger.error("Start bitcoin-server error !", e);
                future.setFailure(e);
            }
        });
    }

    public File loadLocalFile(Config conf) {
        String prefix = conf.getBitcoin().getEnv().getDataDirPrefix();
        File root = new File(conf.getBitcoin().getDataDir(), prefix);
        try {
            root.mkdirs();
            if (!root.canWrite()) {
                throw new RuntimeException("Not to writer dir : " + root.getCanonicalPath());
            }
            logger.info("scan local block-link data : {}", root.getCanonicalPath());
            File lock = new File(root, "lock");
            lock.createNewFile();
            new RandomAccessFile(lock, "rw").getChannel().lock();
            return root;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addPeer(Peer peer) {
        future.addListener((e) -> {
            logger.info("load peer : {}:{}", peer.getHost(), peer.getPort());
            publisher.publishEvent(new BitcoinAddPeerEvent(peer));
        });
    }

    public void addPeer(String host, int port) {
        this.addPeer(new Peer(host, port));
    }
}
