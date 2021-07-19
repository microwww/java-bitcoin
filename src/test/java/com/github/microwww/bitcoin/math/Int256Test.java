package com.github.microwww.bitcoin.math;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class Int256Test {

    @Test
    public void test() {
        byte[] bytes = new Int256(new byte[]{1, 2, 3, 4}).file256bit();
        assertEquals(32, bytes.length);
        assertEquals(64, HexUtil.encodeHexStr(bytes).length());
        bytes[0] = (byte) 255;
        byte[] big = ArrayUtil.addAll(bytes, new byte[]{(byte) 255, 0});
        System.out.println(HexUtil.encodeHexStr(big));
        System.out.println(HexUtil.encodeHexStr(big));
    }

}