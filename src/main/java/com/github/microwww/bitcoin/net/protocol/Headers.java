package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.math.Uint8;
import com.github.microwww.bitcoin.math.UintVar;
import com.github.microwww.bitcoin.net.Peer;
import io.netty.buffer.ByteBuf;
import org.springframework.util.Assert;

import java.util.List;

public class Headers extends AbstractProtocolAdapter<Headers> {
    private ChainBlock[] chainBlocks;

    public Headers(Peer peer) {
        super(peer);
    }

    @Override
    protected Headers read0(ByteBuf buf) {
        int count = UintVar.reader(buf).intValueExact();
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
        UintVar.valueOf(chainBlocks.length).write(buf);
        buf.writeByte(chainBlocks.length);
        for (ChainBlock header : chainBlocks) {
            header.writeHeader(buf);
        }
    }

    public ChainBlock[] getChainBlocks() {
        return chainBlocks;
    }

    public Headers setChainBlocks(ChainBlock[] chainBlocks) {
        this.chainBlocks = chainBlocks;
        return this;
    }

    public Headers setChainBlocks(List<ChainBlock> chainBlocks) {
        ChainBlock[] cbs = new ChainBlock[chainBlocks.size()];
        for (int i = 0; i < cbs.length; i++) {
            cbs[i] = chainBlocks.get(i);
        }
        this.chainBlocks = cbs;
        return this;
    }
}
