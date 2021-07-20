package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.block.Transaction;
import com.github.microwww.bitcoin.block.Transaction.TxIn;
import com.github.microwww.bitcoin.block.Transaction.TxOut;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.net.Peer;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class Tx extends AbstractProtocolAdapter<Tx> {
    private Transaction tx;

    public Tx(Peer peer) {
        super(peer);
    }

    @Override
    protected void write0(ByteBuf buf) {
        buf.writeIntLE(tx.getVersion());
        buf.writeByte(tx.getTxIn().size());
        buf.writeByte(tx.getTxOut().size());
        for (TxIn in : tx.getTxIn()) {
            in.write(buf);
        }
        for (TxOut out : tx.getTxOut()) {
            out.write(buf);
        }
    }

    @Override
    protected Tx read0(ByteBuf buf) {
        tx.setVersion(buf.readIntLE());
        tx.setInputCount(buf.readByte());
        tx.setOutputCount(buf.readByte());
        {
            List<TxIn> list = new ArrayList(tx.getInputCount());
            for (int i = 0; i < tx.getInputCount(); i++) {
                TxIn in = new TxIn().read(buf);
                list.add(in);
            }
        }
        {
            List<TxOut> list = new ArrayList(tx.getOutputCount());
            for (int i = 0; i < tx.getOutputCount(); i++) {
                TxOut out = new TxOut().read(buf);
                list.add(out);
            }
        }
        tx.setLockTimeOrBlockId(new Uint32(buf.readIntLE()));
        return this;
    }

}
