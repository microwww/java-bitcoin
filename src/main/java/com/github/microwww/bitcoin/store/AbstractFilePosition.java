package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.ByteSerializable;
import io.netty.buffer.ByteBuf;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Optional;

public abstract class AbstractFilePosition<T extends ByteSerializable> {
    protected final File file;
    protected T target;
    protected long position;

    public AbstractFilePosition(File file, long position) {
        this.file = file;
        this.position = position;
    }

    public AbstractFilePosition(File file, T target) {
        this.file = file;
        this.target = target;
    }

    public Optional<T> getTarget() {
        return Optional.ofNullable(target);
    }

    public T target() {
        return target;
    }

    public T load() {
        return load(false);
    }

    public File getFile() {
        return file;
    }

    public long getPosition() {
        return position;
    }

    public T load(boolean force) {
        if (target == null || force) {
            try (FileChannel channel = new RandomAccessFile(file, "r").getChannel()) {
                channel.position(this.position);
                return deserialization(channel);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return target;
    }

    public abstract T deserialization(FileChannel channel) throws IOException;

    public void readLength(FileChannel channel, ByteBuf buf, int length) throws IOException {
        ByteBuffer f = ByteBuffer.allocate(1 * 1024 * 1024);
        int read = buf.readableBytes();
        while (read < length) {
            f.clear();
            int r = channel.read(f);
            if (r < 0) {
                throw new IOException("EOF error !");
            }
            read += r;
            f.rewind();
            buf.writeBytes(f);
        }
    }
}
