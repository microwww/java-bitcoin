package com.github.microwww.bitcoin.script.instruction;

import com.github.microwww.bitcoin.script.Interpreter;
import io.netty.buffer.ByteBuf;

public interface Script {
    byte[] ZERO = {};

    void operand(ByteBuf bf);

    void exec(Interpreter interpreter);

    int opcode();

    default byte[] getOperand() {
        return ZERO;
    }
}
