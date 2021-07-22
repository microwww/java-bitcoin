package com.github.microwww.bitcoin.math;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;


public class Int256Test {

    @Test
    public void test() {
        byte[] bytes = new Uint256(new byte[]{1, 2, 3, 4}).fill256bit();
        assertEquals(32, bytes.length);
        assertEquals(64, HexUtil.encodeHexStr(bytes).length());
        bytes[0] = (byte) 255;
        byte[] big = ArrayUtil.addAll(bytes, new byte[]{(byte) 255, 0});
        System.out.println(HexUtil.encodeHexStr(big));
        System.out.println(HexUtil.encodeHexStr(big));
    }

    @Test
    public void testLong() {
        BigInteger big = BigInteger.valueOf(Integer.MAX_VALUE);
        System.out.println(big.toString(16));
        BigInteger bg = BigInteger.valueOf(Integer.valueOf(Integer.MAX_VALUE + 1));
        System.out.println(bg.abs().toString(16));

        System.out.println(Integer.toUnsignedString(Integer.MAX_VALUE + 1, 16));

        long v = Integer.valueOf(Integer.MAX_VALUE + 1).intValue();
        v = v << 32 >>> 32;
        assertEquals("80000000", Long.toUnsignedString(v, 16));
    }
}