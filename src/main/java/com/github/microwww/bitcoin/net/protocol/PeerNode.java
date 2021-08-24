package com.github.microwww.bitcoin.net.protocol;

import io.netty.buffer.ByteBuf;
import org.springframework.util.Assert;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;

public class PeerNode {
    public static final int PEER_NODE_ADDRESS_LENGTH = 16;
    public static final int PEER_NODE_LENGTH = 4 + PEER_NODE_ADDRESS_LENGTH + 2;
    private int time;
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

    public PeerNode setServices(long services) {
        this.services = services;
        return this;
    }

    public byte[] getAddress() {
        return address;
    }

    public InetAddress getInetAddress() throws UnknownHostException {
        return InetAddress.getByAddress(address);
    }

    public PeerNode setAddress(byte[] address) {
        this.address = address;
        return this;
    }

    public int getPort() {
        return Short.toUnsignedInt(port);
    }

    public PeerNode setPort(short port) {
        this.port = port;
        return this;
    }

    public PeerNode write(ByteBuf buf) {
        buf.writeLongLE(services).writeBytes(address).writeShortLE(port);
        return this;
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

    public int getTime() {
        return time;
    }

    public Date getDate() {
        return new Date(Integer.toUnsignedLong(time) * 1000);
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "PeerNode{" +
                "time=" + this.getDate() +
                "services=" + services +
                ", address=" + Arrays.toString(address) +
                ", port=" + port +
                '}';
    }
}
