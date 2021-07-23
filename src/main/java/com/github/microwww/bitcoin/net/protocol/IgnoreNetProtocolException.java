package com.github.microwww.bitcoin.net.protocol;

public class IgnoreNetProtocolException extends UnsupportedNetProtocolException {
    private final String cmd;

    public IgnoreNetProtocolException(String cmd) {
        super("Unsupported Net Protocol : " + cmd + ", You can add it in : NetProtocol");
        this.cmd = cmd;
    }

    public String getCmd() {
        return cmd;
    }
}
