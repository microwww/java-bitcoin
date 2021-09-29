package com.github.microwww.bitcoin.script.instruction;

public abstract class AbstractScriptOperand implements Script {
    protected byte[] operand = ZERO;
    protected final int code;

    public AbstractScriptOperand(int code) {
        this.code = code;
    }

    @Override
    public int opcode() {
        return code;
    }

    @Override
    public byte[] getOperand() {
        return operand;
    }

    public void setOperand(byte[] operand) {
        this.operand = operand;
    }

    @Override
    public String toString() {
        return ScriptNames.values()[code].name() + "|0x" + Integer.toUnsignedString(this.opcode(), 16);
    }
}
