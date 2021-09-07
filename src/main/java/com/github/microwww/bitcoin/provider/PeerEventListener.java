package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.event.BitcoinAddPeerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

@Configuration
public class PeerEventListener {
    private static final Logger logger = LoggerFactory.getLogger(PeerEventListener.class);
    private static final ExecutorService exec = Executors.newSingleThreadExecutor();

    @Component
    public static class BitcoinAddPeerListener implements ApplicationListener<BitcoinAddPeerEvent> {
        @Autowired
        PeerConnection peerConnection;

        @Override
        public void onApplicationEvent(BitcoinAddPeerEvent event) {
            exec.submit(() -> {
                URI uri = event.getBitcoinSource();
                try {
                    peerConnection.connection(uri);// no block !!
                } catch (TimeoutException e) {
                    this.errorLogger(uri, e);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    this.errorLogger(uri, e);
                } catch (RuntimeException | ExecutionException ex) {
                    logger.error("Connection {} ERROR !", uri, ex);
                }
            });
        }

        private void errorLogger(URI uri, Exception ex) {
            logger.error("Connection error [{}] : {}", ex.getClass().getSimpleName(), uri);
        }
    }
}
