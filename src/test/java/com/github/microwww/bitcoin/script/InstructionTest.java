package com.github.microwww.bitcoin.script;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InstructionTest {

    @Test
    void opcode() {
        assertEquals(Instructions.SET.select(75).opcode(), 75);
        assertEquals(Instructions.SET.select(96).opcode(), 96);
        assertEquals(Instructions.SET.select(115).opcode(), 115);
        assertEquals(Instructions.SET.select(125).opcode(), 125);
        assertEquals(Instructions.SET.select(137).opcode(), (byte) 137);
        assertEquals(Instructions.SET.select(160).opcode(), (byte) 160);
        assertEquals(Instructions.SET.select(175).opcode(), (byte) 175);
        assertEquals(Instructions.SET.select(186).opcode(), (byte) 186);
    }
}