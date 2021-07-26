package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class HeightChainBlock {
    private ChainBlock block;
    private int height;

    public HeightChainBlock(ChainBlock block, int height) {
        this.block = block;
        this.height = height;
    }

    public HeightChainBlock() {
    }

    public byte[] serialization() {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(height);
        this.block.writeHeader(buffer).writeTxCount(buffer).writeTxBody(buffer);
        return ByteUtil.readAll(buffer);
    }

    public HeightChainBlock deserialization(byte[] bytes) {
        ByteBuf buffer = Unpooled.copiedBuffer(bytes);
        this.height = buffer.readInt();
        this.block = new ChainBlock();
        this.block.readHeader(buffer).readBody(buffer);
        return this;
    }

    public ChainBlock getBlock() {
        return block;
    }

    public int getHeight() {
        return height;
    }
}