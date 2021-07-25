package com.github.microwww.bitcoin.math;

import io.netty.buffer.Unpooled;

import java.util.Objects;

/**
 * 主要是标识类
 */
public class Uint32 extends Number implements Comparable<Uint32> {

    public static final Uint32 MAX_VALUE = new Uint32(0xFFFFFFFF);
    public static final Uint32 ZERO = new Uint32(0x0);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Uint32 uint32 = (Uint32) o;
        return value == uint32.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public String toHex() {
        return Long.toHexString(value);
    }

    public byte[] toBytes() {
        return new byte[]{(byte) (value >> 24), (byte) (value & 0x00ff0000 >> 16), (byte) (value & 0X0000FF00 >> 8), (byte) (value & 0X000000FF)};
    }

    public byte[] toBytesLE() {
        return new byte[]{(byte) (value & 0X000000FF), (byte) (value & 0X0000FF00 >> 8), (byte) (value & 0x00ff0000 >> 16), (byte) (value >> 24)};
    }

    @Override
    public String toString() {
        return "0x" + Long.toUnsignedString(value, 16);
    }
}
