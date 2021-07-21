package com.github.microwww.bitcoin.math;

import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;

import java.math.BigInteger;

public class Uint256 extends BigInteger {

    public static final int LEN = 256 / 8;

    public Uint256(byte[] val) {
        super(1, val);
    }

    /**
     * 不支持有前导的负号
     *
     * @param val
     * @param radix
     */
    public Uint256(String val, int radix) {
        super(startError(val), radix);
    }

    /**
     * 不支持有前导的负号
     *
     * @param val
     * @return
     */
    private static String startError(String val) {
        if (val.length() > 1) {
            char c = val.charAt(0);
            if (c == '-') {
                throw new IllegalArgumentException("Not start with `-` ");
            }
        }
        return val;
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
        return ByteUtil.reverse(this.file256bit());
    }

    public static byte[] zero256() {
        return new byte[LEN];
    }

    public byte[] sha256sha256() {
        return dsha256(this.file256bit());
    }

    public byte[] sha256sha256(Uint256 in) {
        byte[] b3 = ByteUtil.concat(this.file256bit(), in.file256bit());
        return ByteUtil.sha256sha256(b3);
    }

    @Override
    public String toString() {
        return "0x" + toHex();
    }

    public static byte[] dsha256(byte[] val) {
        return ByteUtil.sha256sha256(val);
    }

    public static Uint256 read(ByteBuf bf) {
        return new Uint256(ByteUtil.readLength(bf, LEN));
    }

    public String toHex() {
        return toHex(false);
    }

    public String toHex(boolean reverse) {
        if (reverse) {
            return ByteUtil.hex(this.reverse256bit());
        }
        return ByteUtil.hex(this.file256bit());
    }
}
