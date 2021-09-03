package com.github.microwww.bitcoin.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class Uint32Test {

    @Test
    void toBytes() {
        assertArrayEquals(new byte[]{0, 0, 0, 0}, new Uint32(0).toBytes());
        assertArrayEquals(new byte[]{0, 0, 1, 1}, new Uint32(0x0101).toBytes());
        assertArrayEquals(new byte[]{5, 5, 1, 1}, new Uint32(0x05050101).toBytes());
    }

    @Test
    void toBytesLE() {
        assertArrayEquals(new byte[]{0, 0, 0, 0}, new Uint32(0).toBytesLE());
        assertArrayEquals(new byte[]{1, 2, 3, 4}, new Uint32(0x04030201).toBytesLE());
        assertArrayEquals(new byte[]{1, (byte) 0xAC, 0, 0}, new Uint32(0x0000AC01).toBytesLE());
    }
}