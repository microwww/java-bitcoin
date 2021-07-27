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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * struct CDiskTxPos : public FlatFilePos
 * init.cpp | CleanupBlockRevFiles
 * validation.h  Open a block file (blk?????.dat) FILE* OpenBlockFile(const FlatFilePos &pos, bool fReadOnly = false);
 * bicoin-core `WriteBlockToDisk`
 **/
public class DiskBlock implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(DiskBlock.class);
    public static final Pattern pt = Pattern.compile("blk0*([0-9]+).dat");
    public static final String sequenceFile = "blk%05d.dat"; // blk00000.dat

    private final int bestConfirmHeight;
    private static int sequence = -1;
    private final CChainParams chainParams;
    private final File root;
    private final DB levelDB;
    private final MemBlockHeight heights;

    private int MAX_BYTES = 128 * 1024 * 1024; // 128M
    private FileChannel current; //
    private File currentFile; //

    public DiskBlock(CChainParams chainParams) {
        this.chainParams = chainParams;
        this.bestConfirmHeight = this.chainParams.settings.getBestConfirmHeight();
        File file = ChainBlockStore.lockupRootDirectory(chainParams.settings);
        root = new File(file, "blocks");
        try {
            levelDB = ChainBlockStore.leveldb(root, "index", chainParams.settings.isReIndex());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ChainBlock genesisBlock = chainParams.env.createGenesisBlock();
        heights = new MemBlockHeight(genesisBlock);
    }

    public synchronized DiskBlock init() {
        if (current != null) {
            throw new IllegalStateException("Not re-try it");
        }
        logger.info("Init DiskBlock, Get block-file, And init chain-height");
        try {
            currentFile = fetchFile();
            current = new RandomAccessFile(currentFile, "rwd").getChannel();
        } catch (IOException e) {
            try {
                if (current != null) current.close();
            } catch (IOException ex) {
            }
            throw new RuntimeException(e);
        } finally {
        }
        byte[] bytes = levelDB.get(LevelDBPrefix.DB_LAST_BLOCK.prefixBytes);
        ChainBlock generate = heights.getGenerate();
        if (bytes == null) { // 如果没有最新的块, 需要初始化创世块
            HeightChainBlock hc = new HeightChainBlock(generate, 0);
            this.writeBlock(hc, true);
            byte[] array = hc.serialization();
            levelDB.put(LevelDBPrefix.DB_LAST_BLOCK.prefixBytes, array);
        } else {
            LinkedList<Uint256> list = new LinkedList<>();
            HeightChainBlock ds = new HeightChainBlock().deserialization(bytes);
            ChainBlock latest = ds.getBlock();
            int h = ds.getHeight();
            logger.debug("Loading latest block in levelDB : {}, {}", h, latest.hash());
            while (true) {
                list.add(latest.hash());
                Uint256 next = latest.header.getPreHash();
                Optional<HeightChainBlock> nv = this.readBlock(next);
                if (nv.isPresent()) {
                    h--;
                    HeightChainBlock hc = nv.get();
                    Assert.isTrue(hc.getHeight() == h, "levelDB height not match");
                    latest = hc.getBlock();
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
            Optional<HeightChainBlock> heightChainBlock = this.readBlock(uint256.get());
            HeightChainBlock hbk = heightChainBlock.get();
            levelDB.put(LevelDBPrefix.DB_LAST_BLOCK.prefixBytes, hbk.serialization());
        }
        logger.info("Init block-chain TO : {}, {}", heights.getLatestHeight(), heights.getLatestHash());
        return this;
    }

    public Optional<Uint256> getHash(int height) {// TODO : block file !
        return heights.get(height);
    }

    private FileChannel tryRollingFile() throws IOException {
        if (current == null) {
            throw new IllegalStateException("Please init first, invoke init()");
        }
        while (current.position() >= MAX_BYTES) {
            currentFile = maxSequenceFile(1);
            long position = currentFile.length();
            if (current != null) current.close();
            current = new RandomAccessFile(currentFile, "rwd").getChannel().position(position);
            logger.info("Rolling file : {}", currentFile.getCanonicalPath());
        }
        return current;
    }

    private File fetchFile() throws IOException {
        return maxSequenceFile(0);
    }

    private File maxSequenceFile(int inv) throws IOException {
        if (sequence < 0) {
            File[] files = root.listFiles();
            sequence = 0;
            for (File f : files) {
                if (f.isFile()) {
                    String name = f.getName().toLowerCase();
                    Matcher matcher = pt.matcher(name);
                    if (matcher.matches()) {
                        int nv = Integer.parseInt(matcher.group(1));
                        if (nv > sequence) {
                            sequence = nv;
                        }
                    }
                }
            }
        }
        sequence += inv;
        File f = new File(root, String.format(sequenceFile, sequence));
        if (!f.exists()) {
            f.createNewFile();
        } else if (f.isDirectory()) {
            throw new IOException("File do not allow a directory : " + f.getCanonicalPath());
        }
        return f;
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

    public boolean writeBlock(ChainBlock block, boolean ifExistSkip) {
        Uint256 pre = block.header.getPreHash();
        Optional<HeightChainBlock> hc = this.readBlock(pre);
        if (hc.isPresent()) {
            return writeBlock(new HeightChainBlock(block, hc.get().getHeight() + 1), ifExistSkip);
        }
        return false;
    }

    public boolean writeBlock(ChainBlock block, int height, boolean ifExistSkip) {
        return writeBlock(new HeightChainBlock(block, height), ifExistSkip);
    }

    /**
     * 只能顺序写
     *
     * @param hc
     * @return
     */
    public boolean writeBlock(HeightChainBlock hc, boolean ifExistSkip) {
        ChainBlock block = hc.getBlock();
        Uint256 hash = block.hash();
        if (ifExistSkip) {
            if (findInLevelDB(hash).isPresent()) {
                return false;
            }
        }
        write(hc);
        return true;
    }

    public Optional<byte[]> findInLevelDB(Uint256 hash) {
        byte[] key = ByteUtil.concat(LevelDBPrefix.DB_BLOCK_INDEX.prefixBytes, hash.fill256bit());
        return Optional.ofNullable(levelDB.get(key));
    }

    private void write(HeightChainBlock hc) {
        ChainBlock block = hc.getBlock();
        Uint256 hash = block.hash();
        // magic + len + payload
        ByteBuf r = Unpooled.buffer()
                .writeInt(chainParams.getEnvParams().getMagic())
                .writeIntLE(0); // 后面覆盖掉
        Assert.isTrue(r.readableBytes() == 8, "--");
        block.writeHeader(r).writeTxCount(r).writeTxBody(r);
        //

        int len = r.readableBytes();
        r.setIntLE(4, len - 8);
        try {
            FileChannel ch = tryRollingFile();
            long position = ch.position();
            int write = ch.write(r.nioBuffer());
            Assert.isTrue(write == len, "Not write all");
            logger.debug("Add levelDB: {} , {} , {}", hc.getHeight(), hash, block.header.getPreHash());
            Assert.isTrue(position < Integer.MAX_VALUE, "Int overflow");

            // height + position + len + name
            r.clear().writeInt(hc.getHeight()).writeInt((int) position).writeInt(len).writeBytes(currentFile.getName().getBytes(StandardCharsets.UTF_8));

            levelDB.put(
                    ByteUtil.concat(LevelDBPrefix.DB_BLOCK_INDEX.prefixBytes, hash.fill256bit()),
                    ByteUtil.readAll(r)
            );
            int height = hc.getHeight();
            if (height > this.getLatestHeight()) {
                this.resetLatest(block, height);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<HeightChainBlock> readBlock(Uint256 hash) {
        try {
            return tryReadBlock(hash);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<HeightChainBlock> tryReadBlock(Uint256 hash) throws IOException {
        Optional<byte[]> data = this.findInLevelDB(hash);
        if (!data.isPresent()) {
            return Optional.empty();
        }
        logger.debug("Get levelDB: {}", hash);
        byte[] bytes = data.get();
        Assert.isTrue(bytes.length > 12, "(height,position,length,name)Uint32 + Uint32 + Uint32 + string....");

        //
        ByteBuf byteBuf = Unpooled.copiedBuffer(bytes);
        int height = byteBuf.readInt();
        int position = byteBuf.readInt();
        int len = byteBuf.readInt();
        byte[] name = ByteUtil.readAll(byteBuf);
        File f = new File(root, new String(name, StandardCharsets.UTF_8));
        //

        FileChannel r = new RandomAccessFile(f, "r").getChannel();
        r.position(position);
        ByteBuffer v = ByteBuffer.allocate(len);
        int read = r.read(v);
        Assert.isTrue(read == len, "not read All");
        v.rewind();
        ByteBuf bf = Unpooled.copiedBuffer(v);
        int magic = bf.readInt();
        Assert.isTrue(chainParams.getEnvParams().getMagic() == magic, "marge match !");// TODO :: 校验头
        bf.readInt(); // len
        ChainBlock chainBlock = new ChainBlock().readHeader(bf).readBody(bf);
        return Optional.of(new HeightChainBlock(chainBlock, height));
    }

    private void levelDBPut(byte[] k1, byte[] k2, byte[] v1, byte[] v2) {
        levelDB.put(
                ByteUtil.concat(k1, k2),
                ByteUtil.concat(v1, v2)
        );
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
            current.close();
        }
    }

    public boolean resetLatest(ChainBlock block, int height) {
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
                Optional<HeightChainBlock> hc = this.readBlock(hash);
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
        levelDB.put(LevelDBPrefix.DB_LAST_BLOCK.prefixBytes, new HeightChainBlock(block, height).serialization());
        return true;
    }
}
