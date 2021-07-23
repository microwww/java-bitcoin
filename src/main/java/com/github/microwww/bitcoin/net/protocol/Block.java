package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.chain.BlockChainContext;
import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.net.Peer;
import io.netty.buffer.ByteBuf;

public class Block extends AbstractProtocolAdapter<Block> {

    private final ChainBlock chainBlock;

    public Block(Peer peer) {
        super(peer);
        chainBlock = new ChainBlock();
    }

    @Override
    protected void write0(ByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Block read0(ByteBuf buf) {
        chainBlock.readHeader(buf).readBody(buf);
        return this;
    }

    public ChainBlock getChainBlock() {
        return chainBlock;
    }
}
