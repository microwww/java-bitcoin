package com.github.microwww.bitcoin.script;

import io.netty.buffer.ByteBuf;

public enum Instruction_4C_60 implements Instruction {
    OP_PUSHDATA1,// 76
    OP_PUSHDATA2,
    OP_PUSHDATA4,
    OP_1NEGATE,
    OP_RESERVED,// 80
    OP_1,// OP_TRUE 81
    OP_2,
    OP_3,
    OP_4,
    OP_5,
    OP_6,
    OP_7,
    OP_8,
    OP_9,
    OP_10, // 90
    OP_11,
    OP_12,
    OP_13,
    OP_14,
    OP_15,
    OP_16, // 96
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
        return (byte) (0x4C + this.ordinal());
    }

}
