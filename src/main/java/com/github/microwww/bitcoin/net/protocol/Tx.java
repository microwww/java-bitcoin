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
        // byte scriptLenth;
        byte[] scriptPubKey;

        public void write(ByteBuf buf) {
            throw new UnsupportedOperationException();
        }

        public TxOut read(ByteBuf buf) {
            throw new UnsupportedOperationException();
        }

        public long getVal() {
            return val;
        }

        public void setVal(long val) {
            this.val = val;
        }

        public byte[] getScriptPubKey() {
            return scriptPubKey;
        }

        public void setScriptPubKey(byte[] scriptPubKey) {
            this.scriptPubKey = scriptPubKey;
        }
    }

    public static class TxIn {
        private byte[] scriptSig;
        public void write(ByteBuf buf) {
            throw new UnsupportedOperationException();
        }

        public TxIn read(ByteBuf buf) {
            throw new UnsupportedOperationException();
        }

        public byte[] getScriptSig() {
            return scriptSig;
        }

        public void setScriptSig(byte[] scriptSig) {
            this.scriptSig = scriptSig;
        }
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public byte getInputCount() {
        return inputCount;
    }

    public void setInputCount(byte inputCount) {
        this.inputCount = inputCount;
    }

    public byte getOutputCount() {
        return outputCount;
    }

    public void setOutputCount(byte outputCount) {
        this.outputCount = outputCount;
    }

    public List<TxIn> getTxIn() {
        return txIn;
    }

    public void setTxIn(List<TxIn> txIn) {
        this.txIn = txIn;
    }

    public List<TxOut> getTxOut() {
        return txOut;
    }

    public void setTxOut(List<TxOut> txOut) {
        this.txOut = txOut;
    }

    public Uint32 getLockTimeOrBlockId() {
        return lockTimeOrBlockId;
    }

    public void setLockTimeOrBlockId(Uint32 lockTimeOrBlockId) {
        this.lockTimeOrBlockId = lockTimeOrBlockId;
    }
}
