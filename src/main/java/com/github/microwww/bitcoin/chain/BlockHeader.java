package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.Serializable;

/**
 * 头部 80 个字节,
 * 读写方法 使用 CBlock 的读写, 该对象会被 初始化 到 CBlock 中, 会导致很难知道数据流是否已经读写了,
 * 所以read / write 方法放到CBlock中, 防止歧义的发生
 */
public class BlockHeader implements Serializable {
    private static final int HEADER_LENGTH = 80;
    private Uint32 blockLength;

    // 下面属性 hash
    private int version;
    private Uint256 preHash;
    private Uint256 merkleRoot;
    private Uint32 time;
    private Uint32 bits;
    private Uint32 nonce;
    // 上面属性 hash

    public BlockHeader() {
    }

    /**
     * 在 CBlock中使用
     * @param bf
     */
    protected void read(ByteBuf bf) { // 80 字节
        version = bf.readIntLE();
        preHash = Uint256.read(bf);
        merkleRoot = Uint256.read(bf);
        time = new Uint32(bf.readIntLE());
        bits = new Uint32(bf.readIntLE());
        nonce = new Uint32(bf.readIntLE());
    }

    /**
     * 在 CBlock中使用
     * @return
     */
    protected Uint256 hash() {
        ByteBuf bf = Unpooled.buffer(HEADER_LENGTH);
        this.writer(bf);
        return new Uint256(ByteUtil.sha256sha256(ByteUtil.readAll(bf)));
    }

    /**
     * 在 CBlock中使用
     * @param bf
     * @return
     */
    protected BlockHeader writer(ByteBuf bf) {
        bf.writeIntLE(this.version);
        bf.writeBytes(preHash.fill256bit());
        bf.writeBytes(merkleRoot.fill256bit());
        bf.writeIntLE(time.intValue());
        bf.writeIntLE(bits.intValue());
        bf.writeIntLE(nonce.intValue());
        return this;
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

    @Override
    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    public StringBuilder toString(StringBuilder bf) {
        bf.append("Block {")
                .append("  hash=").append(hash())
                .append(", version=").append(version)
                .append(", preHash=").append(preHash)
                .append(", merkleRoot=").append(merkleRoot)
                .append(", time=").append(time)
                .append(", bits=").append(bits)
                .append(", nonce=").append(nonce);
        return bf.append('}');
    }
}
