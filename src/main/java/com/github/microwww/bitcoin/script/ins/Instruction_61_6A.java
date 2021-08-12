package com.github.microwww.bitcoin.script.ins;

import com.github.microwww.bitcoin.script.Instruction;
import com.github.microwww.bitcoin.script.Interpreter;
import com.github.microwww.bitcoin.script.ScriptOperation;
import com.github.microwww.bitcoin.script.ex.TransactionInvalidException;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Instruction_61_6A implements Instruction {

    // Flow control
    OP_NOP,// 97
    OP_VER,
    OP_IF,// 99
    OP_NOTIF, // 100
    OP_VERIF,
    OP_VERNOTIF,
    OP_ELSE,
    OP_ENDIF,
    OP_VERIFY() {
        @Override
        public ScriptOperation compile(ByteBuf bf) {
            return new ScriptOperation(this, new byte[]{});
        }

        @Override
        public void exec(Interpreter executor, Object data) {
            boolean v = executor.stack.peekSuccess();
            if (!v) {
                throw new TransactionInvalidException("OP_VERIFY not equal");
            }
            logger.debug("OP_VERIFY SUCCESS !");
            executor.stack.pop();
        }
    },
    OP_RETURN, // 106
    ;
    private static final Logger logger = LoggerFactory.getLogger(Instruction_61_6A.class);

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
        return (byte) (0x61 + this.ordinal());
    }
}
