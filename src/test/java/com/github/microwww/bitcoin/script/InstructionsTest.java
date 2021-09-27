package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.script.instruction.ScriptNames;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InstructionsTest {

    @Test
    public void scriptName() {
        assertEquals(ScriptNames.values()[75].name(), "_75");
        assertEquals(ScriptNames.values()[96].name(), "OP_16");
        assertEquals(ScriptNames.values()[115].name(), "OP_IFDUP");
        assertEquals(ScriptNames.values()[125].name(), "OP_TUCK");
        assertEquals(ScriptNames.values()[137], ScriptNames.OP_RESERVED1);
        assertEquals(ScriptNames.values()[160].name(), "OP_GREATERTHAN");
        assertEquals(ScriptNames.values()[175].name(), "OP_CHECKMULTISIGVERIFY");
        assertEquals(ScriptNames.values()[186].name(), "_186");
    }
}