package com.github.microwww.bitcoin.math;

import java.math.BigInteger;
import java.util.Objects;

public class Uint64 extends Number implements Comparable<Uint64> {

    private final long value;

    public Uint64(long value) {
        this.value = value;
    }

    public Uint64(byte[] bt) {
        value = new BigInteger(bt).longValue();
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
        return value;
    }

    public String toUnsignedString() {
        return Long.toUnsignedString(this.value);
    }

    public String toUnsignedString(int radix) {
        return Long.toUnsignedString(this.value, radix);
    }

    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Uint64 uint64 = (Uint64) o;
        return value == uint64.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public int compareTo(Uint64 o) {
        if (this.value == o.value) {
            return 0;
        }
        if (this.value < 0) {// 小于0的更大
            if (o.value > 0) {
                return 1;
            }
            return Long.compare(o.value, this.value); // 都是负数, 小的更大
        } else {
            if (o.value < 0) {
                return -1;
            }
            return Long.compare(this.value, o.value);
        }
    }

    public String toHex() {
        return "0x" + Long.toUnsignedString(value, 16);
    }
}
