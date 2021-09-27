package com.github.microwww.bitcoin.script.instruction;

import com.github.microwww.bitcoin.script.Interpreter;
import com.github.microwww.bitcoin.script.ex.TransactionInvalidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FlowControl {
    private static final Logger logger = LoggerFactory.getLogger(FlowControl.class);

    // Flow control
    static class OP_NOP extends AbstractScriptNoOperand {

        public OP_NOP(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
        }
    }// 97

    // OP_VER,
    // OP_IF,// 99
    // OP_NOTIF, // 100
    // OP_VERIF,
    // OP_VERNOTIF,
    // OP_ELSE,
    // OP_ENDIF,
    static class OP_VERIFY extends AbstractScriptNoOperand {

        public OP_VERIFY(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            boolean v = executor.stack.peekSuccess();
            if (!v) {
                throw new TransactionInvalidException("OP_VERIFY not equal");
            }
            logger.debug("OP_VERIFY SUCCESS !");
            executor.stack.pop();
        }
    }

    // OP_RETURN, // 106
}
