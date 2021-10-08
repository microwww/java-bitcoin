package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.File;
import java.nio.charset.StandardCharsets;

class HeightBlock {
    private FileChainBlock fileChainBlock;
    private int height;

    public HeightBlock(FileChainBlock block, int height) {
        this.fileChainBlock = block;
        this.height = height;
    }

    // height + position + <name-len> + name
    public static HeightBlock deserializationLevelDB(File root, byte[] data) {
        ByteBuf pool = Unpooled.copiedBuffer(data);
        int height = pool.readIntLE();
        int position = pool.readIntLE();
        int len = pool.readByte();
        byte[] name = ByteUtil.readLength(pool, len);
        FileChainBlock f = new FileChainBlock(new File(root, new String(name, StandardCharsets.UTF_8)));
        f.setPosition(position);
        HeightBlock h = new HeightBlock(f, height);
        return h;
    }

    // height + position + <name-len> + name
    public byte[] serializationLevelDB() {
        ByteBuf pool = Unpooled.buffer(32); // 4 + 4 + 1 + 12
        byte[] name = this.fileChainBlock.getFile().getName().getBytes(StandardCharsets.UTF_8);
        pool.clear().writeIntLE(height).writeIntLE((int) this.fileChainBlock.getPosition()).writeByte(name.length).writeBytes(name);
        return ByteUtil.readAll(pool);
    }

    public FileChainBlock getFileChainBlock() {
        return fileChainBlock;
    }

    public ChainBlock getBlock() {
        return this.fileChainBlock.getBlock();
    }

    public void setFileChainBlock(FileChainBlock fileChainBlock) {
        this.fileChainBlock = fileChainBlock;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}