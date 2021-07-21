package com.github.microwww.bitcoin.block;

import com.github.microwww.bitcoin.math.MerkleTree;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.math.Uint8;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Arrays;

public class BlockHeader {
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

    /**
     * 静态方法二不是普通方法是为了避免和属性产生冲突, 后续可能为修改该方法的位置
     *
     * @param block
     * @return
     */
    public static Uint256 blockMerkleRoot(BlockHeader block) {
        MerkleTree<RawTransaction, Uint256> tree = MerkleTree.merkleTree(Arrays.asList(block.getTxs()), e -> e.hash(), (e1, e2) -> new Uint256(e1.sha256sha256(e2)));
        return tree.getHash();
    }

    public Uint32 getBlockLength() {
        return blockLength;
    }

    public void setBlockLength(Uint32 blockLength) {
        this.blockLength = blockLength;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Uint256 getPreHash() {
        return preHash;
    }

    public void setPreHash(Uint256 preHash) {
        this.preHash = preHash;
    }

    public Uint256 getMerkleRoot() {
        return merkleRoot;
    }

    public void setMerkleRoot(Uint256 merkleRoot) {
        this.merkleRoot = merkleRoot;
    }

    public Uint32 getTime() {
        return time;
    }

    public void setTime(Uint32 time) {
        this.time = time;
    }

    public Uint32 getBits() {
        return bits;
    }

    public void setBits(Uint32 bits) {
        this.bits = bits;
    }

    public Uint32 getNonce() {
        return nonce;
    }

    public void setNonce(Uint32 nonce) {
        this.nonce = nonce;
    }

    public Uint8 getTxCount() {
        return txCount;
    }

    public void setTxCount(Uint8 txCount) {
        this.txCount = txCount;
    }

    public RawTransaction[] getTxs() {
        return txs;
    }

    public void setTxs(RawTransaction[] txs) {
        this.txs = txs;
    }
}
