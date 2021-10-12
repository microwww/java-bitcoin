package com.github.microwww.bitcoin.store;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccessBlockFileTest {

    @Test
    public void parseIndex() {
        int i = AccessBlockFile.parseIndex("blk0001.dat");
        assertEquals(1, i);
        i = AccessBlockFile.parseIndex("blk0200.dat");
        assertEquals(200, i);
        i = AccessBlockFile.parseIndex("blk99999.dat");
        assertEquals(99999, i);
        i = AccessBlockFile.parseIndex("blk00000.dat");
        assertEquals(0, i);
    }
}