package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.ChainBlock;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileChainBlock {
    private static final Logger logger = LoggerFactory.getLogger(FileChainBlock.class);
    private final File file;
    private int magic;
    private long position;
    private ChainBlock block;
    private FileTransaction[] fileTransactions; // run writeBlock method will set

    public FileChainBlock(File file) {
        this.file = file;
    }

    public FileChainBlock loadBlock() {
        return loadBlock(false);
    }

    public FileChainBlock loadBlock(boolean force) {
        if (block == null || force) try {
            FileChannel channel = new RandomAccessFile(file, "r").getChannel();
            channel.position(this.position);
            readBlock(Unpooled.buffer(), channel);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return this;
    }

    public FileChainBlock readBlock(ByteBuf cache, FileChannel channel) throws IOException {
        try {
            return readBlockWithError(cache, channel);
        } catch (RuntimeException ex) {
            int i = cache.readerIndex();
            logger.error("Read block error : {}[{}] bytes-index {}", this.file.getAbsolutePath(), this.position, i);
            throw ex;
        }
    }

    private FileChainBlock readBlockWithError(ByteBuf cache, FileChannel channel) throws IOException {
        ByteBuffer f = ByteBuffer.allocate(1 * 1024 * 1024);
        channel.position(this.position);
        channel.read(f);
        f.rewind();
        cache.clear().writeBytes(f);
        this.magic = cache.readInt();
        int len = cache.readIntLE();
        Assert.isTrue(len > 80 && len < 4_000_000, "Data format error, 80 < ? < 4M");
        while (cache.readableBytes() < len) {
            f.clear();
            channel.read(f);
            f.rewind();
            cache.writeBytes(f);
        }
        this.block = new ChainBlock().readHeader(cache).readBody(cache);
        Assert.isTrue(cache.readerIndex() == len + 8, "Fill block bytes.length != block read length");
        channel.position(this.position + cache.readerIndex());
        logger.debug("Read File : {}, Position: {}", file.getName(), this.position);
        return this;
    }

    public File getFile() {
        return file;
    }

    public ChainBlock getBlock() {
        return block;
    }

    public void setBlock(ChainBlock block) {
        this.block = block;
    }

    public long getPosition() {
        return position;
    }

    public FileChainBlock setPosition(long position) {
        this.position = position;
        return this;
    }

    public int getMagic() {
        return magic;
    }

    public FileChainBlock setMagic(int magic) {
        this.magic = magic;
        return this;
    }

    public FileTransaction[] getFileTransactions() {
        return fileTransactions;
    }

    public void setFileTransactions(FileTransaction[] fileTransactions) {
        this.fileTransactions = fileTransactions;
    }

    public boolean isCache() {
        return block == null;
    }
}