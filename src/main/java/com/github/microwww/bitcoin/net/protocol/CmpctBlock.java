package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.net.Peer;
import io.netty.buffer.ByteBuf;

/**
 * 挖矿生成块会发送该消息
 */
public class CmpctBlock extends AbstractProtocolAdapter<CmpctBlock> {

    private final ChainBlock chainBlock;

    public CmpctBlock(Peer peer) {
        super(peer);
        chainBlock = new ChainBlock();
    }

    @Override
    protected void write0(ByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected CmpctBlock read0(ByteBuf buf) {
        chainBlock.readHeader(buf).readBody(buf);
        return this;
    }

    public ChainBlock getChainBlock() {
        return chainBlock;
    }
}
