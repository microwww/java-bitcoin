package com.github.microwww.bitcoin.script.ins;

import com.github.microwww.bitcoin.script.Instruction;
import com.github.microwww.bitcoin.script.Interpreter;
import com.github.microwww.bitcoin.script.ScriptOperation;
import io.netty.buffer.ByteBuf;

public class InstructionAdaptor implements Instruction {

    @Override
    public ScriptOperation compile(ByteBuf bf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void exec(Interpreter interpreter, Object data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte opcode() {
        throw new UnsupportedOperationException();
    }
}