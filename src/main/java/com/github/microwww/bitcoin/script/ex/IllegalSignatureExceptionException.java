package com.github.microwww.bitcoin.script.ex;

public class IllegalSignatureExceptionException extends ScriptRuntimeException {
    public IllegalSignatureExceptionException(String message) {
        super(message);
    }
}
