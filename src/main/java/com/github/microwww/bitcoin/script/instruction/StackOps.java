package com.github.microwww.bitcoin.script.instruction;

import com.github.microwww.bitcoin.script.Interpreter;

public abstract class StackOps {

    // OP_TOALTSTACK, // 107
    // OP_FROMALTSTACK,
    // OP_2DROP, // 109
    // OP_2DUP, // 110
    // OP_3DUP,
    // OP_2OVER,
    // OP_2ROT,
    // OP_2SWAP,
    // OP_IFDUP, // 115
    // OP_DEPTH,
    public static class OP_DROP extends AbstractScriptNoOperand {

        public OP_DROP(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            executor.stack.assertSizeGE(1).pop();
        }

    }

    static class OP_DUP extends AbstractScriptNoOperand {

        public OP_DUP(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            byte[] peek = executor.stack.assertNotEmpty().peek();
            executor.stack.push(peek);
        }
    }

    // OP_NIP,
    // OP_OVER, // 120
    static class OP_PICK extends AbstractScriptNoOperand {

        public OP_PICK(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            int i = executor.stack.assertNotEmpty().popInt();
            executor.stack.assertSizeGE(i);
            executor.stack.push(executor.stack.peek(i));
        }
    }
    // OP_ROLL,
    // OP_ROT,
    // OP_SWAP,
    // OP_TUCK, // 125
}
