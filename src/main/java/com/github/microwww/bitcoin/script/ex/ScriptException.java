package com.github.microwww.bitcoin.script.ex;

public class ScriptException extends RuntimeException {
    public ScriptException(String message) {
        super(message);
    }

    public ScriptException(String message, Throwable cause) {
        super(message, cause);
    }
}
