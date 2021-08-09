package com.github.microwww.bitcoin.script;

import io.netty.buffer.ByteBuf;

public interface Instruction<T> {
    byte[] ZERO = {};

    ScriptOperation<T> compile(ByteBuf bf);

    default void exec(Interpreter interpreter, T data, int pc) {
        this.exec(interpreter, data);
    }

    void exec(Interpreter interpreter, T data);

    byte opcode();
}
