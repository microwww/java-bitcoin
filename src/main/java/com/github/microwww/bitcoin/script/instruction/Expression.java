package com.github.microwww.bitcoin.script.instruction;

public class Expression<T> {
    private int code;
    private T operand;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getOperand() {
        return operand;
    }

    public void setOperand(T operand) {
        this.operand = operand;
    }
}
