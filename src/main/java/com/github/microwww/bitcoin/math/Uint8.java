package com.github.microwww.bitcoin.math;

import java.util.Objects;

/**
 * 主要是标识类
 */
public class Uint8 extends Number implements Comparable<Uint8> {

    public static final Uint8 MAX_VALUE = new Uint8(0xFF);
    public static final Uint8 ZERO = new Uint8(0x0);
    private final long value;

    /**
     * 负数会导致一个大于最大 short 的值
     *
     * @param value
     */
    public Uint8(byte value) {
        this((long) value);
    }

    /**
     * 会被截断为无符号的最大整数
     *
     * @param value
     */
    public Uint8(long value) {
        this.value = value << 56 >>> 56;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Uint8 uint8 = (Uint8) o;
        return value == uint8.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public int compareTo(Uint8 o) {
        return Long.compare(this.value, o.value);
    }

    public static void assertion(int v) {
        if (v > 0xFF || v < 0) {
            throw new IllegalArgumentException("0 <= Uint8 < 256");
        }
    }

    @Override
    public String toString() {
        return "0x" + Long.toUnsignedString(value, 16);
    }
}
