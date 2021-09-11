package com.github.microwww.bitcoin.script.ex;

public class ScriptRuntimeException extends ScriptException {
    public ScriptRuntimeException(String message) {
        super(message);
    }

    public ScriptRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
