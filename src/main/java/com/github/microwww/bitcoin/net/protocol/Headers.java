package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.math.Uint8;
import com.github.microwww.bitcoin.net.Peer;
import io.netty.buffer.ByteBuf;
import org.springframework.util.Assert;

public class Headers extends AbstractProtocolAdapter<Headers> {
    private ChainBlock[] chainBlocks;

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
        this.chainBlocks = blocks;
        return this;
    }

    @Override
    protected void write0(ByteBuf buf) {
        Assert.isTrue(chainBlocks.length <= 0xFF, "TO long");
        buf.writeByte(chainBlocks.length);
        for (ChainBlock header : chainBlocks) {
            header.writeHeader(buf);
        }
    }

    public ChainBlock[] getChainBlocks() {
        return chainBlocks;
    }

    public void setChainBlocks(ChainBlock[] chainBlocks) {
        this.chainBlocks = chainBlocks;
    }
}
