package com.github.microwww.bitcoin.script.instruction;

import io.netty.buffer.ByteBuf;

public abstract class AbstractScriptNoOperand implements Script {
    protected final int code;

    public AbstractScriptNoOperand(int code) {
        this.code = code;
    }

    @Override
    public void operand(ByteBuf bf) {
    }

    @Override
    public int opcode() {
        return code;
    }

    @Override
    public String toString() {
        return ScriptNames.values()[code].name() + "|0x" + Integer.toUnsignedString(this.opcode(), 16);
    }
}
