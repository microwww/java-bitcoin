package com.github.microwww.bitcoin.script.instruction;

import com.github.microwww.bitcoin.script.Interpreter;

public class Expansion {

    // expansion
    // OP_NOP1,
    static class OP_CHECKLOCKTIMEVERIFY extends AbstractScriptNoOperand { // OP_NOP2
        public OP_CHECKLOCKTIMEVERIFY(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            byte[] peek = executor.stack.assertSizeGE(1).peek();
            if (peek.length != 4) {
                return;
            }
            // 软分叉 暂不支持
            throw new UnsupportedOperationException(ScriptNames.OP_CHECKLOCKTIMEVERIFY.name());
        }
    }
    // OP_CHECKSEQUENCEVERIFY,
    // OP_NOP4,
    // OP_NOP5, // 180
    // OP_NOP6,
    // OP_NOP7,
    // OP_NOP8,
    // OP_NOP9,
    // OP_NOP10, // 185
    // OP_UNKNOWN,

}
