package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.provider.Peer;
import io.netty.buffer.ByteBuf;

public class Tx extends AbstractProtocolAdapter<Tx> {
    private RawTransaction transaction = new RawTransaction();

    public Tx(Peer peer) {
        super(peer);
    }

    @Override
    protected void write0(ByteBuf buf) {
        transaction.write(buf);
    }

    @Override
    protected Tx read0(ByteBuf buf) {
        transaction.read(buf);
        return this;
    }

    public RawTransaction getTransaction() {
        return transaction;
    }

    public void setTransaction(RawTransaction transaction) {
        this.transaction = transaction;
    }
}
