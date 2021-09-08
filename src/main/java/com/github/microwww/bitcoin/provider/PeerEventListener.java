package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.event.BitcoinAddPeerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class PeerEventListener {
    private static final Logger logger = LoggerFactory.getLogger(PeerEventListener.class);

    @Component
    public static class BitcoinAddPeerListener implements ApplicationListener<BitcoinAddPeerEvent> {
        @Autowired
        PeerConnection peerConnection;

        @Override
        public void onApplicationEvent(BitcoinAddPeerEvent event) {
            peerConnection.addPeer(event.getBitcoinSource());// no block !!
        }
    }
}
