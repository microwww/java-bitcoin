package com.github.microwww.bitcoin.math;

import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;

import java.math.BigInteger;

/**
 * 变长的整数 小端, 最大 8 byte
 */
public class UintVar extends BigInteger {

    public static final UintVar ZERO = new UintVar((byte) 0x0);

    private UintVar(byte... val) {
        super(1, val);
    }

    /**
     * java 只能接受整数个数据
     *
     * @param val
     */
    public static UintVar valueOf(int val) {
        return new UintVar(new byte[]{(byte) (val >>> 24), (byte) (val << 8 >>> 24), (byte) (val << 16 >>> 24), (byte) (val << 24 >>> 24)});
    }

    public static UintVar parse(ByteBuf bf) {
        byte b0 = bf.readByte();
        int v0 = Byte.toUnsignedInt(b0);
        if (v0 < 0xFD) {
            return UintVar.valueOf(v0);
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
        int i = UintVar.parse(bf).intValueExact();
        return ByteUtil.readLength(bf, i);
    }

    public static ByteBuf writeData(ByteBuf bf, byte[] data) {
        UintVar.valueOf(data.length).write(bf);
        bf.writeBytes(data);
        return bf;
    }

    public UintVar write(ByteBuf bf) {
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

    public int bytesLength() {
        long i = this.longValueExact();
        if (i < 0xFD) {
            return 1;
        } else if (i <= 0xFFFF) {
            return 3;
        } else if (i <= 0xFFFFFFFF) {
            return 5;
        } else {
            return 9;
        }
    }
}
