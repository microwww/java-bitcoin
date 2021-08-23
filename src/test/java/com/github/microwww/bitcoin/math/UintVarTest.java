package com.github.microwww.bitcoin.math;

import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class UintVarTest {

    @Test
    void parseAndRead() {
        ByteBuf bf = Unpooled.buffer();
        bf.writeBytes(new byte[]{(byte) 0xfd, 0x40, 0x01});
        UintVar parse = UintVar.parse(bf);
        // 交易 3a5769fb2126d870aded5fcaced3bc49fa9768436101895931adb5246e41e957
        assertEquals(320, parse.intValue());
    }

    @Test
    void writeData() {
        ByteBuf bf = Unpooled.buffer();
        ByteBuf va = UintVar.writeData(bf, new byte[320]);

        byte[] bytes = ByteUtil.readAll(va);
        assertEquals(323, bytes.length);
        assertArrayEquals(new byte[]{(byte) 0xfd, 0x40, 0x01}, Arrays.copyOf(bytes, 3));
    }
}