package com.github.microwww.bitcoin.util;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.digest.DigestUtil;
import io.netty.buffer.ByteBuf;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.springframework.util.Assert;

/**
 * 外部的工具方法放到一个地方, 方便之后更换
 */
public class ByteUtil {

    public static byte[] readAll(ByteBuf buf) {
        return readLength(buf, buf.readableBytes());
    }

    public static byte[] readLength(ByteBuf buf, int len) {
        Assert.isTrue(len >= 0, "length > 0 , But : " + len);
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

    public static byte[] sha256(byte[] val) {
        return DigestUtil.sha256(val);
    }

    public static String hex(byte[] val) {
        return HexUtil.encodeHexStr(val);
    }

    public static byte[] hex(String val) {
        return HexUtil.decodeHex(val);
    }

    public static String hexReverse(byte[] val) {
        return HexUtil.encodeHexStr(ArrayUtil.reverse(val));
    }

    public static byte[] sha256sha256(byte[]... val) {
        return sha256sha256(ArrayUtil.addAll(val));
    }

    public static byte[] reverse(byte[] bytes) {
        return ArrayUtil.reverse(bytes);
    }

    public static byte[] concat(byte[]... bytes) {
        return ArrayUtil.addAll(bytes);
    }

    public static byte[] sha256ripemd160(byte[] input) {
        return ripemd160(sha256(input));
    }

    public static byte[] ripemd160(byte[] data) {
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(data, 0, data.length);
        byte[] out = new byte[20];
        digest.doFinal(out, 0);
        return out;
    }
}
