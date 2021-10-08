package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.chain.BlockHeader;
import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.math.UintVar;
import com.github.microwww.bitcoin.provider.Peer;
import io.netty.buffer.ByteBuf;

public class Headers extends AbstractProtocolAdapter<Headers> {
    private BlockHeader[] chainBlocks;

    public Headers(Peer peer) {
        super(peer);
    }

    @Override
    protected Headers read0(ByteBuf buf) {
        int count = UintVar.parse(buf).intValueExact();
        BlockHeader[] blocks = new BlockHeader[count];
        for (int i = 0; i < count; i++) {
            blocks[i] = new ChainBlock().readHeader(buf).header;
        }
        this.chainBlocks = blocks;
        return this;
    }

    @Override
    protected void write0(ByteBuf buf) {
        // Assert.isTrue(chainBlocks.size() <= 0xFF, "TO long");
        if (chainBlocks.length == 0) {
            return;
        }
        UintVar.valueOf(chainBlocks.length).write(buf);
        for (BlockHeader header : chainBlocks) {
            new ChainBlock(header).serialization(buf, false);
        }
    }

    public BlockHeader[] getChainBlocks() {
        return chainBlocks;
    }

    public Headers setChainBlocks(BlockHeader[] chainBlocks) {
        this.chainBlocks = chainBlocks;
        return this;
    }
}
