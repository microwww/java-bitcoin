package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.net.protocol.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlockHeaderTest {

    @Test
    void threshold() {
        BlockHeader h = new BlockHeader();
        h.setBits(new Uint32(0x1903a30c));
        assertEquals("3A30C00000000000000000000000000000000000000000000", h.threshold().toString(16).toUpperCase()); // 03A30C00000000000000000000000000000000000000000000
    }
}