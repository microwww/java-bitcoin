package com.github.microwww.bitcoin.math;

public class Uint64 extends Number implements Comparable<Uint64> {

    private final long value;

    public Uint64(long value) {
        this.value = value;
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
}
