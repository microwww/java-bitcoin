package com.github.microwww.bitcoin.net.protocol;

import cn.hutool.core.util.RandomUtil;
import com.github.microwww.bitcoin.conf.BlockInfo;
import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.net.Peer;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class Version extends ProtocolAdapter {
    protected final Settings settings;
    private PeerNode receiveNode; // remote-node
    private PeerNode emittingNode;// local-node

    private long nonce = RandomUtil.randomLong();
    private byte flag = 1;
    protected Date timestamp;

    public Version(Settings settings) {
        this(settings, new Date());
    }

    public Version(Settings settings, Date now) {
        this.settings = settings;
        receiveNode = new PeerNode(0L, (short) 0);
        emittingNode = new PeerNode(settings.getServices(), (short) 0);
        this.timestamp = now;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeIntLE(settings.getProtocolVersion())
                .writeLongLE(settings.getServices())
                .writeLongLE(timestamp.getTime() / 1000);
        receiveNode.write(buf);
        emittingNode.write(buf);
        buf.writeLongLE(nonce);
        byte[] bytes = settings.getAgent().getBytes(StandardCharsets.ISO_8859_1);
        buf.writeByte(bytes.length);
        buf.writeBytes(bytes);
        buf.writeIntLE(BlockInfo.getInstance().getHeight().intValue());
        buf.writeByte(flag);
    }

    public static Version read(Peer peer, byte[] payload) {
        ByteBuf buf = Unpooled.copiedBuffer(payload);
        Version ver = new Version(new Settings());
        ver.settings.setProtocolVersion(buf.readIntLE());
        ver.settings.setServices(buf.readLongLE());
        ver.timestamp = new Date(buf.readLongLE() * 1000);
        ver.receiveNode = PeerNode.read(buf);
        ver.emittingNode = PeerNode.read(buf);
        ver.nonce = buf.readLongLE();
        byte len = buf.readByte();
        byte[] chs = ByteUtil.readLength(buf, len);
        ver.settings.setAgent(new String(chs, StandardCharsets.ISO_8859_1));
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

    public Settings getSettings() {
        return settings;
    }
}
