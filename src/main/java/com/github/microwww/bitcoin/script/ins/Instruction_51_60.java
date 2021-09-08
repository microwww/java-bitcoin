package com.github.microwww.bitcoin.script.ins;

import com.github.microwww.bitcoin.script.Instruction;
import com.github.microwww.bitcoin.script.Interpreter;
import com.github.microwww.bitcoin.script.ScriptOperation;
import io.netty.buffer.ByteBuf;

public enum Instruction_51_60 implements Instruction {
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
        int size = this.opcode() & 0x0F;
        return new ScriptOperation(this, new byte[]{(byte) size});
    }

    @Override
    public void exec(Interpreter executor, Object data) {
        executor.stack.push((byte[]) data);
    }

    @Override
    public byte opcode() {
        return (byte) (0x51 + this.ordinal());
    }

}
