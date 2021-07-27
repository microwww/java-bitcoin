package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.math.Uint64;
import com.github.microwww.bitcoin.net.Peer;
import io.netty.buffer.ByteBuf;

public class Pong extends AbstractProtocolAdapter<Pong> {
    private Uint64 nonce;

    public Pong(Peer peer) {
        super(peer);
    }

    @Override
    protected Pong read0(ByteBuf buf) {
        this.nonce = new Uint64(buf.readLongLE());
        return this;
    }

    @Override
    protected void write0(ByteBuf buf) {
        buf.writeLongLE(nonce.longValue());
    }

    public Uint64 getNonce() {
        return nonce;
    }

    public Pong setNonce(Uint64 nonce) {
        this.nonce = nonce;
        return this;
    }
}
