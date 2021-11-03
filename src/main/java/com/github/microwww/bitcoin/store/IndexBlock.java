package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.conf.ChainBlockStore;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.iq80.leveldb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class IndexBlock implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(IndexBlock.class);
    public static int MAX_CACHE = 2 * 24 * 6; // 2 天的块

    private final CChainParams chainParams;
    private final File root;
    private final DB levelDB;
    private BlockCache<Uint256, FileChainBlock> cache = new BlockCache<>(MAX_CACHE);

    public IndexBlock(CChainParams chainParams) {
        this.chainParams = chainParams;
        try {
            root = chainParams.settings.getBlocksDirectory();
            File index = chainParams.settings.getBlocksIndexDirectory();
            logger.info("Block index directory: {}", root.getCanonicalPath());
            levelDB = ChainBlockStore.leveldb(index, chainParams.settings.isReIndex());
            init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void init() {
        ChainBlock g = chainParams.env.G;
        Optional<Uint256> height = this.get(0);
        if (height.isPresent()) {
            Assert.isTrue(height.get().equals(g.hash()), "Index 0 hash != ENV.GenesisBlock");
        } else {
            this.setHeight(g.hash(), 0);
            this.setLastBlock(g.hash(), 0);
        }
    }

    public IndexBlock writeChainBlockToLevelDB(FileChainBlock hc) {
        ChainBlock block = hc.getTarget();
        byte[] key = ByteUtil.concat(LevelDBPrefix.DB_BLOCK_INDEX.prefixBytes, block.hash().fill256bit());
        if (logger.isDebugEnabled())
            logger.debug("Index Block key: {}, height: {}, block: {}", ByteUtil.hex(key), block.getHeight(), block.hash());
        levelDB.put(key, this.serializationLevelDB(hc));
        return this;
    }

    public Optional<ChainBlock> findChainBlock(Uint256 hash) {
        return findChainBlockInLevelDB(hash).map(FileChainBlock::getTarget);
    }

    public Optional<FileChainBlock> findChainBlockInLevelDB(Uint256 hash) {
        return cache.get(hash, () -> {
            byte[] key = ByteUtil.concat(LevelDBPrefix.DB_BLOCK_INDEX.prefixBytes, hash.fill256bit());
            return this.findChainBlockInLevelDB(key);
        });
    }

    public synchronized Optional<FileChainBlock> findChainBlockInLevelDB(byte[] key) {
        byte[] bytes = levelDB.get(key);
        if (bytes != null) {
            return Optional.of(FileChainBlock.deserialization(root, bytes));
        }
        return Optional.empty();
    }

    // height + position + <name-len> + name
    private byte[] serializationLevelDB(FileChainBlock block) {
        ByteBuf pool = Unpooled.buffer(32); // 4 + 4 + 1 + 12
        byte[] name = block.getFile().getName().getBytes(StandardCharsets.UTF_8);
        pool.clear().writeIntLE(block.getTarget().getHeight()).writeIntLE((int) block.getPosition()).writeByte(name.length).writeBytes(name);
        return ByteUtil.readAll(pool);
    }

    //////----------------
    public void setLastBlock(Uint256 hash, int height) {
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

    public ChainBlock getLastBlock() {
        Height lastHeight = this.getLastHeight();
        return this.findChainBlock(lastHeight.getHash()).get();
    }

    //////----------------
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
        Height last = this.getLastHeight();
        if (last != null) {
            int max = last.getHeight();
            if (height <= max) {
                byte[] key = ByteUtil.concat(new byte[]{LevelDBPrefix.DB_HEAD_BLOCKS.prefixByte}, new Uint32(height).toBytes());
                byte[] bytes = levelDB.get(key);
                if (bytes != null) {
                    return Optional.of(new Uint256(bytes));
                }
            }
        }
        return Optional.empty();
    }

    public synchronized int getHeight(Uint256 hash) {
        return this.findChainBlockInLevelDB(hash).map(e -> e.getTarget().getHeight()).orElse(-1);
    }

    public synchronized void removeTail(int count) {
        int height = this.getLastHeight().getHeight() - count;
        this.setLastBlock(new Height(this.get(height).get(), height));
    }
    //////----------------

    @Override
    public void close() throws IOException {
        levelDB.close();
    }
}
