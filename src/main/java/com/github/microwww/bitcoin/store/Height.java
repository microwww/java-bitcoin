package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class Height {
    private Uint256 hash;
    private int height;

    public Height() {
    }

    public Height(Uint256 hash, int height) {
        this.hash = hash;
        this.height = height;
    }

    public Uint256 getHash() {
        return hash;
    }

    public Height setHash(Uint256 hash) {
        this.hash = hash;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public Height setHeight(int height) {
        this.height = height;
        return this;
    }

    public byte[] serialization() {
        ByteBuf pool = Unpooled.buffer(4 + 32); // 4 + 32
        pool.writeIntLE(this.getHeight()).writeBytes(this.getHash().fill256bit());
        return ByteUtil.readAll(pool);
    }

    public static Height deserialization(byte[] bytes) {
        ByteBuf pool = Unpooled.copiedBuffer(bytes); // 4 + 32
        int h = pool.readIntLE();//(block.getHeight()).writeBytes(block.getChainBlock().hash().reverse256bit());
        byte[] hash = ByteUtil.readLength(pool, 32);
        return new Height(new Uint256(hash), h);
    }

    @Override
    public String toString() {
        return "height = " + height + ", hash = " + hash;
    }
}