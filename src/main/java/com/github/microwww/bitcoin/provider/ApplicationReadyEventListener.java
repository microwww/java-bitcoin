package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.conf.Settings;
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

@Component
public class ApplicationReadyEventListener implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationReadyEventListener.class);

    private EventLoopGroup executors = new NioEventLoopGroup();
    private DefaultProgressivePromise<Void> future = new DefaultProgressivePromise(executors.next());

    @Autowired
    LocalBlockChain localBlockChain;
    @Autowired
    ApplicationEventPublisher publisher;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        executors.execute(() -> {
            logger.debug("scan local block-link data");
            try {
                Settings conf = event.getApplicationContext().getBean(Settings.class);

                // TODO: 启动server
                logger.info("start bitcoin server");

                future.setSuccess(null);
                conf.toPeers().forEach(e -> {
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
            logger.info("load peer : {}:{}", peer.getHost(), peer.getPort());
            publisher.publishEvent(new BitcoinAddPeerEvent(peer));
        });
    }

    public void addPeer(String host, int port) {
        this.addPeer(new Peer(localBlockChain, host, port));
    }
}
