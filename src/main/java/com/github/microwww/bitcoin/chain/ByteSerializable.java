package com.github.microwww.bitcoin.chain;

import io.netty.buffer.ByteBuf;

import java.io.Serializable;

public interface ByteSerializable extends Serializable {

    public ByteBuf serialization(ByteBuf buffer);

    public ByteBuf deserialization(ByteBuf buffer);
}
