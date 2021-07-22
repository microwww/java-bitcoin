package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.math.Uint8;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Arrays;

public class RawTransaction {
    private int version;
    private Uint8 inputCount;
    private TxIn[] txIns;
    private Uint8 outputCount;
    private TxOut[] txOuts;
    private Uint32 lockTime;

    public void read(ByteBuf bf) {
        version = bf.readIntLE();
        //////// IN
        inputCount = new Uint8(bf.readByte());
        int len = inputCount.intValue();
        txIns = new TxIn[len];
        for (int i = 0; i < len; i++) {
            TxIn in = new TxIn();
            in.read(bf);
            txIns[i] = in;
        }
        ////// OUT
        outputCount = new Uint8(bf.readByte());
        len = outputCount.intValue();
        txOuts = new TxOut[len];
        for (int i = 0; i < len; i++) {
            TxOut out = new TxOut();
            out.read(bf);
            txOuts[i] = out;
        }
        lockTime = new Uint32(bf.readIntLE());
    }

    public Uint256 hash() {
        ByteBuf bf = Unpooled.buffer();
        write(bf);
        return new Uint256(ByteUtil.sha256sha256(ByteUtil.readAll(bf)));
    }

    public void write(ByteBuf bf) {
        bf.writeIntLE(version);
        //////// IN
        bf.writeByte(txIns.length);
        for (TxIn txIn : txIns) {
            txIn.write(bf);
        }
        ////// OUT
        bf.writeByte(txOuts.length);
        for (TxOut txOut : txOuts) {
            txOut.write(bf);
        }
        bf.writeIntLE(lockTime.intValue());
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Uint8 getInputCount() {
        return inputCount;
    }

    public void setInputCount(Uint8 inputCount) {
        this.inputCount = inputCount;
    }

    public TxIn[] getTxIns() {
        return txIns;
    }

    public void setTxIns(TxIn[] txIns) {
        this.txIns = txIns;
    }

    public Uint8 getOutputCount() {
        return outputCount;
    }

    public void setOutputCount(Uint8 outputCount) {
        this.outputCount = outputCount;
    }

    public TxOut[] getTxOuts() {
        return txOuts;
    }

    public void setTxOuts(TxOut[] txOuts) {
        this.txOuts = txOuts;
    }

    public Uint32 getLockTime() {
        return lockTime;
    }

    public void setLockTime(Uint32 lockTime) {
        this.lockTime = lockTime;
    }

    @Override
    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    public StringBuilder toString(StringBuilder builder) {
        builder.append("RawTransaction{")
                .append("  hash=").append(hash())
                .append(", version=").append(version)
                .append(", inputCount=").append(inputCount)
                .append(", txIns=").append(Arrays.toString(txIns))
                .append(", outputCount=").append(outputCount)
                .append(", txOuts=").append(Arrays.toString(txOuts))
                .append(", lockTime=").append(lockTime)
                .append('}');
        return builder;
    }
}
