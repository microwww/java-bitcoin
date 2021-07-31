package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.math.UintVar;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TxIn {
    public Uint256 preTxHash;
    public int preTxOutIndex;
    public UintVar scriptLength;
    public byte[] script;
    public Uint32 sequence;
    private byte[][] txWitness;

    public void read(ByteBuf bf) {
        preTxHash = Uint256.read(bf);
        preTxOutIndex = bf.readIntLE();
        scriptLength = UintVar.reader(bf);
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

    public TxIn setScript(SignatureScript script) {
        this.script = script.getData();
        return this;
    }

    public SignatureScript parseSignatureScript() {
        ByteBuf byteBuf = Unpooled.copiedBuffer(this.script);
        return new SignatureScript().read(byteBuf);
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

    public static class SignatureScript {
        private byte[] signature;
        private byte[] pk;

        public SignatureScript read(ByteBuf bf) {
            int len = UintVar.reader(bf).intValueExact();
            signature = ByteUtil.readLength(bf, len);
            len = UintVar.reader(bf).intValueExact();
            pk = ByteUtil.readLength(bf, len);
            return this;
        }

        public SignatureScript write(ByteBuf bf) {
            UintVar.valueOf(signature.length).write(bf);
            bf.writeBytes(signature);
            UintVar.valueOf(pk.length).write(bf);
            bf.writeBytes(pk);
            return this;
        }

        public byte[] getData() {
            ByteBuf buffer = Unpooled.buffer(signature.length + pk.length + UintVar.MAX_LENGTH + UintVar.MAX_LENGTH);
            this.write(buffer);
            return ByteUtil.readAll(buffer);
        }

        public byte[] getSignature() {
            return signature;
        }

        public void setSignature(byte[] signature) {
            this.signature = signature;
        }

        public byte[] getPk() {
            return pk;
        }

        public void setPk(byte[] pk) {
            this.pk = pk;
        }

        @Override
        public String toString() {
            return "SignatureScript {" +
                    "signature=" + ByteUtil.hex(signature) +
                    ", pk=" + ByteUtil.hex(pk) +
                    '}';
        }
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