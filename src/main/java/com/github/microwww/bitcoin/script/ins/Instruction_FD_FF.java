package com.github.microwww.bitcoin.script.ins;

import com.github.microwww.bitcoin.script.Instruction;
import com.github.microwww.bitcoin.script.Interpreter;
import com.github.microwww.bitcoin.script.ScriptOperation;
import io.netty.buffer.ByteBuf;

public enum Instruction_FD_FF implements Instruction {
    // Opcode added by BIP 342 (Tapscript)
    // OP_CHECKSIGADD,
    // Pseudo-words
    OP_PUBKEYHASH,    // 253
    OP_PUBKEY,        // 254
    OP_INVALIDOPCODE, // 255
    // OP_UNKNOWN,
    ;

    @Override
    public ScriptOperation compile(ByteBuf bf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void exec(Interpreter executor, Object data) {
        throw new UnsupportedOperationException();
    }

    public byte opcode() {
        return (byte) (0xFD + this.ordinal());
    }

}
