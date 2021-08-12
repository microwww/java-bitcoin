package com.github.microwww.bitcoin.script.ins;

import com.github.microwww.bitcoin.script.Instruction;
import com.github.microwww.bitcoin.script.Interpreter;
import com.github.microwww.bitcoin.script.ScriptOperation;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public enum Instruction_83_8A implements Instruction {

    // bit logic
    OP_INVERT, // 131
    OP_AND,
    OP_OR,
    OP_XOR,
    OP_EQUAL {
        @Override
        public ScriptOperation compile(ByteBuf bf) {
            return new ScriptOperation(this, ZERO);
        }

        @Override
        public void exec(Interpreter executor, Object data) {
            byte[] x1 = executor.stack.pop();
            byte[] x2 = executor.stack.pop();
            executor.stack.push(Arrays.equals(x1, x2) ? 1 : 0);
        }
    },
    OP_EQUALVERIFY() {
        @Override
        public ScriptOperation compile(ByteBuf bf) {
            return new ScriptOperation(this, ZERO);
        }

        @Override
        public void exec(Interpreter executor, Object data) {
            byte[] x1 = executor.stack.assertSizeGE(2).pop();
            byte[] x2 = executor.stack.pop();
            executor.stack.push(Arrays.equals(x1, x2) ? 1 : 0);
            if (logger.isDebugEnabled()) {
                logger.debug("x1: {}, x2: {}", ByteUtil.hex(x1), ByteUtil.hex(x2));
            }
            Instruction_61_6A.OP_VERIFY.exec(executor, data);
        }
    }, // 136
    OP_RESERVED1,
    OP_RESERVED2,
    ;
    private static final Logger logger = LoggerFactory.getLogger(Instruction_83_8A.class);

    @Override
    public ScriptOperation compile(ByteBuf bf) {
        throw new UnsupportedOperationException(this.toString());
    }

    @Override
    public void exec(Interpreter executor, Object data, int pc) {
        this.exec(executor, data);
    }

    public void exec(Interpreter executor, Object data) {
        throw new UnsupportedOperationException();
    }

    public byte opcode() {
        return (byte) (0x83 + this.ordinal());
    }

    @Override
    public String toString() {
        byte c = this.opcode();
        return this.name() + "|" + c + "|0x" + ByteUtil.hex(new byte[]{c});
    }
}
