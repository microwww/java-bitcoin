package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.provider.Peer;
import io.netty.buffer.ByteBuf;

/**
 * notfound is a response to a getdata,
 * sent if any requested data items could not be relayed,
 * for example, because the requested transaction was not in the memory pool or relay set.
 */
public class NotFound extends AbstractProtocolAdapter<NotFound> {
    private final GetData data;

    public NotFound(Peer peer) {
        super(peer);
        this.data = new GetData(peer);
    }

    @Override
    protected void write0(ByteBuf buf) {
        data.write0(buf);
    }

    @Override
    protected NotFound read0(ByteBuf buf) {
        this.data.read0(buf);
        return this;
    }

    public GetData.Message[] getData() {
        return data.getMessages();
    }

    public void setData(GetData.Message[] data) {
        this.data.setMessages(data);
    }
}
