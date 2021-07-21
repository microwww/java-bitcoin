package com.github.microwww.bitcoin.math;

import com.github.microwww.bitcoin.util.ByteUtil;

/**
 * 主要是标识类
 */
public class Uint32 extends Number implements Comparable<Uint32> {

    private final long value;

    /**
     * 负数会导致一个大于最大整数的值
     *
     * @param value
     */
    public Uint32(int value) {
        this((long) value);
    }

    /**
     * 超过无符号最大整数, 会被直接删掉bit位
     *
     * @param value
     */
    public Uint32(long value) {
        this.value = value << 32 >>> 32;
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public float floatValue() {
        return longValue();
    }

    @Override
    public double doubleValue() {
        return longValue();
    }

    @Override
    public int compareTo(Uint32 o) {
        return Long.compare(this.value, o.value);
    }

    public String toHex() {
        return Long.toHexString(value);
    }

    @Override
    public String toString() {
        return "0x" + Long.toUnsignedString(value, 16);
    }
}
