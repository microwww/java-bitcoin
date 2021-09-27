package com.github.microwww.bitcoin.script.instruction;

import com.github.microwww.bitcoin.script.Interpreter;
import com.github.microwww.bitcoin.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public abstract class BitLogic {
    private static final Logger logger = LoggerFactory.getLogger(BitLogic.class);

    // bit logic
    // OP_INVERT, // 131
    // OP_AND,
    // OP_OR,
    // OP_XOR,
    static class OP_EQUAL extends AbstractScriptNoOperand {

        public OP_EQUAL(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            byte[] x1 = executor.stack.pop();
            byte[] x2 = executor.stack.pop();
            executor.stack.push(Arrays.equals(x1, x2) ? 1 : 0);
        }
    }

    static class OP_EQUALVERIFY extends AbstractScriptNoOperand {

        public OP_EQUALVERIFY(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            byte[] x1 = executor.stack.assertSizeGE(2).pop();
            byte[] x2 = executor.stack.pop();
            executor.stack.push(Arrays.equals(x1, x2) ? 1 : 0);
            if (logger.isDebugEnabled()) {
                logger.debug("x1: {} x2: {}", ByteUtil.hex(x1), ByteUtil.hex(x2));
            }
            new FlowControl.OP_VERIFY(ScriptNames.OP_VERIFY.ordinal()).exec(executor);
        }
    } // 136
    // OP_RESERVED1,
    // OP_RESERVED2,
}
