package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.chain.ChainHeight;
import com.github.microwww.bitcoin.chain.PowDifficulty;
import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
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
    private final IndexBlock indexBlock;
    private final IndexHeight indexHeight;
    private final AccessBlockFile fileAccess;

    public DiskBlock(CChainParams chainParams) {
        this.chainParams = chainParams;
        this.bestConfirmHeight = this.chainParams.settings.getBestConfirmHeight();
        try {
            File file = chainParams.settings.lockupRootDirectory();
            logger.info("Data-dir: {}", file.getCanonicalPath());
            root = new File(file, "blocks").getCanonicalFile();
            indexBlock = new IndexBlock(chainParams);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ChainBlock genesisBlock = chainParams.env.createGenesisBlock();
        indexHeight = new IndexHeight(indexBlock, genesisBlock);
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
                int height = indexHeight.getHeight(preHash);
                if (height < 0) {
                    Optional<HeightBlock> opt = indexBlock.findChainBlockInLevelDB(preHash);
                    if (opt.isPresent()) {
                        height = opt.get().getHeight();
                    } else if (hash.equals(indexHeight.getGenerate().hash())) {
                        height = -1;
                    } else {
                        Assert.isTrue(height >= 0, "prehash must exist");
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

        ChainBlock generate = indexHeight.getGenerate();
        logger.info("Generate block hash : {}", generate.hash().toHexReverse256());

        if (chainParams.settings.isReIndex()) {
            try {
                logger.info("Reindex BLOCK, long time");
                reindex();
                logger.info("Reindex BLOCK OVER : {}", indexHeight.getLastHeight());
                return this;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Height opt = indexHeight.getLastHeight();
        if (opt.getHeight() <= 0) { // 如果没有最新的块, 需要初始化创世块
            this.writeBlock(generate, 0, true);
        }
        logger.info("Init block-chain TO : {}, {}", indexHeight.getLastHeight());
        return this;
    }

    public Optional<HeightBlock> findChainBlockInLevelDB(Uint256 hash) {
        return indexBlock.findChainBlockInLevelDB(hash);
    }

    public DiskBlock putChainBlockToLevelDB(HeightBlock hc) {
        indexBlock.writeChainBlockToLevelDB(hc);
        return this;
    }

    public Optional<Uint256> getHash(int height) {// TODO : block file !
        return indexHeight.get(height);
    }

    public HeightBlock getLastBlock() {
        int height = indexHeight.getLastHeight().getHeight();
        return this.getHash(height).flatMap(hash -> {
            return this.getChinBlock(hash);
        }).orElseThrow(() -> new IllegalStateException("Not find height Block : " + height));
    }

    public HeightBlock getBestBlock() {
        int height = indexHeight.getLastHeight().getHeight();
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

    public Optional<HeightBlock> getChinBlock(Uint256 hash) {
        return this.readBlock(hash);
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
        indexBlock.writeChainBlockToLevelDB(hc);
        this.resetHeight(hc);
        return hc;
    }


    public void resetHeight(HeightBlock hc) {
        int height = hc.getHeight();
        if (height > this.getLatestHeight()) {
            this.resetLatest(hc);
        }
    }

    public synchronized Optional<HeightBlock> readBlock(Uint256 hash) {
        Optional<HeightBlock> data = this.findChainBlockInLevelDB(hash);
        if (!data.isPresent()) {
            return Optional.empty();
        }
        logger.debug("Get block from levelDB: {}", hash.toHexReverse256());
        data.get().getFileChainBlock().loadBlock();
        return data;
    }

    /**
     * 从 0 开始
     *
     * @return
     */
    public int getLatestHeight() {
        return indexHeight.getLastHeight().getHeight();
    }

    /**
     * @param hash
     * @return 找不到返回 -1
     */
    public int getHeight(Uint256 hash) {
        return indexHeight.getHeight(hash);
    }

    public synchronized boolean resetLatest(HeightBlock newBlock) {
        int height = newBlock.getHeight();
        ChainBlock block = newBlock.getBlock();
        Height ht = indexHeight.getLastHeight();
        int latest = ht.getHeight();
        if (latest >= height) {
            return false;
        }
        Assert.isTrue(latest + 1 == height, "Only one by one !");
        int h = indexHeight.tryPush(newBlock.getBlock());
        if (h >= 0) {// 大部分情况走这里
            return true;
        }
        logger.debug("需要截断, 然后新增, 例如: 出现分叉的时候的回滚, 也许数据量会很大, 不在内存中计算, 可能是个长时间的任务");
        logger.info("Rollback latest height: {}, {}, new height: {}, {}", latest, ht.getHash(), height, block.hash());
        indexHeight.setHeight(block.hash(), height);
        ChainBlock next = block;
        for (int i = latest; i > 0; i--) {
            Uint256 pre = next.header.getPreHash();
            Uint256 r = indexHeight.get(i).get();
            HeightBlock preHeight = this.findChainBlockInLevelDB(pre).get();
            Assert.isTrue(preHeight.getHeight() == i, "PreHash Must height - 1 : " + i);
            if (r.equals(pre)) {
                logger.debug("Rollback TO: {}, {}", i, r);
                break;
            }
            // 回退高度, 这样即使中途出错, 仍然可以运行在新的高度
            indexHeight.removeTail(1);// 第一个可以不回退, 为了简单忽略
            indexHeight.setHeight(pre, i);
            next = preHeight.getBlock();
        }
        indexHeight.setLastBlock(newBlock.getBlock().hash(), newBlock.getHeight());
        logger.debug("To new height :{}, {}", height, block.hash());
        return true;
    }

    public File getRoot() {
        return root;
    }

    public IndexBlock getIndexBlock() {
        return indexBlock;
    }

    public IndexHeight getIndexHeight() {
        return indexHeight;
    }

    @Override
    public void close() throws IOException {
        try {
            indexBlock.close();
        } finally {
            fileAccess.close();
        }
    }

    public void verifyNBits(ChainBlock cb) {
        HeightBlock hb = this.readBlock(cb.header.getPreHash()).get();
        Uint32 uint32 = PowDifficulty.nextWorkRequired(new ChainHeight(hb.getHeight(), hb.getBlock()), n -> {
            Optional<Uint256> hash = this.getHash(n);
            return this.readBlock(hash.get()).get().getBlock();
        });
        if (!uint32.equals(cb.header.getBits())) {
            logger.error("Block nBits error : {} != {}, BLOCK {}", uint32, cb.header.getBits(), cb.hash());
            throw new IllegalArgumentException("POW nBits error");
        }
    }
}
