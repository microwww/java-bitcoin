package com.github.microwww.bitcoin.net.protocol;

public class UnsupportedNetProtocolException extends RuntimeException {
    private final String cmd;

    public UnsupportedNetProtocolException(String cmd) {
        super("Unsupported Net Protocol : " + cmd + ", You can add it in : NetProtocol");
        this.cmd = cmd;
    }

    public String getCmd() {
        return cmd;
    }
}
