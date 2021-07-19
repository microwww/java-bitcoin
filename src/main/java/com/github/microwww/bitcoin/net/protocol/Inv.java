package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.net.Peer;

public class Inv extends AbstractTypeHash<Inv> {
    public Inv(Peer peer) {
        super(peer);
    }
}
