package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.net.Peer;
import io.netty.buffer.ByteBuf;

public class Inv extends AbstractProtocolAdapter<Inv> {
    private final GetData data;

    public Inv(Peer peer) {
        super(peer);
        data = new GetData(peer);
    }

    @Override
    protected Inv read0(ByteBuf buf) {
        this.data.read0(buf);
        return this;
    }

    public GetData.Message[] getData() {
        return data.getMessages();
    }

    public Inv setData(GetData.Message[] msg) {
        data.setMessages(msg);
        return this;
    }

    public void validity() {
        data.validity(this);
    }
}
