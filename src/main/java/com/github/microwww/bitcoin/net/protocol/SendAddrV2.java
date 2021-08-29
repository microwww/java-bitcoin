package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.provider.Peer;

public class SendAddrV2 extends AbstractProtocolAdapter<SendAddrV2> {
    public SendAddrV2(Peer peer) {
        super(peer);
    }
}
