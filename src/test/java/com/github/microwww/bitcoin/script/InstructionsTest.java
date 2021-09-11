package com.github.microwww.bitcoin.script;

import org.junit.jupiter.api.Test;

import static com.github.microwww.bitcoin.script.ins.Instruction_00_4B._75;
import static com.github.microwww.bitcoin.script.ins.Instruction_4C_50.OP_RESERVED;
import static com.github.microwww.bitcoin.script.ins.Instruction_51_60.OP_16;
import static com.github.microwww.bitcoin.script.ins.Instruction_61_6A.OP_VERNOTIF;
import static com.github.microwww.bitcoin.script.ins.Instruction_6B_7D.OP_IFDUP;
import static com.github.microwww.bitcoin.script.ins.Instruction_6B_7D.OP_TUCK;
import static com.github.microwww.bitcoin.script.ins.Instruction_7E_82.OP_SUBSTR;
import static com.github.microwww.bitcoin.script.ins.Instruction_83_8A.OP_RESERVED1;
import static com.github.microwww.bitcoin.script.ins.Instruction_8B_A5.OP_GREATERTHAN;
import static com.github.microwww.bitcoin.script.ins.Instruction_A6_AF.OP_CHECKMULTISIGVERIFY;
import static com.github.microwww.bitcoin.script.ins.Instruction_B0_B9.OP_CHECKSEQUENCEVERIFY;
import static com.github.microwww.bitcoin.script.ins.Instruction_BA_FC._186;
import static com.github.microwww.bitcoin.script.ins.Instruction_FD_FF.OP_INVALIDOPCODE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InstructionsTest {

    @Test
    public void testSelect() {
        assertEquals(Instructions.SET.select(75), _75);
        assertEquals(Instructions.SET.select(80), OP_RESERVED);
        assertEquals(Instructions.SET.select(96), OP_16);
        assertEquals(Instructions.SET.select(102), OP_VERNOTIF);
        assertEquals(Instructions.SET.select(115), OP_IFDUP);
        assertEquals(Instructions.SET.select(125), OP_TUCK);
        assertEquals(Instructions.SET.select(127), OP_SUBSTR);
        assertEquals(Instructions.SET.select(137), OP_RESERVED1);
        assertEquals(Instructions.SET.select(160), OP_GREATERTHAN);
        assertEquals(Instructions.SET.select(175), OP_CHECKMULTISIGVERIFY);
        assertEquals(Instructions.SET.select(178), OP_CHECKSEQUENCEVERIFY);
        assertEquals(Instructions.SET.select(186), _186);
        assertEquals(Instructions.SET.select(255), OP_INVALIDOPCODE);
    }

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