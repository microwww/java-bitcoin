package com.github.microwww.bitcoin.net.protocol;

import cn.hutool.core.util.HexUtil;
import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.net.MessageHeader;
import com.github.microwww.bitcoin.net.NetProtocol;
import com.github.microwww.bitcoin.net.Peer;
import com.github.microwww.bitcoin.util.ClassPath;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class VersionTest {

    @Test
    public void testSendVersion() {
        Settings settings = new Settings();
        ByteBuf payload = Unpooled.buffer();
        //buffer.skipBytes(MessageHeader.HEADER_SIZE);
        Date date = new Date(40 * 365 * 24 * 60 * 60 * 1000); // 1970 + 40年
        new Version(settings, date).setNonce(123456789012345678L).write(payload);
        int i = payload.readableBytes();
        byte[] byts = new byte[i];
        payload.readBytes(byts);
        ByteBuf bf = Unpooled.buffer();
        MessageHeader header = new MessageHeader(settings.getMagic(), NetProtocol.VERSION).setPayload(byts).writer(bf);
        bf.writeBytes(payload);

        byte[] by = new byte[bf.readableBytes()];
        bf.readBytes(by);
        String hex = HexUtil.encodeHexStr(by);
        // System.out.println(hex);
        String line3 = ClassPath.readClassPathFile("/data/line-data.txt").get(2);
        Assert.assertEquals(line3, hex);

        MessageHeader read = MessageHeader.read(Unpooled.copiedBuffer(by));
        assertEquals(header.getMagic(), read.getMagic());
        assertArrayEquals(header.getPayload(), read.getPayload());
        assertEquals(header.getNetProtocol(), read.getNetProtocol());
        assertTrue(read.verifyChecksum());
    }

    @Test
    public void testParseVersion() {
        Settings settings = new Settings();
        Peer peer = new Peer(settings, "localhost", 8333);
        String line5 = ClassPath.readClassPathFile("/data/line-data.txt").get(4);
        byte[] bytes = HexUtil.decodeHex(line5);
        MessageHeader read = MessageHeader.read(Unpooled.copiedBuffer(bytes));
        assertEquals(settings.getMagic(), read.getMagic());
        assertEquals(NetProtocol.VERSION, read.getNetProtocol());
        // payload length
        assertTrue(read.verifyChecksum());
        // assertEquals(header.getCommand(), read.getCommand());
        Version ver = Version.read(peer, read.getPayload());
        assertEquals(70016, ver.getSettings().getProtocolVersion());
        assertEquals(1626405650000L, ver.getTimestamp().getTime()); // Jul 16 2021 11:20:50 GMT+0800
        assertEquals(1033, ver.getSettings().getServices()); // 1033 | 0x0000000000000409

        assertEquals(0, ver.getReceiveNode().getServices());
        assertArrayEquals(new byte[PeerNode.PEER_NODE_ADDRESS_LENGTH], ver.getReceiveNode().getAddress());
        assertEquals(0, ver.getReceiveNode().getPort());

        assertEquals(1033, ver.getEmittingNode().getServices());
        assertArrayEquals(new byte[PeerNode.PEER_NODE_ADDRESS_LENGTH], ver.getEmittingNode().getAddress());
        assertEquals(0, ver.getEmittingNode().getPort());
        assertEquals(110, peer.getBlockHeight());
        assertEquals(1, ver.getFlag());
    }
}