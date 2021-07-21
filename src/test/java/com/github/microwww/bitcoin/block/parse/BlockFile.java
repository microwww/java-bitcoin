package com.github.microwww.bitcoin.block.parse;

import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.math.Uint8;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Arrays;

public class BlockFile {

    public static class RawBlock { // 80byte
        public Uint32 blockLength;

        // 下面属性 hash
        public int version;
        public Uint256 preHash;
        public Uint256 merkleRoot;
        public Uint32 time;
        public Uint32 bits;
        public Uint32 nonce;
        // 上面属性 hash

        public Uint8 txCount;
        RawTransaction[] txs;

        public void read(ByteBuf bf) {
            version = bf.readIntLE();
            preHash = Uint256.read(bf);
            merkleRoot = Uint256.read(bf);
            time = new Uint32(bf.readIntLE());
            bits = new Uint32(bf.readIntLE());
            nonce = new Uint32(bf.readIntLE());
            txCount = new Uint8(bf.readByte());
            int len = txCount.intValue();
            txs = new RawTransaction[len];
            for (int i = 0; i < len; i++) {
                RawTransaction tr = new RawTransaction();
                tr.read(bf);
                txs[i] = tr;
            }
        }

        public Uint256 hash() {
            ByteBuf bf = Unpooled.buffer(80);
            bf.writeIntLE(this.version);
            bf.writeBytes(preHash.file256bit());
            bf.writeBytes(merkleRoot.file256bit());
            bf.writeIntLE(time.intValue());
            bf.writeIntLE(bits.intValue());
            bf.writeIntLE(nonce.intValue());
            return new Uint256(ByteUtil.sha256sha256(ByteUtil.readAll(bf)));
        }

        @Override
        public String toString() {
            return "RawBlock{" +
                    "  hash=" + hash() +
                    ", version=" + version +
                    ", preHash=" + preHash +
                    ", merkleRoot=" + merkleRoot +
                    ", time=" + time +
                    ", bits=" + bits +
                    ", nonce=" + nonce +
                    ", txCount=" + txCount +
                    ", txs=" + Arrays.toString(txs) +
                    '}';
        }
    }

    public static class RawTransaction {
        public int version;
        public Uint8 inputCount;
        public TxIn[] txIns;
        public Uint8 outputCount;
        public TxOut[] txOuts;
        public Uint32 lockTime;

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
            return new Uint256(ByteUtil.sha256sha256(ByteUtil.readAll(bf)));
        }

        @Override
        public String toString() {
            return "RawTransaction{" +
                    "  hash=" + hash() +
                    ", version=" + version +
                    ", inputCount=" + inputCount +
                    ", txIns=" + Arrays.toString(txIns) +
                    ", outputCount=" + outputCount +
                    ", txOuts=" + Arrays.toString(txOuts) +
                    ", lockTime=" + lockTime +
                    '}';
        }
    }

    public static class TxIn {
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
            bf.writeBytes(preTxHash.file256bit());
            bf.writeIntLE(preTxOutIndex);
            bf.writeByte(script.length);
            bf.writeBytes(script);
            bf.writeIntLE(sequence.intValue());
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

    public static class TxOut {
        public long value;
        public Uint8 scriptLength;
        public byte[] script;

        public void read(ByteBuf bf) {
            value = bf.readLongLE();
            scriptLength = new Uint8(bf.readByte());
            script = ByteUtil.readLength(bf, scriptLength.intValue());
        }

        public void write(ByteBuf bf) {
            bf.writeLongLE(value);
            //scriptLength = new Uint8(bf.readByte());
            bf.writeByte(script.length);
            bf.writeBytes(script);
        }

        @Override
        public String toString() {
            return "TxOut{" +
                    "value=" + value +
                    ", scriptLength=" + scriptLength +
                    ", script=" + ByteUtil.hex(script) +
                    '}';
        }
    }

}
