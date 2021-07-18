package com.github.microwww.bitcoin.math;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;

import java.math.BigInteger;

public class Int256 {
    public static final int LEN = 32;
    private final byte[] bytes;

    public Int256(String val, int radix) {
        this(new BigInteger(val, radix));
    }

    public Int256(byte[] val) {
        this(new BigInteger(val));
    }

    public Int256(BigInteger val) {
        bytes = to256bit(val);
    }

    public byte[] reverse() {
        return ArrayUtil.reverse(bytes);
    }

    public static byte[] to256bit(BigInteger val) {
        byte[] bt = val.toByteArray();
        if (bt.length < LEN) {
            byte[] i = new byte[LEN];
            System.arraycopy(bt, 0, i, LEN - bt.length, bt.length);
            return i;
        } else if (bt.length > LEN) {
            byte[] i = new byte[LEN];
            System.arraycopy(bt, 0, i, 0, LEN);
        }
        return bt;
    }

    public static byte[] getZero() {
        return new byte[LEN];
    }

    public Int256 sha256sha256() {
        return new Int256(Hash.sha256sha256(this.bytes));
    }

    public Int256 sha256sha256(Int256 in) {
        byte[] b3 = ArrayUtil.addAll(this.bytes, in.bytes);
        return new Int256(Hash.sha256sha256(b3));
    }

    @Override
    public String toString() {
        return HexUtil.encodeHexStr(this.bytes);
    }
}
