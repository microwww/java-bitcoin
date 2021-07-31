package com.github.microwww.bitcoin.math;

import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;

import java.math.BigInteger;

/**
 * 变长的整数 小端, 最大 8 byte
 */
public class UintVar extends BigInteger {

    public static final UintVar ZERO = new UintVar((byte) 0x0);
    public static final int MAX_LENGTH = 9;

    public UintVar(byte... val) {
        super(ByteUtil.concat(new byte[]{0}, val));
    }

    /**
     * java 只能接受整数个数据
     *
     * @param val
     */
    public static UintVar valueOf(int val) {
        return new UintVar(new byte[]{(byte) (val >>> 24), (byte) (val << 8 >>> 24), (byte) (val << 16 >>> 24), (byte) (val << 24 >>> 24)});
    }

    public static UintVar reader(ByteBuf bf) {
        byte b0 = bf.readByte();
        long v0 = unsignedByte(b0);
        if (v0 < 0xFD) {
            return new UintVar(b0);
        }
        byte b1 = bf.readByte();
        byte b2 = bf.readByte();
        if (v0 == 0xFD) {
            return new UintVar(b2, b1);
        }
        byte b3 = bf.readByte();
        byte b4 = bf.readByte();
        if (v0 == 0xFE) {
            return new UintVar(b4, b3, b2, b1);
        }
        byte b5 = bf.readByte();
        byte b6 = bf.readByte();
        byte b7 = bf.readByte();
        byte b8 = bf.readByte();
        return new UintVar(b8, b7, b6, b5, b4, b3, b2, b1);
    }

    public static byte[] parseAndRead(ByteBuf bf) {
        int i = UintVar.reader(bf).intValueExact();
        return ByteUtil.readLength(bf, i);
    }

    public UintVar write(ByteBuf bf) {
        byte bts = this.byteValue();
        try {
            int i = this.intValueExact();
            if (i < 0xFD) {
                bf.writeByte(i);
            } else if (i <= 0xFFFF) {
                bf.writeByte(0xFD).writeShortLE(i);
            } else {
                bf.writeByte(0xFE).writeIntLE(i);
            }
        } catch (ArithmeticException ex) {
            long l = this.longValue();
            bf.writeByte(0xFF).writeLongLE(l);
        }
        return this;
    }

    public static long unsignedByte(long value) {
        return value << 56 >>> 56;
    }
}
