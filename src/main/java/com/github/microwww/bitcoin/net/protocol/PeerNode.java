package com.github.microwww.bitcoin.net.protocol;

import io.netty.buffer.ByteBuf;
import org.springframework.util.Assert;

public class PeerNode {
    public static final int PEER_NODE_ADDRESS_LENGTH = 16;
    public static final int PEER_NODE_LENGTH = 4 + PEER_NODE_ADDRESS_LENGTH + 2;
    private long services;
    private byte[] address = new byte[PEER_NODE_ADDRESS_LENGTH];
    private short port;

    public PeerNode(long services, short port) {
        this.services = services;
        this.port = port;
    }

    public long getServices() {
        return services;
    }

    public void setServices(long services) {
        this.services = services;
    }

    public byte[] getAddress() {
        return address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    public short getPort() {
        return port;
    }

    public void setPort(short port) {
        this.port = port;
    }

    public void write(ByteBuf buf) {
        buf.writeLongLE(services).writeBytes(address).writeShortLE(port);
    }

    public static PeerNode read(ByteBuf buf) {
        Assert.isTrue(buf.readableBytes() >= PEER_NODE_LENGTH, "Error PeerNode format");
        long services = buf.readLongLE();
        short port = buf.readShortLE();
        PeerNode node = new PeerNode(services, port);
        byte[] address = new byte[PEER_NODE_ADDRESS_LENGTH];
        buf.readBytes(address);
        node.setAddress(address);
        return node;
    }
}
