package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.store.AccessBlockFile;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiskBlockTest {

    @Test
    public void testPatten() {
        Matcher matcher = AccessBlockFile.pt.matcher("blk00001.dat");
        assertTrue(matcher.matches());
        String group = matcher.group(1);
        assertEquals("1", group);
    }

    @Test
    public void verifyNBits() {
        byte[] hex = ByteUtil.hex("01000000e25509cde707c3d02a693e4fe9e7cdd57c38a0d2c8d6341f20dae84b000000000c113df1185e162ee92d031fe21d1400ff7d705a3e9b9c860eea855313cd8ca26c087f49ffff001d30b732310101000000010000000000000000000000000000000000000000000000000000000000000000ffffffff0804ffff001d02fe01ffffffff0100f2052a010000004341044cacdab163f95401172664f36e52e1fd7181c15a321eb99d7d00957e22948b7bf8f924763047e44d663890a961208c9a0634ac86ca33acc016c2ac5307154e05ac00000000");
        ByteBuf bf = Unpooled.buffer().writeBytes(hex);
        ChainBlock h2015 = new ChainBlock().reset(bf);
        hex = ByteUtil.hex("0100000075f34113c86ac301450b583b6dc8e55a7a63292fc927025fa2ba3f0d000000008a6dc2b378503a15701871f870ea92afa030f40f08d6afb0369d5da15e925eb6b2757c49ffff001d037fbbe10101000000010000000000000000000000000000000000000000000000000000000000000000ffffffff0804ffff001d02df03ffffffff0100f2052a01000000434104604b25008d9c679c7b9e25ce07b7c7e9415cdb8cfd3109a750f1010c852a8a180e59fe24e879d9a6fc71ec47b2410d44b618b96c505f53eca8225c9e0e758445ac00000000");
        bf.clear().writeBytes(hex);
        ChainBlock to = new ChainBlock().reset(bf);
    }
}