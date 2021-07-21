package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.block.RawTransaction;
import com.github.microwww.bitcoin.block.TxIn;
import com.github.microwww.bitcoin.block.TxOut;
import com.github.microwww.bitcoin.net.Peer;
import io.netty.buffer.ByteBuf;

public class Tx extends AbstractProtocolAdapter<Tx> {
    private RawTransaction transaction;

    public Tx(Peer peer) {
        super(peer);
    }

    @Override
    protected void write0(ByteBuf buf) {
        buf.writeIntLE(transaction.getVersion());
        buf.writeByte(transaction.getTxIns().length);
        buf.writeByte(transaction.getTxOuts().length);
        for (TxIn in : transaction.getTxIns()) {
            in.write(buf);
        }
        for (TxOut out : transaction.getTxOuts()) {
            out.write(buf);
        }
    }

    @Override
    protected Tx read0(ByteBuf buf) {
        transaction.read(buf);
        return this;
    }

}
