package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.math.Uint256;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.Optional;

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
    private final IndexBlock levelDB;
    private final IndexHeight heights;
    private final AccessBlockFile fileAccess;
    private BlockCache cache = new BlockCache();

    public DiskBlock(CChainParams chainParams) {
        this.chainParams = chainParams;
        this.bestConfirmHeight = this.chainParams.settings.getBestConfirmHeight();
        try {
            File file = chainParams.settings.lockupRootDirectory();
            logger.info("Data-dir: {}", file.getCanonicalPath());
            root = new File(file, "blocks").getCanonicalFile();
            levelDB = new IndexBlock(chainParams);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ChainBlock genesisBlock = chainParams.env.createGenesisBlock();
        heights = new IndexHeight(levelDB, genesisBlock);
        this.fileAccess = new AccessBlockFile(root, chainParams.getEnvParams().getMagic());
    }

    /**
     * 第一个 prehash 为 0000...0000 的是根
     *
     * @throws IOException
     */
    public synchronized void reindex() throws IOException {
        ByteBuf bf = Unpooled.buffer(1024 * 1024);
        File[] files = this.fileAccess.listFile();
        long time = 0;
        for (File file : files) {
            long length = file.length();
            FileChannel channel = new RandomAccessFile(file, "r").getChannel();
            channel.position(0);
            while (channel.position() < length) {
                FileChainBlock fc = new FileChainBlock(file).setPosition(channel.position())
                        .readBlock(bf, channel);
                int magic = chainParams.getEnvParams().getMagic();
                Assert.isTrue(fc.getMagic() == chainParams.getEnvParams().getMagic(), "Env is not match , need : " + magic);
                Uint256 preHash = fc.getBlock().header.getPreHash();
                Uint256 hash = fc.getBlock().hash();
                int height = heights.getHeight(preHash);
                if (height < 0) {
                    Optional<HeightBlock> opt = levelDB.findChainBlockInLevelDB(preHash);
                    if (opt.isPresent()) {
                        height = opt.get().getHeight();
                    } else {
                        int exist = heights.getHeight(hash);// gen 0
                        if (exist < 0) {
                            Assert.isTrue(height >= 0, "prehash must exist");
                        } else {
                            height = exist - 1;
                        }
                    }
                }
                this.indexBlock(fc, height + 1);
                long next = System.currentTimeMillis();
                if (next - time > 5000) {
                    logger.info("Re-index Height: {}, Hash: {}, PreHash: {}", height + 1, hash.toHexReverse256(), preHash.toHexReverse256());
                    time = next;
                }
            }
        }
    }

    public synchronized DiskBlock init() {
        logger.info("Init DiskBlock, Get block-file, And init chain-height");

        ChainBlock generate = heights.getGenerate();
        logger.info("Generate block hash : {}", generate.hash().toHexReverse256());

        if (chainParams.settings.isReIndex()) {
            try {
                logger.info("Reindex BLOCK, long time");
                reindex();
                logger.info("Reindex BLOCK OVER : {}", heights.getLastBlock());
                return this;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Height opt = heights.getLastBlock().get();
        if (opt.getHeight() <= 0) { // 如果没有最新的块, 需要初始化创世块
            this.writeBlock(generate, 0, true);
        }
        logger.info("Init block-chain TO : {}, {}", heights.getLastBlock());
        return this;
    }

    public Optional<HeightBlock> findChainBlockInLevelDB(Uint256 hash) {
        return levelDB.findChainBlockInLevelDB(hash);
    }

    public DiskBlock putChainBlockToLevelDB(HeightBlock hc) {
        levelDB.putChainBlockToLevelDB(hc);
        return this;
    }

    public Optional<Uint256> getHash(int height) {// TODO : block file !
        return heights.get(height);
    }

    public ChainBlock getLatestBlock() {
        int height = heights.getLastBlock().get().getHeight();
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
            Optional<HeightBlock> fd = findChainBlockInLevelDB(hash);
            if (fd.isPresent()) {
                return fd;
            }
        }
        FileChainBlock write = write(block);
        HeightBlock hb = this.indexBlock(write, height);
        cache.put(hash, hb);
        return Optional.of(hb);
    }

    private synchronized FileChainBlock write(ChainBlock block) {
        try {
            FileChainBlock fc = fileAccess.writeBlock(block);
            fc.setBlock(block);
            fc.setMagic(chainParams.getEnvParams().getMagic());
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
        levelDB.putChainBlockToLevelDB(hc);
        this.resetHeight(hc);
        return hc;
    }


    public void resetHeight(HeightBlock hc) {
        int height = hc.getHeight();
        if (height > this.getLatestHeight()) {
            this.resetLatest(hc);
        }
    }

    public Optional<HeightBlock> readBlock(Uint256 hash) {
        try {
            return cache.get(hash, () -> {
                return tryReadBlock(hash);
            });
        } catch (Exception e) {
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

    /**
     * 从 0 开始
     *
     * @return
     */
    public int getLatestHeight() {
        return heights.getLastBlock().get().getHeight();
    }

    /**
     * @param hash
     * @return 找不到返回 -1
     */
    public int getHeight(Uint256 hash) {
        return heights.getHeight(hash);
    }

    @Override
    public void close() throws IOException {
        try {
            levelDB.close();
        } finally {
            fileAccess.close();
        }
    }

    public synchronized boolean resetLatest(HeightBlock hb) {
        int height = hb.getHeight();
        ChainBlock block = hb.getBlock();
        Height ht = heights.getLatest();
        int latest = ht.getHeight();
        if (latest >= height) {
            return false;
        } else if (latest + 1 == height) {// 大部分情况走这里
            int i = heights.tryPush(hb.getBlock());
            if (i >= 0) {
                return true;
            }
        }
        // TODO:: 需要截断, 然后新增, 例如: 出现分叉的时候的回滚, 也许数据量会很大, 不在内存中计算, 可能是个长时间的任务
        ChainBlock next = block;
        for (int i = height - 1; i > 0; i--) {
            Uint256 pre = next.header.getPreHash();
            Optional<Uint256> r = heights.get(i);
            Optional<HeightBlock> ph = this.findChainBlockInLevelDB(pre);
            Assert.isTrue(ph.isPresent(), "Must exist in DB : " + pre);
            Assert.isTrue(ph.get().getHeight() == i, "PreHash Must height - 1 : " + i);
            if (r.isPresent()) {
                Assert.isTrue(i == ph.get().getHeight(), "Search Height != LevelDB height");
                break;
            }
            if (logger.isDebugEnabled()) { // TODO :: 出错了, 仍然可以运行在新的高度
                heights.push(hb.getBlock().hash(), hb.getHeight());
            }
            heights.setHeight(pre, i);
            next = ph.get().getBlock();
        }
        heights.push(hb.getBlock().hash(), hb.getHeight());
        return true;
    }

    public File getRoot() {
        return root;
    }
}
