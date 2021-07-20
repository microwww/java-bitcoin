package com.github.microwww.bitcoin.block;

import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import io.netty.buffer.ByteBuf;

import java.util.Collections;
import java.util.List;

public class Transaction {
    private int version;
    private byte inputCount;
    private byte outputCount;
    private List<TxIn> txIn = Collections.emptyList();
    private List<TxOut> txOut = Collections.emptyList();
    private Uint32 lockTimeOrBlockId;

    public Transaction() {
    }

    public Uint256 hash() {
        throw new UnsupportedOperationException();
    }

    public static class TxOut {
        private long amount; // 转账金额
        // byte scriptLenth;
        private byte[] scriptPubKey;

        public void write(ByteBuf buf) {
            throw new UnsupportedOperationException();
        }

        public TxOut read(ByteBuf buf) {
            throw new UnsupportedOperationException();
        }

        public long getAmount() {
            return amount;
        }

        public void setAmount(long amount) {
            this.amount = amount;
        }

        public byte[] getScriptPubKey() {
            return scriptPubKey;
        }

        public void setScriptPubKey(byte[] scriptPubKey) {
            this.scriptPubKey = scriptPubKey;
        }
    }

    public static class TxIn {
        private OutPoint prevout; // 这个的输入是前一个输出
        private byte[] scriptSig;
        private Uint32 sequence;

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

    public class OutPoint {
        private Uint256 hash;
        private Uint32 n;
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
