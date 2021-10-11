package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.math.UintVar;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.OptionalInt;
import java.util.concurrent.TimeUnit;

/**
 * 头部 80 个字节,
 * 读写方法 使用 CBlock 的读写, 该对象会被 初始化 到 CBlock 中, 会导致很难知道数据流是否已经读写了,
 * 所以read / write 方法放到CBlock中, 防止歧义的发生
 */
public class BlockHeader implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(BlockHeader.class);
    private static final int HEADER_LENGTH = 80;

    // 下面属性 hash
    private int version;
    private Uint256 preHash;
    private Uint256 merkleRoot;
    private Uint32 time;
    private Uint32 bits; // POW 难度 // CalculateNextWorkRequired
    private Uint32 nonce;
    // 上面属性 hash

    private UintVar txCount = UintVar.ZERO; // 这只是个标记位, 协议传输和解析的时候需要, 真实使用 txs.length 即可
    private OptionalInt height = OptionalInt.empty();

    public BlockHeader() {
    }

    /**
     * 在 CBlock中使用, 读取时候会读取交易数量, 写入的时候不会写入交易数量
     *
     * @param bf
     */
    protected void read(ByteBuf bf) { // 80 + 1 字节
        version = bf.readIntLE();
        preHash = Uint256.read(bf);
        merkleRoot = Uint256.read(bf);
        time = new Uint32(bf.readIntLE());
        bits = new Uint32(bf.readIntLE());
        nonce = new Uint32(bf.readIntLE());
        txCount = UintVar.parse(bf);
    }

    public int bytesLength() {
        return HEADER_LENGTH + txCount.bytesLength();
    }

    public Uint256 txid() {
        return hash();
    }

    /**
     * 在 CBlock中使用, TODO :: 建议缓存
     *
     * @return
     */
    public Uint256 hash() {
        ByteBuf bf = Unpooled.buffer(HEADER_LENGTH);
        this.writer(bf);
        return new Uint256(ByteUtil.sha256sha256(ByteUtil.readAll(bf)));
    }

    /**
     * 在 CBlock中使用, 读取时候会读取交易数量, 写入的时候不会写入交易数量, 需要根据 block 的 txs.length 数据写入
     *
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

    public int getVersion() {
        return version;
    }

    public BlockHeader setVersion(int version) {
        this.version = version;
        return this;
    }

    public Uint256 getPreHash() {
        return preHash;
    }

    public BlockHeader setPreHash(Uint256 preHash) {
        this.preHash = preHash;
        return this;
    }

    public Uint256 getMerkleRoot() {
        return merkleRoot;
    }

    public BlockHeader setMerkleRoot(Uint256 merkleRoot) {
        this.merkleRoot = merkleRoot;
        return this;
    }

    public Uint32 getTime() {
        return time;
    }

    public Date getDateTime() {
        return new Date(this.time.longValue() * 1_000);
    }

    public BlockHeader setTime(Uint32 time) {
        this.time = time;
        return this;
    }

    public Uint32 getBits() {
        return bits;
    }

    public BigInteger threshold() {
        return PowDifficulty.difficultyUncompress(this.bits);
    }

    public Uint256 difficulty() {
        return new Uint256(PowDifficulty.difficultyUncompress(this.bits));
    }

    public void assertDifficulty() {
        BigInteger dif = threshold();
        Uint256 hash = this.hash();
        BigInteger act = new BigInteger(1, hash.reverse256bit());
        if (logger.isDebugEnabled()) {
            logger.debug("POW Difficulty {}, Target: {}, Active: {}", this.getBits(), dif.toString(16), act.toString(16));
        }
        if (dif.compareTo(act) < 0) {
            logger.error("POW Difficulty error: {}, Target: {}", hash, dif.toString(16));
            throw new IllegalArgumentException("POW Difficulty ERROR, hash: " + hash);
        }
    }

    public BlockHeader setBits(Uint32 bits) {
        this.bits = bits;
        return this;
    }

    public Uint32 getNonce() {
        return nonce;
    }

    public BlockHeader setNonce(Uint32 nonce) {
        this.nonce = nonce;
        return this;
    }

    @Override
    public String toString() {
        return toString(new StringBuilder(), "").toString();
    }

    public UintVar getTxCount() {
        return txCount;
    }

    public void setTxCount(int count) {
        txCount = UintVar.valueOf(count);
    }

    public void setTxCount(UintVar count) {
        txCount = count;
    }

    public OptionalInt getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = OptionalInt.of(height);
    }

    public boolean isInTwoHours() {
        long twoHours = TimeUnit.HOURS.toSeconds(2);
        return this.getTime().longValue() >= System.currentTimeMillis() / 1000 - twoHours;
    }

    public StringBuilder toString(StringBuilder bf, String prefix) {
        return bf
                .append(prefix).append(" hash   = ").append(hash())
                .append(prefix).append(" height = ").append(height.orElse(-1))
                .append(prefix).append(" version= ").append(version)
                .append(prefix).append(" preHash= ").append(preHash)
                .append(prefix).append(" merkle = ").append(merkleRoot)
                .append(prefix).append(" time   = ").append(time)
                .append(prefix).append(" bits   = ").append(bits)
                .append(prefix).append(" nonce  = ").append(nonce)
                .append(prefix).append("    _tx = ").append(txCount);
    }
}
