package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.provider.Peer;
import io.netty.buffer.ByteBuf;

public class FeeFilter extends AbstractProtocolAdapter<FeeFilter> {
    private long fee = 1000;

    public FeeFilter(Peer peer) {
        super(peer);
    }

    @Override
    protected FeeFilter read0(ByteBuf buf) {
        this.fee = buf.readLongLE();
        return this;
    }

    @Override
    protected void write0(ByteBuf buf) {
        buf.writeLongLE(fee);
    }

    public long getFee() {
        return fee;
    }

    public FeeFilter setFee(long fee) {
        this.fee = fee;
        return this;
    }
}
