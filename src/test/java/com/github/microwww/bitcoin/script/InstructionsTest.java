package com.github.microwww.bitcoin.script;

import org.junit.jupiter.api.Test;

import static com.github.microwww.bitcoin.script.ins.Instruction_83_8A.OP_RESERVED1;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InstructionsTest {

    @Test
    public void testSelect() {
        assertEquals(Instructions.SET.select(75).toString(), "_75");
        assertEquals(Instructions.SET.select(96).toString(), "OP_16");
        assertEquals(Instructions.SET.select(115).toString(), "OP_IFDUP");
        assertEquals(Instructions.SET.select(125).toString(), "OP_TUCK");
        assertEquals(Instructions.SET.select(137), OP_RESERVED1);
        assertEquals(Instructions.SET.select(160).toString(), "OP_GREATERTHAN");
        assertEquals(Instructions.SET.select(175).toString(), "OP_CHECKMULTISIGVERIFY");
        assertEquals(Instructions.SET.select(186).toString(), "_186");
    }
}