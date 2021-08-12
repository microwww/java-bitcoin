package com.github.microwww.bitcoin.script.ex;

public class TransactionInvalidException extends ScriptRuntimeException {
    public TransactionInvalidException(String message) {
        super(message);
    }
}
