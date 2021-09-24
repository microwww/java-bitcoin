package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.provider.Peer;
import io.netty.buffer.ByteBuf;

public class SendCmpct extends AbstractProtocolAdapter<SendCmpct> {
    private boolean status = false; // true,1: High Bandwidth, false,0: low bandwidth
    private int version = 1;

    public SendCmpct(Peer peer) {
        super(peer);
    }

    @Override
    protected SendCmpct read0(ByteBuf buf) {
        status = buf.readByte() != 0;
        version = buf.readIntLE();
        return this;
    }

    @Override
    protected void write0(ByteBuf buf) {
        buf.writeByte(status ? 1 : 0).writeIntLE(version);
    }

    public boolean isStatus() {
        return status;
    }

    public SendCmpct setStatus(boolean status) {
        this.status = status;
        return this;
    }

    public int getVersion() {
        return version;
    }

    public SendCmpct setVersion(int version) {
        this.version = version;
        return this;
    }

    @Override
    public String toString() {
        return "status=" + status + ", version=" + version;
    }
}
