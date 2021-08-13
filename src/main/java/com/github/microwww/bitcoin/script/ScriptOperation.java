package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.util.ByteUtil;

public class ScriptOperation<T> {
    public static final ScriptOperation[] instructions = new ScriptOperation[512];
    public final Instruction keyword;
    public final T operand;

    public ScriptOperation(Instruction keyword, T operand) {
        this.keyword = keyword;
        this.operand = operand;
    }

    /**
     * @param interpreter
     * @param index       源码中的位置
     */
    public void exec(Interpreter interpreter, int index) {
        keyword.exec(interpreter, operand, index);
    }

    @Override
    public String toString() {
        if (operand instanceof byte[]) {
            return keyword + "(0x" + ByteUtil.hex((byte[]) operand) + ")" ;
        } else {
            return keyword + "(" + operand + ")" ;
        }
    }
}
