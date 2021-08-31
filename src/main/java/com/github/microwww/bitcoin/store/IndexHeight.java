package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.util.ByteUtil;
import org.iq80.leveldb.DB;
import org.springframework.util.Assert;

import java.util.Optional;

public class IndexHeight {
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
        Optional<Height> latest = this.getLastBlock();
        if (!latest.isPresent()) {
            this.setHeight(hash, 0);
            this.setLastBlock(hash, 0);
        }
    }

    private void setLastBlock(Uint256 hash, int height) {
        this.setLastBlock(new Height(hash, height));
    }

    private void setLastBlock(Height height) {
        levelDB.put(LevelDBPrefix.DB_LAST_BLOCK.prefixBytes, height.serialization());
    }

    public Height getLatest() {
        return this.getLastBlock().get();
    }

    public Optional<Height> getLastBlock() {
        byte[] bytes = levelDB.get(LevelDBPrefix.DB_LAST_BLOCK.prefixBytes);
        if (bytes != null) {
            Height hb = Height.deserialization(bytes);
            return Optional.of(hb);
        }
        return Optional.empty();
    }

    public synchronized int tryPush(ChainBlock block) {
        Uint256 preHash = block.header.getPreHash();
        Height last = this.getLastBlock().get();
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
        int h = this.getLastBlock().get().getHeight();
        Assert.isTrue(h + 1 == height, "Only add to HEADER : " + h);
        setHeight(hash, height);
        this.setLastBlock(hash, height);
    }

    /**
     * 直接 set 一个高度值
     * @param hash
     * @param height
     */
    public void setHeight(Uint256 hash, int height) {
        byte[] key = ByteUtil.concat(new byte[]{LevelDBPrefix.DB_HEAD_BLOCKS.prefixByte}, new Uint32(height).toBytes());
        levelDB.put(key, hash.toByteArray());
    }

    public synchronized Optional<Uint256> get(int height) {
        int max = this.getLastBlock().get().getHeight();
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
        int height = this.getLastBlock().get().getHeight() - count;
        this.setLastBlock(new Height(this.get(height).get(), height));
    }
}