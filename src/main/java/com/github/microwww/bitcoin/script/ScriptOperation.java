package com.github.microwww.bitcoin.script;

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
}
