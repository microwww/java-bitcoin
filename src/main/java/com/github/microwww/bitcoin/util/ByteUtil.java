package com.github.microwww.bitcoin.util;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.digest.DigestUtil;
import io.netty.buffer.ByteBuf;
import org.springframework.util.Assert;

public class ByteUtil {

    public static byte[] readAll(ByteBuf buf) {
        return readLength(buf, buf.readableBytes());
    }

    public static byte[] readLength(ByteBuf buf, int len) {
        Assert.isTrue(buf.readableBytes() >= len, "not have data");
        byte[] bytes = new byte[len];
        if (len == 0) {
            return bytes;
        }
        buf.readBytes(bytes);
        return bytes;
    }

    public static byte[] sha256sha256(byte[] val) {
        return DigestUtil.sha256(DigestUtil.sha256(val));
    }

    public static String hex(byte[] val) {
        return HexUtil.encodeHexStr(val);
    }

    public static String hexReverse(byte[] val) {
        return HexUtil.encodeHexStr(ArrayUtil.reverse(val));
    }

    public static byte[] sha256sha256(byte[]... val) {
        return sha256sha256(ArrayUtil.addAll(val));
    }
}
