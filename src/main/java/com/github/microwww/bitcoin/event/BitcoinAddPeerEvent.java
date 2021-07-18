package com.github.microwww.bitcoin.event;

import com.github.microwww.bitcoin.net.Peer;

/**
 * 有新的节点添加
 */
public class BitcoinAddPeerEvent extends BitcoinEvent<Peer> {

    public BitcoinAddPeerEvent(Peer source) {
        super(source);
    }
}
