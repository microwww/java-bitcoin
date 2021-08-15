package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.util.FilesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;

public class AccessBlockFile implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(AccessBlockFile.class);
    public static final Pattern pt = Pattern.compile("blk0*([0-9]+).dat");
    public static final String sequenceFile = "blk%05d.dat"; // blk00000.dat
    public static int MAX_BYTES = 128 * 1024 * 1024 - 2 * 1024; // 128M

    private final RollingFile rollingFile = new RollingFile();
    private final File root;

    public AccessBlockFile(File root) {
        this.root = root;
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
    public FileChannel channel() {
        try {
            return getFileChannel();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public FileChannel getFileChannel() throws IOException {
        long position = rollingFile.current.position();
        if (position >= MAX_BYTES) {
            rollingFile.tryRollingFile();
        }
        return rollingFile.current;
    }

    public File getFile() {
        return rollingFile.currentFile;
    }

    public class RollingFile {
        private FileChannel current; //
        private File currentFile; //

        private synchronized FileChannel tryRollingFile() throws IOException {
            int from = 0;
            if (currentFile != null) {
                from = Integer.valueOf(pt.matcher(currentFile.getName()).group(1));
            }
            for (int i = from; i < Integer.MAX_VALUE; i++) {
                File file = sequenceFile(i);
                long length = file.length();
                if (length >= MAX_BYTES) {
                    continue;
                }
                if (current != null) current.close();
                currentFile = file;
                current = new RandomAccessFile(currentFile, "rwd").getChannel().position(length);
                logger.info("Rolling file : {} , from position: {}", currentFile.getCanonicalPath(), length);
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
    }
}
