package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.conf.ChainBlockStore;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.store.LevelDBPrefix;
import com.github.microwww.bitcoin.store.MemBlockHeight;
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
import java.nio.channels.FileLock;
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

    private long filePoint;
    private int MAX_BYTES = 128 * 1024 * 1024; // 128M
    private FileChannel current; //
    private File currentFile; //
    private FileLock fileLock;

    public DiskBlock(CChainParams chainParams) {
        this.chainParams = chainParams;
        this.bestConfirmHeight = this.chainParams.settings.getBestConfirmHeight();
        File file = ChainBlockStore.lockupRootDirectory(chainParams.settings);
        lockFile(file);
        root = new File(file, "blocks");
        try {
            levelDB = ChainBlockStore.leveldb(root, "index", chainParams.settings.isReIndex());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        heights = new MemBlockHeight(chainParams);
    }

    private void lockFile(File root) {
        try {
            File lock = new File(root, "lock");
            lock.createNewFile();
            fileLock = new RandomAccessFile(lock, "rw").getChannel().lock();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public synchronized DiskBlock init() {
        if (current != null) {
            throw new IllegalStateException("Not re-try it");
        }
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
        ChainBlock genesisBlock = chainParams.env.createGenesisBlock();
        heights.init(genesisBlock);
        if (bytes == null) { // 如果没有最新的块, 需要初始化创世块
            byte[] bts = ByteUtil.readAll(genesisBlock.serialization());
            levelDB.put(LevelDBPrefix.DB_LAST_BLOCK.prefixBytes, bts);
            this.writeBlock(genesisBlock, true);
        } else {
            LinkedList<Uint256> list = new LinkedList<>();
            ChainBlock latest = new ChainBlock().deserialization(bytes);
            while (true) {
                list.add(latest.hash());
                Uint256 next = latest.header.getPreHash();
                Optional<ChainBlock> nv = this.tryReadBlock(next);
                if (nv.isPresent()) {
                    latest = nv.get();
                } else break;
            }

            Uint256 last = list.pollLast();
            Assert.isTrue(last.equals(genesisBlock.hash()), "Must equal GenesisBlock");

            int best = chainParams.settings.getBestConfirmHeight();
            for (int i = list.size(), j = 1; i > best; i--, j++) { // 跳过了创世块 从1开始
                heights.loadFrom(list.pollLast(), j);
            }

            for (int j = heights.getLatestHeight(); j >= 0; j++) {
                Uint256 uint256 = list.pollLast();
                if (uint256 == null) {
                    break;
                }
                Optional<ChainBlock> cg = this.tryReadBlock(uint256);
                if (cg.isPresent()) {
                    heights.loadFrom(cg.get(), j + 1);
                } else break;
            }
        }
        return this;
    }

    public Optional<Uint256> getHash(int height) {// TODO : block file !
        return heights.get(height);
    }

    private FileChannel tryRollingFile() throws IOException {
        if (current == null) {
            throw new IllegalStateException("Please init first, invoke init()");
        }
        if (filePoint >= MAX_BYTES) {
            currentFile = maxSequenceFile(1);
            filePoint = currentFile.length();
            if (current != null) current.close();
            current = new RandomAccessFile(currentFile, "rwd").getChannel().position(filePoint);
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
        byte[] key = ByteUtil.concat(LevelDBPrefix.DB_BLOCK_INDEX.prefixBytes, hash.fill256bit());
        byte[] bytes = levelDB.get(key);
        if (bytes == null || bytes.length <= 0) {
            return Optional.empty();
        }
        return Optional.of(new ChainBlock().deserialization(bytes));
    }

    public boolean containsBlock(Uint256 hash) {
        byte[] key = ByteUtil.concat(LevelDBPrefix.DB_BLOCK_INDEX.prefixBytes, hash.fill256bit());
        byte[] bytes = levelDB.get(key);
        return bytes != null && bytes.length > 0;
    }

    /**
     * 只能顺序写
     *
     * @param block
     * @return
     */
    public boolean writeBlock(ChainBlock block, boolean ifExistSkip) {
        byte[] key = ByteUtil.concat(LevelDBPrefix.DB_BLOCK_INDEX.prefixBytes, block.hash().fill256bit());
        if (ifExistSkip) {
            byte[] bytes = levelDB.get(key);
            if (bytes != null) {
                return false;
            }
        }
        ByteBuf buffer = Unpooled.buffer();
        ByteBuf serialization = block.serialization();
        int len = serialization.readableBytes();
        buffer.writeInt(chainParams.getEnvParams().getMagic())
                .writeIntLE(len).writeBytes(serialization);
        try {
            FileChannel ch = tryRollingFile();
            len = buffer.readableBytes();
            for (int i = 0; i < len; ) {
                i += ch.write(buffer.nioBuffer());
            }
            logger.debug("Add levelDB: {} , {}", block.hash(), block.header.getPreHash());
            buffer.clear().writeInt((int) filePoint).writeInt(len).writeBytes(currentFile.getName().getBytes(StandardCharsets.UTF_8));
            levelDB.put(
                    ByteUtil.concat(LevelDBPrefix.DB_BLOCK_INDEX.prefixBytes, block.hash().fill256bit()),
                    ByteUtil.readAll(buffer)
            );
            filePoint += len;
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<ChainBlock> tryReadBlock(Uint256 hash) {
        try {
            return readBlock(hash);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<ChainBlock> readBlock(Uint256 hash) throws IOException {
        byte[] bytes = levelDB.get(ByteUtil.concat(LevelDBPrefix.DB_BLOCK_INDEX.prefixBytes, hash.fill256bit()));
        logger.info("Get levelDB: {}", hash);
        if (bytes == null) {
            return Optional.empty();
        }
        Assert.isTrue(bytes.length > 8, "Uint32 + Uint32 + string....");
        ByteBuf byteBuf = Unpooled.copiedBuffer(bytes);
        int position = byteBuf.readInt();
        int len = byteBuf.readInt();
        File f = new File(root, new String(ByteUtil.readAll(byteBuf)));
        FileChannel r = new RandomAccessFile(f, "r").getChannel();
        r.position(position);
        ByteBuffer v = ByteBuffer.allocate(len);
        int read = r.read(v);
        Assert.isTrue(read == len, "not read All");
        v.rewind();
        ByteBuf bf = Unpooled.copiedBuffer(v);
        Assert.isTrue(chainParams.getEnvParams().getMagic() == bf.readInt(), "marge match !");// TODO :: 校验头
        bf.readInt(); // len
        return Optional.of(new ChainBlock().readHeader(bf).readBody(bf));
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

    public DB getLevelDB() {
        return levelDB;
    }

    @Override
    public void close() throws IOException {
        try {
            levelDB.close();
        } finally {
            try {
                current.close();
            } finally {
                fileLock.close();
            }
        }
    }
}
