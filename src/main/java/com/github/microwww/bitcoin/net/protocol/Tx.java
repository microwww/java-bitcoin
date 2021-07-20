package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.net.Peer;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tx extends AbstractProtocolAdapter<Tx> {
    int version;
    byte inputCount;
    byte outputCount;
    List<TxIn> txIn = Collections.emptyList();
    List<TxOut> txOut = Collections.emptyList();
    Uint32 lockTimeOrBlockId;

    public Tx(Peer peer) {
        super(peer);
    }

    @Override
    protected void write0(ByteBuf buf) {
        buf.writeIntLE(this.version);
        buf.writeByte(this.txIn.size());
        buf.writeByte(this.txOut.size());
        for (TxIn in : txIn) {
            in.write(buf);
        }
        for (TxOut out : txOut) {
            out.write(buf);
        }
    }

    @Override
    protected Tx read0(ByteBuf buf) {
        this.version = buf.readIntLE();
        this.inputCount = buf.readByte();
        this.outputCount = buf.readByte();
        {
            List<TxIn> list = new ArrayList(inputCount);
            for (int i = 0; i < inputCount; i++) {
                TxIn in = new TxIn().read(buf);
                list.add(in);
            }
        }
        {
            List<TxOut> list = new ArrayList(inputCount);
            for (int i = 0; i < outputCount; i++) {
                TxOut out = new TxOut().read(buf);
                list.add(out);
            }
        }
        this.lockTimeOrBlockId = new Uint32(buf.readIntLE());
        return this;
    }

    public static class TxOut {
        long val;
        byte scriptLenth;
        byte[] script;

        public void write(ByteBuf buf) {
            throw new UnsupportedOperationException();
        }

        public TxOut read(ByteBuf buf) {
            throw new UnsupportedOperationException();
        }
    }

    public static class TxIn {
        public void write(ByteBuf buf) {
            throw new UnsupportedOperationException();
        }

        public TxIn read(ByteBuf buf) {
            throw new UnsupportedOperationException();
        }
    }
}
