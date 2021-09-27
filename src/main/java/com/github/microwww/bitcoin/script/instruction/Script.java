package com.github.microwww.bitcoin.script.instruction;

import com.github.microwww.bitcoin.script.Interpreter;
import io.netty.buffer.ByteBuf;

public interface Script {
    byte[] ZERO = {};

    public void operand(ByteBuf bf);

    public void exec(Interpreter interpreter);

    public int opcode();
}
