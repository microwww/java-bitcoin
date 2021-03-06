package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.net.MessageHeader;
import com.github.microwww.bitcoin.net.NetProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public interface AbstractProtocol<T extends AbstractProtocol> {

    public default NetProtocol support() {
        return NetProtocol.select(this.getClass().getSimpleName().toLowerCase());
    }

    /**
     * 将数据写出
     *
     * @param buf
     */
    public abstract int write(ByteBuf buf);

    public abstract T read(byte[] buf);

    /**
     * 将头 和 数据一起写出
     *
     * @param buf
     */
    public MessageHeader writeWithHeader(ByteBuf buf);

}
