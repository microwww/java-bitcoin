package com.github.microwww.bitcoin.provider;

import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DiskBlockTest {

    @Test
    public void testPatten() {
        Matcher matcher = DiskBlock.pt.matcher("blk00001.dat");
        assertTrue(matcher.matches());
        String group = matcher.group(1);
        assertEquals("1", group);
    }

}