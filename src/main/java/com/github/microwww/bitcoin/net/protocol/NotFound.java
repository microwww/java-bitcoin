package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.net.Peer;
import io.netty.buffer.ByteBuf;

/**
 * notfound is a response to a getdata,
 * sent if any requested data items could not be relayed,
 * for example, because the requested transaction was not in the memory pool or relay set.
 */
public class NotFound extends AbstractTypeHash<NotFound> {
    private GetData data;

    public NotFound(Peer peer) {
        super(peer);
    }

    @Override
    protected void write0(ByteBuf buf) {
        data.write0(buf);
    }

    @Override
    protected NotFound read0(ByteBuf buf) {
        // nothing to do
        return super.read0(buf);
    }

    public GetData getData() {
        return data;
    }

    public void setData(GetData data) {
        this.data = data;
    }
}
