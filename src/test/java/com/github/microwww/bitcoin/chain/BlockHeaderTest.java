package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlockHeaderTest {

    @Test
    void threshold() {
        BlockHeader h = new BlockHeader();
        h.setBits(new Uint32(0x1903a30c));
        assertEquals("3A30C00000000000000000000000000000000000000000000", h.threshold().toString(16).toUpperCase()); // 03A30C00000000000000000000000000000000000000000000
    }

    @Test
    void assertDifficulty() {
        // 000000000003ba27aa200b1cecaad478d2b00432346c3f1f3986da1afd33e506
        byte[] bytes = ByteUtil.hex("0100000050120119172a610421a6c3011dd330d9df07b63616c2cc1f1cd00200000000006657a9252aacd5c0b2940996ecff952228c3067cc38d4885efb5a4ac4247e9f337221b4d4c86041b0f2b571000");
        ByteBuf bf = Unpooled.copiedBuffer(bytes);
        ChainBlock ch = new ChainBlock().reset(bf);
        assertArrayEquals(ch.hash().reverse256bit(), ByteUtil.hex("000000000003ba27aa200b1cecaad478d2b00432346c3f1f3986da1afd33e506"));
        ch.header.assertDifficulty();
    }
}