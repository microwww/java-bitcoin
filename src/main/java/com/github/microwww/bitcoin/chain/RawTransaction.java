package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.math.Uint8;
import com.github.microwww.bitcoin.math.UintVar;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.util.Assert;

import java.util.Arrays;

public class RawTransaction {
    private int version;
    private byte marker = 0;
    private byte flag = 0;
    private UintVar inputCount;
    private TxIn[] txIns;
    private Uint8 outputCount;
    private TxOut[] txOuts;
    private Uint8 witnessVersion;
    private byte[][] txWitness; // 隔离见证
    private Uint32 lockTime;

    public void read(ByteBuf bf) {
        version = bf.readIntLE();
        bf.markReaderIndex();
        Uint8 uic = new Uint8(bf.readByte());
        if (uic.intValue() == 0) {
            marker = uic.byteValue();
            flag = bf.readByte();
            Assert.isTrue(1 == flag, "Must 0x0001");
        } else {
            bf.resetReaderIndex();
        }
        inputCount = UintVar.reader(bf);
        //////// IN
        Assert.isTrue(inputCount.intValue() != 0, "Must > 0");
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
        // TODO:: 隔离见证, 格式??
        if (flag == 1) {
            witnessVersion = new Uint8(bf.readByte());
        }
        // ByteUtil.readLength(bf, );
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
        if (version == 2) {
            bf.writeBytes(new byte[]{marker, flag});
        }
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

    public UintVar getInputCount() {
        return inputCount;
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
