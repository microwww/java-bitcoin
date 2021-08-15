package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class HeightBlock {
    private FileChainBlock block;
    private int height;

    public HeightBlock(FileChainBlock block, int height) {
        this.block = block;
        this.height = height;
    }

    // height + position + <name-len> + name
    public static HeightBlock unserializeLevelDB(File root, byte[] data) {
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
    public byte[] serializeLevelDB() {
        ByteBuf pool = Unpooled.buffer(32); // 4 + 4 + 1 + 12
        byte[] name = this.block.getFile().getName().getBytes(StandardCharsets.UTF_8);
        pool.clear().writeIntLE(height).writeIntLE((int) this.block.getPosition()).writeByte(name.length).writeBytes(name);
        return ByteUtil.readAll(pool);
    }

    public FileChainBlock getBlock() {
        return block;
    }

    public void setBlock(FileChainBlock block) {
        this.block = block;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}