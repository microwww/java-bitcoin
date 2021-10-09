package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.RawTransaction;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

class FileTransaction extends AbstractFilePosition<RawTransaction> {

    private int length;

    public FileTransaction(File file, long position, int length) {
        super(file, position);
        this.length = length;
    }

    public FileTransaction(File file, RawTransaction target) {
        super(file, target);
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
        return rt;
    }

    public int getLength() {
        return length;
    }

    public FileTransaction setLength(int length) {
        this.length = length;
        return this;
    }
}