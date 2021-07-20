package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.net.Peer;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public abstract class AbstractTypeHash<T extends AbstractTypeHash> extends AbstractProtocolAdapter<T> {
    private byte count;
    private int type;
    private Uint256 hash;

    public AbstractTypeHash(Peer peer) {
        super(peer);
    }

    @Override
    protected void write0(ByteBuf buf) {
        buf.writeByte(this.count);
        buf.writeIntLE(this.type);
        buf.writeBytes(this.hash.file256bit());
    }

    @Override
    public T read(byte[] buf) {
        ByteBuf bf = Unpooled.copiedBuffer(buf);
        this.count = bf.readByte();
        this.type = bf.readIntLE();
        this.hash = new Uint256(ByteUtil.readLength(bf, 32));
        return (T) this;
    }

    public byte getCount() {
        return count;
    }

    public AbstractTypeHash setCount(byte count) {
        this.count = count;
        return this;
    }

    public int getType() {
        return type;
    }

    public AbstractTypeHash setType(int type) {
        this.type = type;
        return this;
    }

    public Uint256 getHash() {
        return hash;
    }

    public AbstractTypeHash setHash(Uint256 hash) {
        this.hash = hash;
        return this;
    }
}
