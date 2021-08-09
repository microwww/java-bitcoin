package com.github.microwww.bitcoin.script.ins;

import com.github.microwww.bitcoin.script.Instruction;
import com.github.microwww.bitcoin.script.Interpreter;
import com.github.microwww.bitcoin.script.ScriptOperation;
import io.netty.buffer.ByteBuf;

public enum Instruction_BA_FC implements Instruction {
    _186, _187, _188, _189, _190, _191, _192, _193, _194, _195, _196, _197, _198, _199, _200, _201,
    _202, _203, _204, _205, _206, _207, _208, _209, _210, _211, _212, _213, _214, _215, _216, _217,
    _218, _219, _220, _221, _222, _223, _224, _225, _226, _227, _228, _229, _230, _231, _232, _233,
    _234, _235, _236, _237, _238, _239, _240, _241, _242, _243, _244, _245, _246, _247, _248, _249,
    _250, _251, _252,
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
        return (byte) (0xBA + this.ordinal());
    }

}
