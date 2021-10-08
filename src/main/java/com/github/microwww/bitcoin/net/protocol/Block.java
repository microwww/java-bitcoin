package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.provider.Peer;
import io.netty.buffer.ByteBuf;
import org.springframework.util.Assert;

public class Block extends AbstractProtocolAdapter<Block> {

    private ChainBlock chainBlock;

    public Block(Peer peer) {
        super(peer);
    }

    @Override
    protected void write0(ByteBuf buf) {
        Assert.isTrue(chainBlock != null, "Not init chainBlock");
        chainBlock.serialization(buf);
    }

    @Override
    protected Block read0(ByteBuf buf) {
        chainBlock = new ChainBlock().readHeader(buf).readBody(buf);
        return this;
    }

    public ChainBlock getChainBlock() {
        return chainBlock;
    }

    public void setChainBlock(ChainBlock chainBlock) {
        this.chainBlock = chainBlock;
    }
}
