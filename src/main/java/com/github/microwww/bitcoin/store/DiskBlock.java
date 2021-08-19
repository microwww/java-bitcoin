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
import org.springframework.util.Assert;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;

/**
 * struct CDiskTxPos : public FlatFilePos
 * init.cpp | CleanupBlockRevFiles
 * validation.h  Open a block file (blk?????.dat) FILE* OpenBlockFile(const FlatFilePos &pos, bool fReadOnly = false);
 * bicoin-core `WriteBlockToDisk`
 **/
public class DiskBlock implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(DiskBlock.class);

    private final int bestConfirmHeight;
    private final CChainParams chainParams;
    private final File root;
    private final DB levelDB;
    private final MemBlockHeight heights;
    private final AccessBlockFile fileAccess;

    public DiskBlock(CChainParams chainParams) {
        this.chainParams = chainParams;
        this.bestConfirmHeight = this.chainParams.settings.getBestConfirmHeight();
        try {
            File file = chainParams.settings.lockupRootDirectory();
            logger.info("Data-dir: {}", file.getCanonicalPath());
            root = new File(file, "blocks").getCanonicalFile();
            levelDB = ChainBlockStore.leveldb(root, "index", chainParams.settings.isReIndex());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ChainBlock genesisBlock = chainParams.env.createGenesisBlock();
        heights = new MemBlockHeight(genesisBlock);
        this.fileAccess = new AccessBlockFile(root);
    }

    /**
     * 第一个 prehash 为 0000...0000 的是根
     *
     * @throws IOException
     */
    public synchronized void reindex() throws IOException {
        ByteBuf bf = Unpooled.buffer(10 * 1024 * 1024);
        File[] files = this.fileAccess.listFile();
        HashMap<Uint256, List<FileChainBlock>> pool = new HashMap<>(); // prehash, block
        for (File file : files) {
            long length = file.length();
            FileChannel channel = new RandomAccessFile(file, "r").getChannel();
            channel.position(0);
            while (channel.position() <= length) {
                FileChainBlock fc = new FileChainBlock(file).setPosition(channel.position()).readBlock(bf, channel);
                Uint256 preHash = fc.getBlock().header.getPreHash();
                int height = heights.get(preHash);
                if (height < 0) {
                    Optional<HeightBlock> opt = this.findChainBlockInLevelDB(preHash.fill256bit());
                    if (opt.isPresent()) {
                        height = opt.get().getHeight();
                    }
                }
                if (height < 0) {
                    List<FileChainBlock> list = pool.get(preHash); //, fc.getBlock());
                    if (list == null) {
                        list = new ArrayList<>();
                        pool.put(preHash, list);
                    }
                    list.add(fc);
                    if (preHash.equalsByte(new byte[32])) {
                        heights.get(0)
                                .filter(e -> e.equals(fc.getBlock().hash()))
                                .orElseThrow(() -> new RuntimeException("prehash == 000...000 is Must Generate"));
                    } else continue;
                }
                this.indexBlock(fc, height + 1);
                Uint256 hash = fc.getBlock().hash();
                logger.info("Height: {}, Hash: {}, PreHash: {}", height + 1, hash.toHexReverse256(), preHash.toHexReverse256());
                refreshCache(bf, pool, hash, height + 2);
            }
        }
    }

    public void refreshCache(ByteBuf bf, HashMap<Uint256, List<FileChainBlock>> pool, Uint256 hash, int height) {
        List<FileChainBlock> list = pool.get(hash);
        if (list == null || list.isEmpty()) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            FileChainBlock cb = list.get(i);
            this.indexBlock(cb, height);
            refreshCache(bf, pool, cb.getBlock().hash(), height + 1);
        }
    }

    public synchronized DiskBlock init() {
        logger.info("Init DiskBlock, Get block-file, And init chain-height");

        if (chainParams.settings.isReIndex()) {
            try {
                logger.info("Reindex BLOCK");
                reindex();
                logger.info("Reindex BLOCK OVER : " + heights.getLatestHeight());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        ChainBlock generate = heights.getGenerate();
        logger.info("Generate block hash : {}", generate.hash().toHexReverse256());
        Optional<HeightBlock> opt = this.findChainBlockInLevelDB(LevelDBPrefix.DB_LAST_BLOCK.prefixBytes);
        if (!opt.isPresent()) { // 如果没有最新的块, 需要初始化创世块
            Optional<HeightBlock> hc = this.writeBlock(generate, 0, true);
            byte[] array = hc.get().serializationLevelDB();
            levelDB.put(LevelDBPrefix.DB_LAST_BLOCK.prefixBytes, array);
        } else {
            LinkedList<Uint256> list = new LinkedList<>();
            HeightBlock ds = opt.get();
            ChainBlock latest = ds.getFileChainBlock().loadBlock().getBlock();
            int h = ds.getHeight();
            logger.debug("Loading latest block in levelDB : {}, {}", h, latest.hash());
            while (true) {
                list.add(latest.hash());
                Uint256 next = latest.header.getPreHash();
                Optional<HeightBlock> nv = this.readBlock(next);
                if (nv.isPresent()) {
                    HeightBlock hc = nv.get();
                    latest = hc.getBlock();
                    logger.debug("Init from levelDB: {}, {} --> {}", latest.hash(), latest.header.getPreHash());
                    h--;
                    Assert.isTrue(hc.getHeight() == h, "levelDB height not match");
                } else break;
            }

            Uint256 last = list.pollLast();
            Assert.isTrue(last.equals(generate.hash()), "Must equal GenesisBlock");
            Assert.isTrue(h == 0, "All over is zero");
            Assert.isTrue(ds.getHeight() == list.size(), "Height is not match");

            // int best = chainParams.settings.getBestConfirmHeight();
            for (int i = 1; ; i++) { // 跳过了创世块 从1开始
                Uint256 uint256 = list.pollLast();
                if (uint256 == null) break;
                heights.hashAdd(uint256, i);
            }
            Optional<Uint256> uint256 = heights.get(heights.getLatestHeight());
            Optional<HeightBlock> heightChainBlock = this.readBlock(uint256.get());
            this.putChainBlockToLevelDB(heightChainBlock.get());
        }
        logger.info("Init block-chain TO : {}, {}", heights.getLatestHeight(), heights.getLatestHash());
        return this;
    }

    public Optional<HeightBlock> findChainBlockInLevelDB(Uint256 hash) {
        byte[] key = ByteUtil.concat(LevelDBPrefix.DB_BLOCK_INDEX.prefixBytes, hash.fill256bit());
        return this.findChainBlockInLevelDB(key);
    }

    public Optional<HeightBlock> findChainBlockInLevelDB(byte[] key) {
        byte[] bytes = levelDB.get(key);
        if (bytes != null) {
            HeightBlock hb = HeightBlock.deserializationLevelDB(root, bytes);
            return Optional.of(hb);
        }
        return Optional.empty();
    }

    public DiskBlock putChainBlockToLevelDB(HeightBlock hc) {
        byte[] key = ByteUtil.concat(LevelDBPrefix.DB_BLOCK_INDEX.prefixBytes, hc.getBlock().hash().fill256bit());
        levelDB.put(key, hc.serializationLevelDB());
        return this;
    }

    public Optional<Uint256> getHash(int height) {// TODO : block file !
        return heights.get(height);
    }

    public ChainBlock getLatestBlock() {
        int height = heights.getLatestHeight();
        if (height > bestConfirmHeight) {
            height -= bestConfirmHeight;
        } else {
            height = bestConfirmHeight;
        }
        Uint256 uint256 = this.getHash(height).get();
        final int h = height;
        return this.getChinBlock(uint256).orElseThrow(() -> {
            return new IllegalStateException("Not find height Block : " + h);
        });
    }

    public Optional<ChainBlock> getChinBlock(Uint256 hash) {
        return this.readBlock(hash).map(h -> h.getBlock());
    }

    public Optional<HeightBlock> writeBlock(ChainBlock block, boolean ifExistSkip) {
        Uint256 pre = block.header.getPreHash();
        Optional<HeightBlock> hc = this.readBlock(pre);
        if (hc.isPresent()) {
            int h = hc.get().getHeight() + 1;
            return writeBlock(block, h, ifExistSkip);
        }
        return Optional.empty();
    }

    /**
     * 只能顺序写
     *
     * @return
     */
    public Optional<HeightBlock> writeBlock(ChainBlock block, int height, boolean ifExistSkip) {
        Uint256 hash = block.hash();
        if (ifExistSkip) {
            if (findChainBlockInLevelDB(hash).isPresent()) {
                return Optional.empty();
            }
        }
        FileChainBlock write = write(block);
        HeightBlock hb = this.indexBlock(write, height);
        return Optional.of(hb);
    }

    private synchronized FileChainBlock write(ChainBlock block) {
        try {
            FileChannel ch = fileAccess.channel();
            File file = fileAccess.getFile();
            fileAccess.getFileChannel();
            FileChainBlock fc = new FileChainBlock(file);
            fc.setBlock(block);
            fc.setMagic(chainParams.getEnvParams().getMagic());
            fc.writeBlock(Unpooled.buffer(), ch);
            return fc;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HeightBlock indexBlock(FileChainBlock write, int height) {
        ChainBlock block = write.getBlock();
        if (logger.isDebugEnabled())
            logger.debug("Add BLOCK to levelDB: {}, {} , {} , {}", write.getPosition(), height, block.hash(), block.header.getPreHash());
        Assert.isTrue(write.getPosition() < Integer.MAX_VALUE, "Int overflow");
        HeightBlock hc = new HeightBlock(write, height);
        this.writeLevelDB(hc);
        this.resetHeight(hc);
        return hc;
    }

    // ByteBuf : height + position + len + name
    public DiskBlock writeLevelDB(HeightBlock hc) {
        Uint256 hash = hc.getBlock().hash();
        levelDB.put(
                ByteUtil.concat(LevelDBPrefix.DB_BLOCK_INDEX.prefixBytes, hash.fill256bit()),
                hc.serializationLevelDB()
        );
        return this;
    }

    public void resetHeight(HeightBlock hc) {
        int height = hc.getHeight();
        if (height > this.getLatestHeight()) {
            this.resetLatest(hc);
        }
    }

    public Optional<HeightBlock> readBlock(Uint256 hash) {
        try {
            return tryReadBlock(hash);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized Optional<HeightBlock> tryReadBlock(Uint256 hash) throws IOException {
        Optional<HeightBlock> data = this.findChainBlockInLevelDB(hash);
        if (!data.isPresent()) {
            return Optional.empty();
        }
        logger.debug("Get block from levelDB: {}", hash.toHexReverse256());

        //
        HeightBlock hb = data.get();
        FileChainBlock fc = hb.getFileChainBlock();
        File f = fc.getFile();
        logger.debug("Load block height: {}, in {}, position: {}, length: {}", hb.getHeight(), fc.getFile().getName(), fc.getPosition());
        //

        FileChannel r = new RandomAccessFile(f, "r").getChannel();
        r.position(fc.getPosition());
        fc.readBlock(Unpooled.buffer(), r);
        int magic = fc.getMagic();
        Assert.isTrue(chainParams.getEnvParams().getMagic() == magic, "magic not match !");// 校验头
        return data;
    }

    private void levelDBPut(byte[] k1, byte[] k2, byte[] v1, byte[] v2) {
        levelDB.put(ByteUtil.concat(k1, k2), ByteUtil.concat(v1, v2));
    }

    /**
     * 从 0 开始
     *
     * @return
     */
    public int getLatestHeight() {
        return heights.getLatestHeight();
    }

    /**
     * @param hash
     * @return 找不到返回 -1
     */
    public int getHeight(Uint256 hash) {
        return heights.get(hash);
    }

    public DB getLevelDB() {
        return levelDB;
    }

    @Override
    public void close() throws IOException {
        try {
            levelDB.close();
        } finally {
            fileAccess.channel().close();
        }
    }

    public boolean resetLatest(HeightBlock hb) {
        int height = hb.getHeight();
        ChainBlock block = hb.getBlock();
        int latestHeight = this.getLatestHeight();
        if (this.getLatestHeight() >= height) {
            return false;
        }
        Uint256 hash = block.header.getPreHash();
        LinkedList<Uint256> list = new LinkedList<>();
        list.add(block.hash());
        int target = 0;
        for (int i = 0; i < 100; i++) {
            int h = getHeight(hash);
            if (h >= 0) {
                target = h;
                break;
            } else {
                list.add(hash);
                Optional<HeightBlock> hc = this.readBlock(hash);
                if (hc.isPresent()) {
                    hash = hc.get().getBlock().header.getPreHash();
                }
            }
        }
        heights.removeTail(latestHeight - target);
        int last = heights.getLatestHeight();
        Assert.isTrue(last + list.size() == height, "Add will up-up");
        for (int i = last; ; i++) {
            Uint256 uint256 = list.pollLast();
            if (uint256 == null) break;
            heights.hashAdd(uint256, i + 1);
        }
        levelDB.put(LevelDBPrefix.DB_LAST_BLOCK.prefixBytes, hb.serializationLevelDB());
        return true;
    }

    public File getRoot() {
        return root;
    }
}
