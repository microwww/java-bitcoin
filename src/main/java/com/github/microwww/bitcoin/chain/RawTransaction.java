package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.math.Uint8;
import com.github.microwww.bitcoin.math.UintVar;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.util.Assert;

public class RawTransaction {
    private int version;
    private byte marker = 0;
    private byte flag = 0;
    private UintVar inputCount;
    private TxIn[] txIns;
    private UintVar outputCount;
    private TxOut[] txOuts;
    private Uint32 lockTime;

    public void read(ByteBuf bf) {
        version = bf.readIntLE();
        inputCount = UintVar.parse(bf);
        if (inputCount.intValueExact() == 0) {
            marker = 0;
            flag = bf.readByte();
            Assert.isTrue(1 == flag, "Must 0x0001");
            inputCount = UintVar.parse(bf);
        }
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
        outputCount = UintVar.parse(bf);
        len = outputCount.intValueExact();
        txOuts = new TxOut[len];
        for (int i = 0; i < len; i++) {
            TxOut out = new TxOut();
            out.read(bf);
            txOuts[i] = out;
        }
        // TODO:: 隔离见证, 格式??
        if (flag == 1) {
            for (TxIn txIn : txIns) {
                UintVar count = UintVar.parse(bf);
                byte[][] v = new byte[count.intValueExact()][];
                for (int i = 0; i < v.length; i++) {
                    len = UintVar.parse(bf).intValueExact();
                    v[i] = ByteUtil.readLength(bf, len);
                }
                txIn.setTxWitness(v);
            }
        }
        lockTime = new Uint32(bf.readIntLE());
    }

    public Uint256 hash() {
        ByteBuf bf = Unpooled.buffer();
        write(bf, (byte) 0);
        return new Uint256(ByteUtil.sha256sha256(ByteUtil.readAll(bf)));
    }

    public Uint256 whash() {
        ByteBuf bf = Unpooled.buffer();
        write(bf);
        return new Uint256(ByteUtil.sha256sha256(ByteUtil.readAll(bf)));
    }

    public void write(ByteBuf bf) {
        boolean flag = false;
        for (TxIn txIn : this.getTxIns()) {
            if (txIn.getTxWitness() != null) {
                flag = true;
                break;
            }
        }
        write(bf, flag ? 1 : 0);
    }

    public ByteBuf serialize(int witness) {
        ByteBuf buffer = Unpooled.buffer();
        write(buffer, witness);
        return buffer;
    }

    public void write(ByteBuf bf, int witness) {
        bf.writeIntLE(version);
        //////// IN
        if (witness != 0) {
            bf.writeBytes(new byte[]{marker, (byte) witness});
        }
        UintVar.valueOf(txIns.length).write(bf);
        for (TxIn txIn : txIns) {
            txIn.write(bf);
        }
        ////// OUT
        UintVar.valueOf(txOuts.length).write(bf);
        for (TxOut txOut : txOuts) {
            txOut.write(bf);
        }
        if (witness != 0) for (TxIn txIn : txIns) {
            byte[][] wt = txIn.getTxWitness();
            if (wt == null) {
                UintVar.ZERO.write(bf);
                continue;
            }
            UintVar.valueOf(wt.length).write(bf);
            for (byte[] bytes : wt) {
                UintVar.valueOf(bytes.length).write(bf);
                bf.writeBytes(bytes);
            }
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

    public UintVar getOutputCount() {
        return outputCount;
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

    public byte getFlag() {
        return flag;
    }

    public boolean isWitness() {
        return flag != 0;
    }

    @Override
    public RawTransaction clone() {
        RawTransaction tx = new RawTransaction();
        ByteBuf bf = Unpooled.buffer();
        this.write(bf);
        tx.read(bf);
        return tx;
    }

    @Override
    public String toString() {
        return toString(new StringBuilder(), "").toString();
    }

    public StringBuilder toString(StringBuilder builder, String prefix) {
        builder
                .append(prefix).append(" hash    = ").append(hash())
                .append(prefix).append(" version = ").append(version)
                .append(prefix).append(" txIns   = ").append(inputCount);
        for (TxIn txIn : txIns) {
            txIn.toString(builder, prefix + "        ");
        }
        builder.append(prefix).append(" txOuts  = ").append(outputCount);

        for (TxOut txIn : txOuts) {
            txIn.toString(builder, prefix + "        ");
        }

        builder.append(prefix).append("lockTime = ").append(lockTime);
        return builder;
    }
}
