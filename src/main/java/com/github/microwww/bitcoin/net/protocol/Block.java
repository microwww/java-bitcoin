package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.block.BlockHeader;
import com.github.microwww.bitcoin.net.Peer;
import io.netty.buffer.ByteBuf;

public class Block extends AbstractProtocolAdapter<Block> {

    private BlockHeader blockHeader;

    public Block(Peer peer) {
        super(peer);
    }

    @Override
    protected void write0(ByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Block read0(ByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    public BlockHeader getBlockHeader() {
        return blockHeader;
    }

    public void setBlockHeader(BlockHeader blockHeader) {
        this.blockHeader = blockHeader;
    }
}
