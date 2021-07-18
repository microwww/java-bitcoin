package com.github.microwww.bitcoin.math;

import cn.hutool.crypto.digest.DigestUtil;

public class Hash {

    public static byte[] sha256sha256(byte[] val) {
        return DigestUtil.sha256(DigestUtil.sha256(val));
    }
}
