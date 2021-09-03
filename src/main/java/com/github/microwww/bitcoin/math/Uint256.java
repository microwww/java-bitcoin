package com.github.microwww.bitcoin.math;

import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;

import java.math.BigInteger;
import java.util.Arrays;

public class Uint256 extends BigInteger {

    public static final int LEN = 256 / 8;
    public static final Uint256 ZERO = new Uint256(new byte[]{0});
    public static final Uint256 MAX_VALUE;

    static {
        byte[] bytes = new byte[LEN];
        for (int i = 0; i < LEN; i++) {
            bytes[i] = (byte) 0xFF;
        }
        MAX_VALUE = new Uint256(bytes);
    }

    public Uint256(byte[] val) {
        super(1, val);
    }

    public Uint256(BigInteger big) {
        this(big.toByteArray());
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
    public byte[] fill256bit() {
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
        return ByteUtil.reverse(this.fill256bit());
    }

    public String toHexReverse256() {
        return ByteUtil.hex(ByteUtil.reverse(this.fill256bit()));
    }

    public static byte[] zero256() {
        return new byte[LEN];
    }

    // TODO 存储为属性 ?
    public byte[] sha256sha256() {
        return dsha256(this.fill256bit());
    }

    public byte[] sha256sha256(Uint256 in) {
        byte[] b3 = ByteUtil.concat(this.fill256bit(), in.fill256bit());
        return ByteUtil.sha256sha256(b3);
    }

    @Override
    public String toString() {
        return "0x" + toHexReverse256();
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
        return ByteUtil.hex(this.fill256bit());
    }

    public boolean equalsByte(byte[] x) {
        return Arrays.equals(x, this.fill256bit());
    }

    public boolean reverseEqual(byte[] x) {
        return Arrays.equals(x, this.reverse256bit());
    }
}
