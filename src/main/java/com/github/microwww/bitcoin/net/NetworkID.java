package com.github.microwww.bitcoin.net;

import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;

import java.net.InetAddress;
import java.net.UnknownHostException;

public enum NetworkID {
    IPV4(4) {
        @Override
        public String parse(byte[] bytes) throws UnknownHostException {
            return InetAddress.getByAddress(bytes).getHostAddress();
        }
    },
    IPV6(16) {
        @Override
        public String parse(byte[] bytes) throws UnknownHostException {
            return InetAddress.getByAddress(bytes).getHostAddress();
        }
    },
    TORV2(10),
    TORV3(32),
    I2P(32),
    CJDNS(16),
    ;

    private final int len;

    NetworkID(int len) {
        this.len = len;
    }

    public int getId() {
        return this.ordinal() + 1;
    }

    public byte[] read(ByteBuf bf) {
        return ByteUtil.readLength(bf, this.len);
    }

    public String parse(byte[] bytes) throws UnknownHostException {
        return "0x" + ByteUtil.hex(bytes);
    }
}