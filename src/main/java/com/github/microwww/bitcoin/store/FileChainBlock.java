package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.ChainBlock;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

class FileChainBlock extends AbstractFilePosition<ChainBlock> {
    private static final Logger logger = LoggerFactory.getLogger(FileChainBlock.class);
    private int magic;

    public FileChainBlock(File file, ChainBlock target) {
        super(file, target);
    }

    public FileChainBlock(File file, long position) {
        super(file, position);
    }

    @Override
    public ChainBlock deserialization(FileChannel channel) throws IOException {
        readBlock(channel);
        return this.target;
    }

    public FileChainBlock readBlock(FileChannel channel) throws IOException {
        return this.readBlock(Unpooled.buffer(), channel);
    }

    public FileChainBlock readBlock(ByteBuf buffer, FileChannel channel) throws IOException {
        try {
            tryReadBlock(buffer, channel);
        } catch (RuntimeException ex) {
            int i = buffer.readerIndex();
            logger.error("Read block error : {}[{}] bytes-index {}", this.file.getAbsolutePath(), this.position, i);
            throw ex;
        }
        return this;
    }

    private void tryReadBlock(ByteBuf cache, FileChannel channel) throws IOException {
        ByteBuffer f = ByteBuffer.allocate(1 * 1024 * 1024);
        channel.read(f);
        f.rewind();
        cache.clear().writeBytes(f);

        int magicAndLengthBytes = 8;
        this.magic = cache.readInt();
        int len = cache.readIntLE();
        // 隔离见证时, 可超过 1M , 最大 4M
        Assert.isTrue(len > 80 && len < 4_000_000, "Data format error, 80 < ? < 4M");

        super.readLength(channel, cache, len);

        this.target = new ChainBlock().reset(cache);
        Assert.isTrue(cache.readerIndex() == len + magicAndLengthBytes, "Fill block bytes.length != block read length");
        // channel.position(this.position + cache.readerIndex());
        logger.debug("Read File : {}, Position: {}", file.getName(), this.position);
    }

    public int getMagic() {
        return magic;
    }

    public FileChainBlock setMagic(int magic) {
        this.magic = magic;
        return this;
    }

    public boolean isCache() {
        return target == null;
    }
}