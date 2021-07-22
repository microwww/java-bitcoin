package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.math.Uint8;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;

public class TxIn {
    public Uint256 preTxHash;
    public int preTxOutIndex;
    public Uint8 scriptLength;
    public byte[] script;
    public Uint32 sequence;

    public void read(ByteBuf bf) {
        preTxHash = Uint256.read(bf);
        preTxOutIndex = bf.readIntLE();
        scriptLength = new Uint8(bf.readByte());
        script = ByteUtil.readLength(bf, scriptLength.intValue());
        sequence = new Uint32(bf.readIntLE());
    }

    public void write(ByteBuf bf) {
        bf.writeBytes(preTxHash.fill256bit());
        bf.writeIntLE(preTxOutIndex);
        bf.writeByte(script.length);
        bf.writeBytes(script);
        bf.writeIntLE(sequence.intValue());
    }

    public Uint256 getPreTxHash() {
        return preTxHash;
    }

    public void setPreTxHash(Uint256 preTxHash) {
        this.preTxHash = preTxHash;
    }

    public int getPreTxOutIndex() {
        return preTxOutIndex;
    }

    public void setPreTxOutIndex(int preTxOutIndex) {
        this.preTxOutIndex = preTxOutIndex;
    }

    public Uint8 getScriptLength() {
        return scriptLength;
    }

    public void setScriptLength(Uint8 scriptLength) {
        this.scriptLength = scriptLength;
    }

    public byte[] getScript() {
        return script;
    }

    public void setScript(byte[] script) {
        this.script = script;
    }

    public Uint32 getSequence() {
        return sequence;
    }

    public void setSequence(Uint32 sequence) {
        this.sequence = sequence;
    }

    @Override
    public String toString() {
        return "TxIn{" +
                "  preTxHash=" + preTxHash +
                ", preTxOutIndex=" + preTxOutIndex +
                ", scriptLength=" + scriptLength +
                ", script=" + ByteUtil.hex(script) +
                ", sequence=" + sequence +
                '}';
    }
}