package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.math.UintVar;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.util.Assert;

import java.text.DecimalFormat;
import java.util.Optional;

public class RawTransaction implements ByteSerializable {
    private int version;
    private byte marker = 0;
    private byte flag = 0;
    // private UintVar inputCount;
    private TxIn[] txIns = new TxIn[]{};
    // private UintVar outputCount;
    private TxOut[] txOuts = new TxOut[]{};
    private Uint32 lockTime = Uint32.ZERO;

    private void read(ByteBuf bf) {
        version = bf.readIntLE();
        UintVar inputCount = UintVar.parse(bf);
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
        UintVar outputCount = UintVar.parse(bf);
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

    private void write(ByteBuf bf) {
        boolean flag = isWitness();
        if (!flag) {
            for (TxIn txIn : this.getTxIns()) {
                if (txIn.getTxWitness().isPresent()) {
                    flag = true;
                    break;
                }
            }
        }
        write(bf, flag ? 1 : 0);
    }

    public ByteBuf serialization(ByteBuf buffer, int witness) {
        write(buffer, witness);
        return buffer;
    }

    private void write(ByteBuf bf, int witness) {
        bf.writeIntLE(version);
        //////// IN
        if (witness != 0) {
            Assert.isTrue(marker == 0, "marker must equal 0");
            bf.writeBytes(new byte[]{0, (byte) witness});
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
            Optional<byte[][]> opt = txIn.getTxWitness();
            if (!opt.isPresent()) {
                UintVar.ZERO.write(bf);
                continue;
            }
            byte[][] wt = opt.get();
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

    private UintVar getInputCount() {
        return UintVar.valueOf(txIns.length);
    }

    public TxIn[] getTxIns() {
        return txIns;
    }

    public void setTxIns(TxIn[] txIns) {
        this.txIns = txIns;
    }

    private UintVar getOutputCount() {
        return UintVar.valueOf(txOuts.length);
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

    public void checkLockTime() {// TODO: checkLockTime
        long t = lockTime.longValue();
        if (t < 500_000_000) {
            //isBlock
        } else {
            // system-time
        }
        throw new UnsupportedOperationException();
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

    public StringBuilder beautify() {
        ByteBuf bf = Unpooled.buffer();
        this.serialization(bf, this.getFlag());
        RawTransaction tr = this;
        StringBuilder sb = new StringBuilder();
        String fm = "%12s: ";
        hexLength(sb.append(String.format(fm, "version")), bf, 4).append("\n");
        // private byte marker = 0;
        if (tr.getFlag() != 0) {
            hexLength(sb.append(String.format(fm, "witness")), bf, 1);
            hexLength(sb.append(" "), bf, 1).append("\n");
        }

        hexLength(sb.append(String.format(fm, "in-count")), bf, this.getInputCount().bytesLength()).append("\n");
        for (TxIn in : tr.getTxIns()) {
            hexLength(sb.append(String.format(fm, "preHash")), bf, 32).append("\n");
            hexLength(sb.append(String.format(fm, "pre-index")), bf, 4).append(" -> ").append(in.getPreTxOutIndex()).append("\n");
            hexLength(sb.append(String.format(fm, "script")), bf, in.getScriptLength().bytesLength());
            hexLength(sb.append(" "), bf, in.getScript().length).append("\n");
            hexLength(sb.append(String.format(fm, "sequence")), bf, 4).append("\n");
        }

        hexLength(sb.append(String.format(fm, "out-count")), bf, this.getOutputCount().bytesLength()).append("\n");
        for (TxOut out : tr.getTxOuts()) {
            String v = new DecimalFormat("#,####").format(out.getValue());
            hexLength(sb.append(String.format(fm, "amount")), bf, 8).append(" -> ").append(v).append("\n");
            hexLength(sb.append(String.format(fm, "script")), bf, out.getScriptLength().bytesLength());
            hexLength(sb.append(" "), bf, out.getScriptPubKey().length).append("\n");
        }

        if (tr.getFlag() != 0) {
            for (TxIn in : tr.getTxIns()) {
                byte[][] tw = in.getTxWitness().orElse(new byte[][]{});
                int len = UintVar.valueOf(tw.length).bytesLength();
                hexLength(sb.append(String.format(fm, "witness")), bf, len).append("\n");
                for (int i = 0; i < tw.length; i++) {
                    byte[] w = tw[i];
                    len = UintVar.valueOf(w.length).bytesLength();
                    hexLength(sb.append(String.format(fm, "w" + i + "")), bf, len);
                    hexLength(sb.append(" "), bf, w.length).append("\n");
                }
            }
        }
        hexLength(sb.append(String.format(fm, "lockTime")), bf, 4);
        return sb;
    }

    @Override
    public String toString() {
        return toString(new StringBuilder(), "").toString();
    }

    public StringBuilder toString(StringBuilder builder, String prefix) {
        builder
                .append(prefix).append(" hash    = ").append(hash())
                .append(prefix).append(" version = ").append(version)
                .append(prefix).append(" txIns   = ").append(txIns.length);
        for (TxIn txIn : txIns) {
            txIn.toString(builder, prefix + "        ");
        }
        builder.append(prefix).append(" txOuts  = ").append(txOuts.length);

        for (TxOut txIn : txOuts) {
            txIn.toString(builder, prefix + "        ");
        }

        builder.append(prefix).append("lockTime = ").append(lockTime);
        return builder;
    }

    private static StringBuilder hexLength(StringBuilder sb, ByteBuf buf, int len) {
        ByteUtil.hex(sb, buf, len);
        return sb;
    }

    @Override
    public ByteBuf serialization(ByteBuf buffer) {
        return serialization(buffer, this.flag);
    }

    @Override
    public ByteBuf deserialization(ByteBuf buffer) {
        this.read(buffer);
        return buffer;
    }
}
