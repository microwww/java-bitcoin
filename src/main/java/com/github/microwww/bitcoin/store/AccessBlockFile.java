package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.util.FilesUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccessBlockFile implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(AccessBlockFile.class);
    public static final Pattern pt = Pattern.compile("blk0*([0-9]+).dat");
    public static final String sequenceFile = "blk%05d.dat"; // blk00000.dat
    public static int MAX_BYTES = 128 * 1024 * 1024 - 2 * 1024; // 128M

    private final RollingFile rollingFile = new RollingFile();
    private final File root;
    private final int magic;

    public AccessBlockFile(File root, int magic) {
        Assert.isTrue(magic != 0, "To set magic");
        this.root = root;
        this.magic = magic;
        try {
            rollingFile.tryRollingFile();
        } catch (IOException e) {
            try {
                rollingFile.current.close();
            } catch (IOException ex) {
            }
            throw new RuntimeException(e);
        }
    }

    public File[] listFile() {
        File[] files = root.listFiles();
        File[] fs = Arrays.stream(files)
                .filter(f -> pt.matcher(f.getName()).matches())
                .sorted(Comparator.comparing(File::getName))
                .toArray(File[]::new);
        return fs;
    }

    @Override
    public void close() throws IOException {
        rollingFile.current.close();
    }

    /**
     * 每次使用重新获取, 可以自动判断文件是否写满, 自动滚动
     *
     * @return
     */
    public void tryRollingFile() throws IOException {
        long position = rollingFile.current.position();
        if (position >= MAX_BYTES) {
            rollingFile.tryRollingFile();
        }
    }

    public File getFile() {
        return rollingFile.currentFile;
    }

    public FileChainBlock writeBlock(ChainBlock block) throws IOException {
        tryRollingFile();
        return rollingFile.writeBlock(block);
    }

    public class RollingFile {
        private FileChannel current; //
        private File currentFile; //
        private ByteBuf cache = Unpooled.buffer(1024 * 1024);
        private long position = 0;

        private synchronized FileChannel tryRollingFile() throws IOException {
            int from = 0;
            if (currentFile != null) {
                String name = currentFile.getName();
                Matcher matcher = pt.matcher(name);
                Assert.isTrue(matcher.matches(), "Not find match string: " + pt.pattern() + ", " + name);
                from = Integer.valueOf(matcher.group(1));
            }
            for (int i = from; i < Integer.MAX_VALUE; i++) {
                File file = sequenceFile(i);
                long length = file.length();
                if (length >= MAX_BYTES) {
                    continue;
                }
                if (current != null) current.close();
                currentFile = file;
                position = file.length();
                current = new RandomAccessFile(currentFile, "rwd").getChannel().position(length);
                logger.info("Block file in-rolling: {} , from position: {}", currentFile.getCanonicalPath(), length);
                break;
            }
            return current;
        }

        private synchronized File sequenceFile(int sequence) throws IOException {
            Assert.isTrue(sequence >= 0, "start 0");
            File f = new File(root, String.format(sequenceFile, sequence));
            FilesUtil.createCanWriteFile(f);
            return f;
        }

        // will set `fileTransactions`
        public synchronized FileChainBlock writeBlock(ChainBlock block) throws IOException {
            FileChainBlock fc = new FileChainBlock(this.currentFile);
            Assert.isTrue(position == current.position(), "Position only append");
            fc.setPosition(position);
            FileChannel file = this.current;
            while (true) {
                FileLock lock = file.tryLock(position, Integer.MAX_VALUE, false);
                if (lock != null) {
                    try {
                        cache.clear();
                        int lengthPosition = 4;// INIT length = 0, and next to set
                        int magicAndLengthBytes = 8;
                        cache.writeInt(magic).writeIntLE(0);
                        block.serialization(cache);
                        int index = cache.writerIndex();
                        cache.setIntLE(lengthPosition, index - magicAndLengthBytes);
                        ByteBuffer f = cache.nioBuffer();
                        while (f.hasRemaining()) {
                            file.write(f);
                        }
                        file.force(false);
                        position += cache.writerIndex();
                        logger.debug("Write block file {}, position {}", currentFile.getName(), position);
                        return fc;
                    } finally {
                        lock.release();
                    }
                } else {
                    try {
                        logger.debug("Not get file lock, wait ... 100 ms ");
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }
}
