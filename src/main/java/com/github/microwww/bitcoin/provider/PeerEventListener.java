package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.event.BitcoinAddPeerEvent;
import com.github.microwww.bitcoin.net.PeerConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class PeerEventListener {
    private static final ExecutorService exec = Executors.newSingleThreadExecutor();

    @Component
    public static class BitcoinAddPeerListener implements ApplicationListener<BitcoinAddPeerEvent> {
        @Autowired
        PeerConnection peerConnection;

        @Override
        public void onApplicationEvent(BitcoinAddPeerEvent event) {
            exec.submit(() -> {
                peerConnection.connection(event.getBitcoinSource());// no block !!
            });
        }
    }
}
