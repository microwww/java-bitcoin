package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.math.Uint8;
import com.github.microwww.bitcoin.net.Peer;
import io.netty.buffer.ByteBuf;
import org.springframework.util.Assert;

public class Headers extends AbstractProtocolAdapter<Headers> {
    private ChainBlock[] headers;

    public Headers(Peer peer) {
        super(peer);
    }

    @Override
    protected Headers read0(ByteBuf buf) {
        int count = new Uint8(buf.readByte()).intValue();
        ChainBlock[] blocks = new ChainBlock[count];
        for (int i = 0; i < count; i++) {
            blocks[i] = new ChainBlock().readHeader(buf);
        }
        this.headers = blocks;
        return this;
    }

    @Override
    protected void write0(ByteBuf buf) {
        Assert.isTrue(headers.length <= 0xFF, "TO long");
        buf.writeByte(headers.length);
        for (ChainBlock header : headers) {
            header.writeHeader(buf);
        }
    }

    public ChainBlock[] getHeaders() {
        return headers;
    }

    public void setHeaders(ChainBlock[] headers) {
        this.headers = headers;
    }
}
