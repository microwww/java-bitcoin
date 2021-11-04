package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.chain.PowDifficulty;
import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.Assert;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

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
    private final File root; // end with `../blocks/`
    private final IndexBlock indexBlock;
    private final IndexTransaction indexTransaction;
    private final AccessBlockFile fileAccess;

    public static class SpringDiskBlock extends DiskBlock {
        private final ApplicationEventPublisher publisher;

        public SpringDiskBlock(CChainParams chainParams, ApplicationEventPublisher publisher) {
            super(chainParams);
            this.publisher = publisher;
        }

        @Override
        public FileChainBlock writeBlock(ChainBlock block, int height, boolean ifExistSkip) {
            FileChainBlock fc = super.writeBlock(block, height, ifExistSkip);
            this.getIndexTransaction().indexTransaction(fc);
            publisher.publishEvent(fc.new BlockWrite2fileEvent());
            return fc;
        }
    }

    public DiskBlock(CChainParams chainParams) {
        this.chainParams = chainParams;
        this.bestConfirmHeight = this.chainParams.settings.getBestConfirmHeight();
        try {
            root = chainParams.settings.getBlocksDirectory();
            logger.info("Block directory: {}", root.getCanonicalPath());
            indexBlock = new IndexBlock(chainParams);
            indexTransaction = new IndexTransaction(chainParams);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.fileAccess = new AccessBlockFile(root, chainParams.getEnvParams().getMagic());
        logger.info("Remember to invoke `init`, And it is a long time task maybe!");
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
                FileChainBlock fc = new FileChainBlock(file, channel.position());
                fc.readBlock(bf, channel);
                int magic = chainParams.getEnvParams().getMagic();
                Assert.isTrue(fc.getMagic() == chainParams.getEnvParams().getMagic(), "Env is not match , need : " + magic);
                Uint256 preHash = fc.getTarget().header.getPreHash();
                Uint256 hash = fc.getTarget().hash();
                int height = indexBlock.getHeight(preHash);
                if (height < 0) {
                    Optional<FileChainBlock> opt = indexBlock.findChainBlockInLevelDB(preHash);
                    if (opt.isPresent()) {
                        height = opt.get().getTarget().getHeight();
                    }
                    if (hash.equals(chainParams.env.G.hash())) {
                        height = -1;
                    } else {
                        Assert.isTrue(height >= 0, "prehash must exist");
                    }
                }
                Assert.isTrue(fc.getTarget().header.getHeight().isPresent(), "Set block height");
                this.indexBlock(fc);
                long next = System.currentTimeMillis();
                if (next - time > 5000) {
                    logger.info("Re-index Height: {}, Hash: {}, PreHash: {}", height + 1, hash.toHexReverse256(), preHash.toHexReverse256());
                    time = next;
                }
            }
        }
    }

    /**
     * 因为耗时太长, 所以要等 Spring 启动后再执行.
     *
     * @return this
     */
    public synchronized DiskBlock init() {
        logger.info("Init DiskBlock, Get block-file, And init chain-height");

        ChainBlock generate = chainParams.env.G;
        logger.info("Generate block hash : {}", generate.hash().toHexReverse256());

        if (chainParams.settings.isReIndex()) {
            try {
                logger.info("Reindex BLOCK, long time");
                reindex();
                logger.info("Reindex BLOCK OVER : {}", indexBlock.getLastHeight());
                return this;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        int last = indexBlock.getLastHeight();
        if (last <= 0) { // 如果没有最新的块, 需要初始化创世块
            this.writeBlock(generate, 0, true);
        }
        logger.info("Init block-chain TO : {}, {}", indexBlock.getLastHeight());
        return this;
    }

    public Optional<ChainBlock> findChainBlock(Uint256 hash) {
        return indexBlock.findChainBlock(hash);
    }

    public DiskBlock putChainBlockToLevelDB(FileChainBlock hc) {
        indexBlock.writeChainBlockToLevelDB(hc);
        return this;
    }

    public Optional<Uint256> getHash(int height) {// TODO : block file !
        return indexBlock.get(height);
    }

    public ChainBlock getLastBlock() {
        int height = indexBlock.getLastHeight();
        return this.getHash(height)
                .flatMap(this::getChinBlock)
                .orElseThrow(() -> new IllegalStateException("Not find height Block : " + height));
    }

    public ChainBlock getBestBlock() {
        int height = indexBlock.getLastHeight();
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
        return this.readBlock(hash);
    }

    public OptionalInt writeBlock(ChainBlock block, boolean ifExistSkip) {
        Uint256 pre = block.header.getPreHash();
        Optional<ChainBlock> hc = this.readBlock(pre);
        if (hc.isPresent()) {
            int h = hc.get().getHeight() + 1;
            writeBlock(block, h, ifExistSkip);
            if (block.header.getHeight().isPresent()) {
                Assert.isTrue(h == block.getHeight(), "Not set height");
            } else {
                block.header.setHeight(h);
            }
            return OptionalInt.of(h);
        }
        return OptionalInt.empty();
    }

    /**
     * 只能顺序写
     *
     * @return
     */
    public FileChainBlock writeBlock(ChainBlock block, int height, boolean ifExistSkip) {
        block.header.setHeight(height);
        Uint256 hash = block.hash();
        if (ifExistSkip) {
            Optional<FileChainBlock> fd = indexBlock.findChainBlockInLevelDB(hash);
            if (fd.isPresent()) {
                return fd.get();
            }
        }
        FileChainBlock fc = write(block);
        this.indexBlock(fc);
        return fc;
    }

    private synchronized FileChainBlock write(ChainBlock block) {
        try {
            return fileAccess.writeBlock(block);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void indexBlock(FileChainBlock write) {
        ChainBlock block = write.getTarget();
        int height = block.getHeight();
        if (logger.isDebugEnabled())
            logger.debug("Add BLOCK to levelDB: {}, {} , {} , {}", write.getPosition(), height, block.hash(), block.header.getPreHash());
        Assert.isTrue(write.getPosition() < Integer.MAX_VALUE, "Int overflow");
        indexBlock.writeChainBlockToLevelDB(write);
        this.resetHeight(block);
    }


    public void resetHeight(ChainBlock hc) {
        int height = hc.getHeight();
        if (height > this.getLatestHeight()) {
            this.resetLatest(hc);
        }
    }

    public synchronized Optional<ChainBlock> readBlock(Uint256 hash) {
        Optional<FileChainBlock> data = this.indexBlock.findChainBlockInLevelDB(hash);
        if (!data.isPresent()) {
            return Optional.empty();
        }
        logger.debug("Get block from levelDB: {}", hash.toHexReverse256());
        return Optional.ofNullable(data.get().load(false));
    }

    /**
     * 从 0 开始
     *
     * @return
     */
    public int getLatestHeight() {
        return indexBlock.getLastHeight();
    }

    /**
     * @param hash
     * @return 找不到返回 -1
     */
    public int getHeight(Uint256 hash) {
        return indexBlock.getHeight(hash);
    }

    private synchronized boolean resetLatest(ChainBlock newBlock) {
        int height = newBlock.getHeight();
        ChainBlock latest = indexBlock.getLastBlock();
        int last = latest.getHeight();
        if (last >= height) {
            return false;
        }
        Assert.isTrue(last + 1 == height, "Only one by one !");
        int h = indexBlock.tryPush(newBlock);
        if (h >= 0) {// 大部分情况走这里
            return true;
        }
        Uint256 hash = latest.header.hash();
        logger.debug("需要截断, 然后新增, 例如: 出现分叉的时候的回滚, 也许数据量会很大, 不在内存中计算, 可能是个长时间的任务");
        logger.info("Rollback latest height: {}, {}, new height: {}, {}", latest, hash, height, newBlock.hash());
        indexBlock.setHeight(newBlock.hash(), height);
        ChainBlock next = newBlock;
        for (int i = last; i > 0; i--) {
            Uint256 pre = next.header.getPreHash();
            Uint256 r = indexBlock.get(i).get();
            FileChainBlock preHeight = indexBlock.findChainBlockInLevelDB(pre).get();
            Assert.isTrue(preHeight.getTarget().getHeight() == i, "PreHash Must height - 1 : " + i);
            if (r.equals(pre)) {
                logger.debug("Rollback TO: {}, {}", i, r);
                break;
            }
            // 回退高度, 这样即使中途出错, 仍然可以运行在新的高度
            List<ChainBlock> blocks = indexBlock.removeTail(1);// 第一个可以不回退, 为了简单忽略
            for (ChainBlock block : blocks) {
                for (RawTransaction tx : block.getTxs()) {
                    indexTransaction.removeTransaction(tx.hash());
                }
            }
            indexBlock.setHeight(pre, i);
            // TODO:: 回退交易列表
            next = preHeight.getTarget();
        }
        indexBlock.setLastBlock(newBlock.hash(), newBlock.getHeight());
        logger.debug("To new height :{}, {}", height, newBlock.hash());
        return true;
    }

    public void verifyTransactions(ChainBlock cb) {
        this.indexTransaction.verifyTransactions(cb);
    }

    public Optional<RawTransaction> getTransaction(Uint256 hash) {
        return this.indexTransaction.getTransaction(hash);
    }

    IndexBlock getIndexBlock() {
        return indexBlock;
    }

    IndexTransaction getIndexTransaction() {
        return indexTransaction;
    }

    @Override
    public void close() throws IOException {
        try {
            try {
                indexTransaction.close();
            } finally {
                indexBlock.close();
            }
        } finally {
            fileAccess.close();
        }
    }

    public void verifyNBits(ChainBlock cb) {
        ChainBlock block = this.readBlock(cb.header.getPreHash()).get();
        Uint32 uint32 = nextPow(block);
        if (!uint32.equals(cb.header.getBits())) {
            logger.error("Block nBits error : {} != {}, BLOCK {}", uint32, cb.header.getBits(), cb.hash());
            throw new IllegalArgumentException("POW nBits error");
        }
    }

    public Uint32 nextPow(ChainBlock block) {
        Uint32 uint32 = PowDifficulty.nextWorkRequired(block, n -> {
            Optional<Uint256> hash = this.getHash(n);
            return this.readBlock(hash.get()).get();
        });
        return uint32;
    }
}
