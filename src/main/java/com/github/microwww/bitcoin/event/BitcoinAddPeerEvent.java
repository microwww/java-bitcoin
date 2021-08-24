package com.github.microwww.bitcoin.event;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 有新的节点添加, PeerConnection 负责接受, 启动客户端
 */
public class BitcoinAddPeerEvent extends BitcoinEvent<URI> {

    public BitcoinAddPeerEvent(URI uri) {
        super(uri);
    }
}
