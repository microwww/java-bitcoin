package com.github.microwww.bitcoin.script;

import io.netty.buffer.ByteBuf;

public enum Instruction_6B_7D implements Instruction {

    // stack ops
    OP_TOALTSTACK, // 107
    OP_FROMALTSTACK,
    OP_2DROP, // 109
    OP_2DUP, // 110
    OP_3DUP,
    OP_2OVER,
    OP_2ROT,
    OP_2SWAP,
    OP_IFDUP, // 115
    OP_DEPTH,
    OP_DROP() {
        @Override
        public ScriptOperation compile(ByteBuf bf) {
            return new ScriptOperation(this, Instruction.ZERO);
        }

        @Override
        public void exec(Interpreter executor, Object data) {
            executor.stack.assertSizeGE(1).pop();
        }

    },
    OP_DUP {
        @Override
        public ScriptOperation compile(ByteBuf bf) {
            return new ScriptOperation(this, Instruction.ZERO);
        }

        @Override
        public void exec(Interpreter executor, Object data) {
            byte[] peek = executor.stack.assertNotEmpty().peek();
            executor.stack.push(peek);
        }
    },
    OP_NIP,
    OP_OVER, // 120
    OP_PICK {
        @Override
        public ScriptOperation compile(ByteBuf bf) {
            return new ScriptOperation(this, Instruction.ZERO);
        }

        @Override
        public void exec(Interpreter executor, Object data) {
            int i = executor.stack.assertNotEmpty().popInt();
            executor.stack.assertSizeGE(i);
            executor.stack.push(executor.stack.peek(i));
        }
    },
    OP_ROLL,
    OP_ROT,
    OP_SWAP,
    OP_TUCK, // 125
    ;

    @Override
    public ScriptOperation compile(ByteBuf bf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void exec(Interpreter executor, Object data, int pc) {
        this.exec(executor, data);
    }

    public void exec(Interpreter executor, Object data) {
        throw new UnsupportedOperationException();
    }

    public byte opcode() {
        return (byte) (0x6B + this.ordinal());
    }
}
