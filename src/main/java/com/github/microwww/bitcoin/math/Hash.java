package com.github.microwww.bitcoin.math;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.digest.DigestUtil;

public class Hash {
    private Hash() {
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
