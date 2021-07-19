package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.net.Peer;

public class GetData extends AbstractTypeHash<GetData> {
    public GetData(Peer peer) {
        super(peer);
    }
}
