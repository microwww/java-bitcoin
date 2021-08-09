package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;

public enum Instruction_00_4B implements Instruction {
    // push value
    OP_0, _1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15,
    _16, _17, _18, _19, _20, _21, _22, _23, _24, _25, _26, _27, _28, _29, _30, _31,
    _32, _33, _34, _35, _36, _37, _38, _39, _40, _41, _42, _43, _44, _45, _46, _47,
    _48, _49, _50, _51, _52, _53, _54, _55, _56, _57, _58, _59, _60, _61, _62, _63,
    _64, _65, _66, _67, _68, _69, _70, _71, _72, _73, _74, _75;

    @Override
    public ScriptOperation compile(ByteBuf bf) {
        byte[] bytes = ByteUtil.readLength(bf, this.ordinal());
        return new ScriptOperation(this, bytes);
    }

    @Override
    public void exec(Interpreter executor, Object data) {
        executor.stack.push((byte[]) data);
    }

    public byte opcode() {
        return (byte) (0x00 + this.ordinal());
    }

}
