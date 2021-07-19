package com.github.microwww.bitcoin.math;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.digest.DigestUtil;

import java.math.BigInteger;

public class Int256 extends BigInteger {

    public static final int LEN = 256 / 8;

    public Int256(byte[] val) {
        super(val);
    }

    public Int256(String val, int radix) {
        super(val, radix);
    }

    public Int256(String val) {
        super(val);
    }

    public static Int256 uint256(BigInteger val) {
        return new Int256(val.abs().toByteArray());
    }

    public static Int256 uint256(byte[] bytes) {
        return new Int256(new BigInteger(bytes).abs().toByteArray());
    }

    public static Int256 uint256(String var, int radis) {
        return new Int256(new BigInteger(var, radis).abs().toByteArray());
    }

    /**
     * 大端存储, 有时候需要反转为小端, 超出会被截断
     *
     * @return
     */
    public byte[] file256bit() {
        byte[] bt = super.toByteArray();
        int length = LEN;
        byte[] res = new byte[length];
        int det = bt.length - length;
        if (det >= 0) {
            System.arraycopy(bt, det, res, 0, length);
        } else {
            System.arraycopy(bt, 0, res, 0 - det, bt.length);
        }
        return res;
    }

    public byte[] reverse256bit() {
        return ArrayUtil.reverse(this.file256bit());
    }

    public static byte[] zero256() {
        return new byte[LEN];
    }

    public byte[] sha256sha256() {
        return dsha256(this.file256bit());
    }

    public byte[] sha256sha256(Int256 in) {
        byte[] b3 = ArrayUtil.addAll(this.file256bit(), in.file256bit());
        return Hash.sha256sha256(b3);
    }

    @Override
    public String toString() {
        return HexUtil.encodeHexStr(this.file256bit());
    }

    public static byte[] dsha256(byte[] val) {
        return DigestUtil.sha256(DigestUtil.sha256(val));
    }
}
