package com.github.microwww.bitcoin.net.protocol;

import cn.hutool.core.util.RandomUtil;
import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.net.Peer;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class Version extends AbstractProtocolAdapter<Version> {

    private int magic = 0xfabfb5da; // 0xf9beb4d9;
    private String agent = "/j-bitcoin-0.18.1:0.0.1/";
    private int protocolVersion;
    private long services;

    private PeerNode receiveNode; // remote-node
    private PeerNode emittingNode;// local-node

    private long nonce = RandomUtil.randomLong();
    private byte flag = 1;
    protected Date timestamp = new Date();

    public static Version builder(Peer peer, Settings settings) {
        Version ver = new Version(peer);
        ver.setMagic(settings.getMagic())
                .setProtocolVersion(settings.getProtocolVersion())
                .setServices(settings.getServices())
                .setAgent(settings.getAgent());
        return ver;
    }

    public Version(Peer peer) {
        super(peer);
        receiveNode = new PeerNode(0L, (short) 0);
        emittingNode = new PeerNode(0L, (short) 0);
    }

    @Override
    public int write(ByteBuf buf) {
        int f = buf.readableBytes();
        buf.writeIntLE(this.getProtocolVersion())
                .writeLongLE(this.getServices())
                .writeLongLE(timestamp.getTime() / 1000);
        receiveNode.write(buf);
        emittingNode.setServices(this.services).write(buf);
        buf.writeLongLE(nonce);
        byte[] bytes = this.getAgent().getBytes(StandardCharsets.ISO_8859_1);
        buf.writeByte(bytes.length);
        buf.writeBytes(bytes);
        buf.writeIntLE(peer.getLocalBlockChain().getDiskBlock().getHeight().intValue());
        buf.writeByte(flag);
        return buf.readableBytes() - f;
    }

    public Version read(byte[] payload) {
        ByteBuf buf = Unpooled.copiedBuffer(payload);
        Version ver = new Version(peer);
        ver.setProtocolVersion(buf.readIntLE());
        ver.setServices(buf.readLongLE());
        ver.timestamp = new Date(buf.readLongLE() * 1000);
        ver.receiveNode = PeerNode.read(buf);
        ver.emittingNode = PeerNode.read(buf);
        ver.nonce = buf.readLongLE();
        byte len = buf.readByte();
        byte[] chs = ByteUtil.readLength(buf, len);
        ver.setAgent(new String(chs, StandardCharsets.ISO_8859_1));
        peer.setBlockHeight(buf.readIntLE());
        ver.flag = buf.readByte();
        peer.setVersion(ver);
        return ver;
    }

    public PeerNode getReceiveNode() {
        return receiveNode;
    }

    public Version setReceiveNode(PeerNode receiveNode) {
        this.receiveNode = receiveNode;
        return this;
    }

    public PeerNode getEmittingNode() {
        return emittingNode;
    }

    public Version setEmittingNode(PeerNode emittingNode) {
        this.emittingNode = emittingNode;
        return this;
    }

    public byte getFlag() {
        return flag;
    }

    public Version setFlag(byte flag) {
        this.flag = flag;
        return this;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public long getNonce() {
        return nonce;
    }

    public Version setNonce(long nonce) {
        this.nonce = nonce;
        return this;
    }

    public int getMagic() {
        return magic;
    }

    public Version setMagic(int magic) {
        this.magic = magic;
        return this;
    }

    public String getAgent() {
        return agent;
    }

    public Version setAgent(String agent) {
        this.agent = agent;
        return this;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public Version setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
        return this;
    }

    public long getServices() {
        return services;
    }

    public Version setServices(long services) {
        this.services = services;
        return this;
    }

    public Version setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public String toString() {
        return "Version{" +
                "magic=" + magic +
                ", agent='" + agent + '\'' +
                ", protocolVersion=" + protocolVersion +
                ", services=" + services +
                ", receiveNode=" + receiveNode +
                ", emittingNode=" + emittingNode +
                ", nonce=" + nonce +
                ", flag=" + flag +
                ", timestamp=" + timestamp +
                '}';
    }
}
