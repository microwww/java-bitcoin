package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.net.Peer;
import io.netty.buffer.ByteBuf;

public class GetHeaders extends AbstractProtocolAdapter<GetHeaders> {

    public GetHeaders(Peer peer) {
        super(peer);
    }

    @Override
    public int write(ByteBuf buf) {
        return super.write(buf);
    }
}
