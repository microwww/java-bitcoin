package com.github.microwww.bitcoin.net.protocol;

import io.netty.buffer.ByteBuf;

public class GetHeaders extends ProtocolAdapter {
    int version;
    @Override
    public int write(ByteBuf buf) {
        return super.write(buf);
    }
}
