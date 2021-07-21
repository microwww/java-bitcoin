package com.github.microwww.bitcoin.math;

/**
 * 主要是标识类
 */
public class Uint16 extends Number implements Comparable<Uint16> {

    private final long value;

    /**
     * 负数会导致一个大于最大 short 的值
     *
     * @param value
     */
    public Uint16(short value) {
        this((long) value);
    }

    /**
     * 会被截断为无符号的最大整数
     *
     * @param value
     */
    public Uint16(long value) {
        this.value = value << 48 >>> 48;
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
    public int compareTo(Uint16 o) {
        return Long.compare(this.value, o.value);
    }

    @Override
    public String toString() {
        return "0x" + Long.toUnsignedString(value, 16);
    }
}
