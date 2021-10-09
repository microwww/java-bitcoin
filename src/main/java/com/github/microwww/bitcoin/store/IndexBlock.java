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

    private final File root;
    private final DB levelDB;
    private BlockCache cache = new BlockCache();

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

    public IndexBlock writeChainBlockToLevelDB(HeightBlock hc) {
        byte[] key = ByteUtil.concat(LevelDBPrefix.DB_BLOCK_INDEX.prefixBytes, hc.getBlock().hash().fill256bit());
        if (logger.isDebugEnabled())
            logger.debug("Index Block key: {}, height: {}, block: {}", ByteUtil.hex(key), hc.height, hc.getBlock().hash());
        levelDB.put(key, this.serializationLevelDB(hc));
        return this;
    }

    public Optional<ChainBlock> findChainBlock(Uint256 hash) {
        return findChainBlockInLevelDB(hash).map(HeightBlock::loadBlock);
    }

    public Optional<HeightBlock> findChainBlockInLevelDB(Uint256 hash) {
        return cache.get(hash, () -> {
            byte[] key = ByteUtil.concat(LevelDBPrefix.DB_BLOCK_INDEX.prefixBytes, hash.fill256bit());
            return this.findChainBlockInLevelDB(key);
        });
    }

    private synchronized Optional<HeightBlock> findChainBlockInLevelDB(byte[] key) {
        byte[] bytes = levelDB.get(key);
        if (bytes != null) {
            HeightBlock hb = this.deserializationLevelDB(bytes);
            hb.fileChainBlock.load();
            return Optional.of(hb);
        }
        return Optional.empty();
    }

    private HeightBlock deserializationLevelDB(byte[] data) {
        ByteBuf pool = Unpooled.copiedBuffer(data);
        int height = pool.readIntLE();
        int position = pool.readIntLE();
        int len = pool.readByte();
        byte[] name = ByteUtil.readLength(pool, len);
        File file = new File(root, new String(name, StandardCharsets.UTF_8));
        FileChainBlock f = new FileChainBlock(file, position);
        HeightBlock h = new HeightBlock(f, height);
        return h;
    }

    // height + position + <name-len> + name
    private byte[] serializationLevelDB(HeightBlock block) {
        ByteBuf pool = Unpooled.buffer(32); // 4 + 4 + 1 + 12
        byte[] name = block.fileChainBlock.getFile().getName().getBytes(StandardCharsets.UTF_8);
        pool.clear().writeIntLE(block.height).writeIntLE((int) block.fileChainBlock.getPosition()).writeByte(name.length).writeBytes(name);
        return ByteUtil.readAll(pool);
    }

    DB getLevelDB() {
        return levelDB;
    }

    public void close() throws IOException {
        levelDB.close();
    }

    static class HeightBlock {
        public final FileChainBlock fileChainBlock;
        public final int height;

        public HeightBlock(FileChainBlock block, int height) {
            this.fileChainBlock = block;
            this.height = height;
        }

        public ChainBlock getBlock() {
            return fileChainBlock.target();
        }

        public ChainBlock loadBlock() {
            fileChainBlock.load();
            fileChainBlock.target().header.setHeight(this.height);
            return this.getBlock();
        }

        public FileChainBlock getFileChainBlock() {
            return fileChainBlock;
        }

        public int getHeight() {
            return height;
        }
    }
}
