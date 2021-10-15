package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.conf.ChainBlockStore;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.iq80.leveldb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class IndexBlock implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(IndexBlock.class);
    public static int MAX_CACHE = 2 * 24 * 6; // 2 天的块

    private final File root;
    private final DB levelDB;
    private BlockCache<Uint256, FileChainBlock> cache = new BlockCache<>(MAX_CACHE);

    public IndexBlock(CChainParams chainParams) {
        try {
            File file = chainParams.settings.lockupRootDirectory();
            root = new File(file, "blocks").getCanonicalFile();
            levelDB = ChainBlockStore.leveldb(root, "index", chainParams.settings.isReIndex());
            logger.info("Data-dir: {}", new File(root, "index").getCanonicalPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    DB getLevelDB() {
        return levelDB;
    }

    public void close() throws IOException {
        levelDB.close();
    }
}
