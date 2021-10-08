package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.RawTransaction;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileTransaction {
    private RawTransaction transaction;
    private File file;
    private long position;
    private int length;

    public FileTransaction(RawTransaction transaction) {
        this.transaction = transaction;
    }

    public FileTransaction(File file) {
        this.file = file;
    }

    public FileTransaction setTransaction(RawTransaction transaction) {
        this.transaction = transaction;
        return this;
    }

    public File getFile() {
        return file;
    }

    public FileTransaction setFile(File file) {
        this.file = file;
        return this;
    }

    public RawTransaction getTransaction() {
        return transaction;
    }

    public long getPosition() {
        return position;
    }

    public FileTransaction setPosition(long position) {
        this.position = position;
        return this;
    }

    public int getLength() {
        return length;
    }

    public FileTransaction setLength(int length) {
        this.length = length;
        return this;
    }

    public RawTransaction readFileRawTransaction() throws IOException {
        try (
                FileChannel fc = new RandomAccessFile(this.file, "r").getChannel()
        ) {
            return readFileRawTransaction(fc);
        }
    }

    public RawTransaction readFileRawTransaction(FileChannel fc) throws IOException {
        ByteBuf bf = Unpooled.buffer();
        fc.position(this.position);
        ByteBuffer bb = ByteBuffer.allocate(1024 * 10);
        for (int i = 0; i < this.getLength(); i++) {
            bb.clear();
            int read = fc.read(bb);
            bb.rewind();
            bf.writeBytes(bb);
            i += read; // read enough
        }
        RawTransaction rt = new RawTransaction();
        rt.read(bf);
        return rt;
    }
}