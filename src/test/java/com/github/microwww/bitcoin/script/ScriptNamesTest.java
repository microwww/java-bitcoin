package com.github.microwww.bitcoin.script;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScriptNamesTest {

    @Test
    public void testIndex() {
        assertEquals(75, ScriptNames._75.ordinal());
        assertEquals(96, ScriptNames.OP_16.ordinal());
        assertEquals(115, ScriptNames.OP_IFDUP.ordinal());
        assertEquals(125, ScriptNames.OP_TUCK.ordinal());
        assertEquals(137, ScriptNames.OP_RESERVED1.ordinal());
        assertEquals(160, ScriptNames.OP_GREATERTHAN.ordinal());
        assertEquals(175, ScriptNames.OP_CHECKMULTISIGVERIFY.ordinal());
        assertEquals(186, ScriptNames._186.ordinal());
    }
}