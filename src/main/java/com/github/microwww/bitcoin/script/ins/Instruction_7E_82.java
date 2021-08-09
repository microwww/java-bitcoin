package com.github.microwww.bitcoin.script.ins;

import com.github.microwww.bitcoin.script.Instruction;
import com.github.microwww.bitcoin.script.Interpreter;
import com.github.microwww.bitcoin.script.ScriptOperation;
import io.netty.buffer.ByteBuf;

public enum Instruction_7E_82 implements Instruction {

    // splice ops
    OP_CAT, // 126
    OP_SUBSTR,
    OP_LEFT,
    OP_RIGHT,
    OP_SIZE,// 130
    ;

    @Override
    public ScriptOperation compile(ByteBuf bf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void exec(Interpreter executor, Object data) {
        throw new UnsupportedOperationException();
    }
    public byte opcode() {
        return (byte) (0x7E + this.ordinal());
    }

}
