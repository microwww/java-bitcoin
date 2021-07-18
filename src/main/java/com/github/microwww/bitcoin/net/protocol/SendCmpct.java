package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.net.Peer;

public class SendCmpct extends AbstractProtocolAdapter<SendCmpct> {

    public SendCmpct(Peer peer) {
        super(peer);
    }
}
