package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.net.Peer;

public class VerACK extends AbstractProtocolAdapter<VerACK> {
    public VerACK(Peer peer) {
        super(peer);
    }
}
