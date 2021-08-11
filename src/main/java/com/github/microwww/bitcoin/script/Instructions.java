package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.script.ins.*;
import org.springframework.util.Assert;

public final class Instructions { // Instruction-set
    private final Instruction[] instructions = new Instruction[256];

    {
        int i = 0;
        for (Instruction_00_4B value : Instruction_00_4B.values()) {
            instructions[i] = value;
            i++;
        }
        Assert.isTrue(i == 0x4B + 1, "count 0x4B");
        for (Instruction_4C_50 value : Instruction_4C_50.values()) {
            instructions[i] = value;
            i++;
        }
        Assert.isTrue(i == 0x50 + 1, "count 0x4C");
        for (Instruction_51_60 value : Instruction_51_60.values()) {
            instructions[i] = value;
            i++;
        }
        Assert.isTrue(i == 0x60 + 1, "count 0x60");
        for (Instruction_61_6A value : Instruction_61_6A.values()) {
            instructions[i] = value;
            i++;
        }
        Assert.isTrue(i == 0x6A + 1, "count 0xAF");
        for (Instruction_6B_7D value : Instruction_6B_7D.values()) {
            instructions[i] = value;
            i++;
        }
        Assert.isTrue(i == 0x7D + 1, "count 0xAF");
        for (Instruction_7E_82 value : Instruction_7E_82.values()) {
            instructions[i] = value;
            i++;
        }
        Assert.isTrue(i == 0x82 + 1, "count 0xAF");
        for (Instruction_83_8A value : Instruction_83_8A.values()) {
            instructions[i] = value;
            i++;
        }
        Assert.isTrue(i == 0x8A + 1, "count 0xAF");
        for (Instruction_8B_A5 value : Instruction_8B_A5.values()) {
            instructions[i] = value;
            i++;
        }
        Assert.isTrue(i == 0xA5 + 1, "count 0xAF");
        for (Instruction_A6_AF value : Instruction_A6_AF.values()) {
            instructions[i] = value;
            i++;
        }
        Assert.isTrue(i == 0xAF + 1, "count 0xAF");
        for (Instruction_B0_B9 value : Instruction_B0_B9.values()) {
            instructions[i] = value;
            i++;
        }
        Assert.isTrue(i == 0xB9 + 1, "count 0xAF");
        for (Instruction_BA_FC value : Instruction_BA_FC.values()) {
            instructions[i] = value;
            i++;
        }
        Assert.isTrue(i == 0xFC + 1, "count 0xAF");
        for (Instruction_FD_FF value : Instruction_FD_FF.values()) {
            instructions[i] = value;
            i++;
        }
        Assert.isTrue(i == 0xFF + 1, "count 256");
    }

    public static final Instructions SET = new Instructions();

    private Instructions() {
    }

    public Instruction select(byte code) {
        return instructions[Byte.toUnsignedInt(code)];
    }

    public Instruction select(int code) {
        return instructions[code];
    }
}
