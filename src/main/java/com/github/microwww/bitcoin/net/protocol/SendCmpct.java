package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.provider.Peer;
import io.netty.buffer.ByteBuf;

/**
 * with Get data-type, {@link com.github.microwww.bitcoin.net.protocol.GetData.Type},
 */
public class SendCmpct extends AbstractProtocolAdapter<SendCmpct> {
    private boolean status = false;
    private Uint32 val = Uint32.ONE;

    public SendCmpct(Peer peer) {
        super(peer);
    }

    @Override
    protected SendCmpct read0(ByteBuf buf) {
        status = buf.readByte() == 1;
        val = new Uint32(buf.readIntLE());
        return this;
    }

    @Override
    protected void write0(ByteBuf buf) {
        buf.writeByte(status ? 1 : 0).writeIntLE(val.intValue());
    }

    public boolean isStatus() {
        return status;
    }

    public SendCmpct setStatus(boolean status) {
        this.status = status;
        return this;
    }

    public Uint32 getVal() {
        return val;
    }

    public SendCmpct setVal(Uint32 val) {
        this.val = val;
        return this;
    }

    @Override
    public String toString() {
        return "status=" + status + ", val=" + val;
    }
}
