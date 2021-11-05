package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.event.BitcoinEvent;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

class FileChainBlock extends AbstractFilePosition<ChainBlock> {
    private static final Logger logger = LoggerFactory.getLogger(FileChainBlock.class);
    private int magic;
    private Integer height;

    public class BlockWrite2fileEvent extends BitcoinEvent<FileChainBlock> {
        public BlockWrite2fileEvent() {
            super(FileChainBlock.this);
        }
    }

    public FileChainBlock(File file, long position, ChainBlock target) {
        super(file, position, target);
    }

    public FileChainBlock(File file, long position) {
        super(file, position);
    }

    @Override
    public ChainBlock deserialization(FileChannel channel) throws IOException {
        readBlock(channel);
        return this.target;
    }

    public ChainBlock readBlock(FileChannel channel) throws IOException {
        return this.readBlock(Unpooled.buffer(), channel);
    }

    public ChainBlock readBlock(ByteBuf buffer, FileChannel channel) throws IOException {
        try {
            tryReadBlock(buffer, channel);
            return this.target;
        } catch (RuntimeException ex) {
            int i = buffer.readerIndex();
            logger.error("Read block error : {}[{}] bytes-index {}", this.file.getAbsolutePath(), this.position, i);
            throw ex;
        }
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
        if (this.height != null) {
            // Assert.isTrue(this.height != null, "Not init `height` in FileChainBlock");
            this.target.header.setHeight(this.height);
        }
        logger.debug("Read File : {}, Position: {}", file.getName(), this.position);
    }

    public int getMagic() {
        return magic;
    }

    public FileChainBlock setMagic(int magic) {
        this.magic = magic;
        return this;
    }

    @Override
    public ChainBlock getTarget() {
        if (this.target == null) {
            super.getTarget();
            Assert.isTrue(this.height != null, "Height not init");
            this.target.header.setHeight(this.height);
        }
        return this.target;
    }

    public int getHeight() {
        if (this.target == null) {
            return height;
        } else {
            return this.target.getHeight();
        }
    }

    public FileChainBlock setHeight(int height) {
        this.height = height;
        if (target != null) {
            target.header.setHeight(height);
        }
        return this;
    }

    public byte[] serialization() {
        ByteBuf pool = Unpooled.buffer();
        pool.writeIntLE(this.target.getHeight());
        pool.writeIntLE((int) position);
        byte[] name = this.file.getName().getBytes(StandardCharsets.UTF_8);
        Assert.isTrue(name.length < 0xFF, "name length > 255");
        pool.writeByte(name.length);
        pool.writeBytes(name);
        return ByteUtil.readAll(pool);
    }

    public static FileChainBlock deserialization(File root, byte[] data) {
        ByteBuf pool = Unpooled.copiedBuffer(data);
        int height = pool.readIntLE();
        int position = pool.readIntLE();
        int len = pool.readByte();
        byte[] name = ByteUtil.readLength(pool, len);
        FileChainBlock fc = new FileChainBlock(new File(root, new String(name, StandardCharsets.UTF_8)), position);
        fc.height = height;
        return fc;
    }
}