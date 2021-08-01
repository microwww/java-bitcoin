package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.math.UintVar;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TxIn {
    private Uint256 preTxHash;
    private int preTxOutIndex;
    private UintVar scriptLength;
    private byte[] script;
    private Uint32 sequence;
    private byte[][] txWitness;

    public void read(ByteBuf bf) {
        preTxHash = Uint256.read(bf);
        preTxOutIndex = bf.readIntLE();
        scriptLength = UintVar.parse(bf);
        script = ByteUtil.readLength(bf, scriptLength.intValueExact());
        sequence = new Uint32(bf.readIntLE());
    }

    public void write(ByteBuf bf) {
        bf.writeBytes(preTxHash.fill256bit());
        bf.writeIntLE(preTxOutIndex);
        UintVar.valueOf(script.length).write(bf);
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

    public UintVar getScriptLength() {
        return scriptLength;
    }

    public byte[] getScript() {
        return script;
    }

    public TxIn setScript(byte[] script) {
        this.script = script;
        return this;
    }

    public Uint32 getSequence() {
        return sequence;
    }

    public void setSequence(Uint32 sequence) {
        this.sequence = sequence;
    }

    public byte[][] getTxWitness() {
        return txWitness;
    }

    public void setTxWitness(byte[][] txWitness) {
        this.txWitness = txWitness;
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