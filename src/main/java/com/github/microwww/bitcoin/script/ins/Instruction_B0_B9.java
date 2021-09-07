package com.github.microwww.bitcoin.script.ins;

import com.github.microwww.bitcoin.script.Instruction;
import com.github.microwww.bitcoin.script.Interpreter;
import com.github.microwww.bitcoin.script.ScriptOperation;
import io.netty.buffer.ByteBuf;

public enum Instruction_B0_B9 implements Instruction {

    // expansion
    OP_NOP1,
    OP_CHECKLOCKTIMEVERIFY { // OP_NOP2

        @Override
        public ScriptOperation compile(ByteBuf bf) {
            return new ScriptOperation(this, ZERO);
        }

        @Override
        public void exec(Interpreter executor, Object data) {
            byte[] peek = executor.stack.assertSizeGE(1).peek();
            if (peek.length != 4) {
                return;
            }
            // 软分叉 暂不支持
            super.exec(executor, data);
        }
    },
    OP_CHECKSEQUENCEVERIFY,
    OP_NOP4,
    OP_NOP5, // 180
    OP_NOP6,
    OP_NOP7,
    OP_NOP8,
    OP_NOP9,
    OP_NOP10, // 185
    // OP_UNKNOWN,
    ;

    @Override
    public ScriptOperation compile(ByteBuf bf) {
        return new ScriptOperation(this, ZERO);
    }

    @Override
    public void exec(Interpreter executor, Object data) {
        throw new UnsupportedOperationException(this.name());
    }

    public byte opcode() {
        return (byte) (0xB0 + this.ordinal());
    }

}
