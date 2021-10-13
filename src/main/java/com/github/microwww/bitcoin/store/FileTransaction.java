package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.math.Uint256;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

class FileTransaction extends AbstractFilePosition<RawTransaction> {

    private final int length;
    private final Uint256 block;

    public FileTransaction(File file, long position, int length, Uint256 block) {
        super(file, position);
        this.length = length;
        this.block = block;
    }

    public FileTransaction(File file, long position, int length, Uint256 block, RawTransaction target) {
        super(file, position, target);
        this.length = length;
        this.block = block;
    }

    @Override
    public RawTransaction deserialization(FileChannel channel) throws IOException {
        return readFileRawTransaction(channel);
    }

    public RawTransaction readFileRawTransaction(FileChannel fc) throws IOException {
        ByteBuf bf = Unpooled.buffer();
        super.readLength(fc, bf, this.length);
        RawTransaction rt = new RawTransaction();
        rt.deserialization(bf);
        rt.setBlockHash(this.block);
        return rt;
    }

    public int getLength() {
        return length;
    }

    public Uint256 getBlock() {
        return block;
    }
}