package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.util.ByteUtil;
import org.iq80.leveldb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Optional;

public class IndexHeight {
    private static final Logger logger = LoggerFactory.getLogger(IndexHeight.class);
    private final ChainBlock generate;
    private final IndexBlock indexBlock;
    private final DB levelDB;

    public IndexHeight(IndexBlock indexBlock, ChainBlock generate) {
        this.generate = generate;
        this.indexBlock = indexBlock;
        this.levelDB = indexBlock.getLevelDB();
        init();
    }

    private void init() {
        Uint256 hash = generate.hash();
        Height latest = this.getLastHeight();
        if (latest == null) {
            this.setHeight(hash, 0);
            this.setLastBlock(hash, 0);
        }
    }

    private synchronized void setLastBlock(Uint256 hash, int height) {
        this.setLastBlock(new Height(hash, height));
    }

    private synchronized void setLastBlock(Height height) {
        levelDB.put(LevelDBPrefix.DB_LAST_BLOCK.prefixBytes, height.serialization());
        logger.debug("Add LAST_BLOCK: {}", height);
    }

    public synchronized Height getLastHeight() {
        byte[] bytes = levelDB.get(LevelDBPrefix.DB_LAST_BLOCK.prefixBytes);
        if (bytes != null) {
            return Height.deserialization(bytes);
        }
        return null;
    }

    public HeightBlock getLastBlock() {
        Height lastHeight = this.getLastHeight();
        return indexBlock.findChainBlockInLevelDB(lastHeight.getHash()).get();
    }

    public synchronized int tryPush(ChainBlock block) {
        Uint256 preHash = block.header.getPreHash();
        Height last = this.getLastHeight();
        if (last.getHash().equals(preHash)) {
            int h = last.getHeight() + 1;
            this.putResetLatest(block.hash(), h);
            return h;
        }
        return -1;
    }

    public synchronized void push(Uint256 hash, int height) {
        this.putResetLatest(hash, height);
    }

    private synchronized void putResetLatest(Uint256 hash, int height) {
        int h = this.getLastHeight().getHeight();
        Assert.isTrue(h + 1 == height, "Only add to HEADER : " + h);
        setHeight(hash, height);
        this.setLastBlock(hash, height);
    }

    /**
     * 直接 set 一个高度值
     *
     * @param hash
     * @param height
     */
    public void setHeight(Uint256 hash, int height) {
        byte[] key = ByteUtil.concat(new byte[]{LevelDBPrefix.DB_HEAD_BLOCKS.prefixByte}, new Uint32(height).toBytes());
        if (logger.isDebugEnabled())
            logger.debug("Index height: {}, {}, key: {}", height, hash, ByteUtil.hex(key));
        levelDB.put(key, hash.toByteArray());
    }

    public synchronized Optional<Uint256> get(int height) {
        int max = this.getLastHeight().getHeight();
        if (height <= max) {
            byte[] key = ByteUtil.concat(new byte[]{LevelDBPrefix.DB_HEAD_BLOCKS.prefixByte}, new Uint32(height).toBytes());
            byte[] bytes = levelDB.get(key);
            if (bytes != null) {
                return Optional.of(new Uint256(bytes));
            }
        }
        return Optional.empty();
    }

    public synchronized int getHeight(Uint256 hash) {
        return indexBlock.findChainBlockInLevelDB(hash).map(HeightBlock::getHeight).orElse(-1);
    }

    public ChainBlock getGenerate() {
        return generate;
    }

    public void removeTail(int count) {
        int height = this.getLastHeight().getHeight() - count;
        this.setLastBlock(new Height(this.get(height).get(), height));
    }
}