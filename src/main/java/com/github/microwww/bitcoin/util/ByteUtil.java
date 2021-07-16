package com.github.microwww.bitcoin.util;

import io.netty.buffer.ByteBuf;
import org.springframework.util.Assert;

public class ByteUtil {

    public static byte[] readLength(ByteBuf buf, int len) {
        Assert.isTrue(buf.readableBytes() >= len, "not have data");
        byte[] bytes = new byte[len];
        if (len == 0) {
            return bytes;
        }
        buf.readBytes(bytes);
        return bytes;
    }
}
