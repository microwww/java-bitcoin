package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.util.ClassPath;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChainBlockTest {

    @Test
    void readBody() {
        List<String> lns = ClassPath.readClassPathFile("/data/line-data.txt");
        String s = lns.get(53);
        byte[] hex = ByteUtil.hex(s);
        ByteBuf bf = Unpooled.copiedBuffer(hex);
        ChainBlock chainBlock = new ChainBlock();
        chainBlock.reset(bf);
        assertEquals("5c9cca670df43a0a4e50f1944751397e64c77e125534fa161c82a210f9ea7c7f", chainBlock.hash().toHex());
        assertEquals(1, chainBlock.getTxs().length);
        //assertEquals(1, chainBlock.getTxs()[0].getTxIns().length);
        //assertEquals("0e1d9eebcc1da2c152ef9f303c3efe2e4bffbff118c2668d6368af25e60fed2d", chainBlock.getTxs()[0].hash().toHex());
        //assertEquals(2, chainBlock.getTxs()[0].getTxOuts().length);
        //bf.clear();
        //chainBlock.writeHeader(bf).writeTxCount(bf).writeTxBody(bf);
        // byte[] bytes = ByteUtil.readAll(bf);
        // assertEquals(ByteUtil.hex(bytes), s);
    }

    @Test
    void readHeaderAndBodyUintvar320() {
        List<String> lns = ClassPath.readClassPathFile("/data/online.data.txt");
        String s = lns.get(26);
        byte[] hex = ByteUtil.hex(s);
        ByteBuf bf = Unpooled.copiedBuffer(hex);
        ChainBlock chainBlock = new ChainBlock();
        chainBlock.reset(bf);
        assertEquals("00000000afe94c578b4dc327aa64e1203283c5fd5f152ce886341766298cf523", chainBlock.hash().toHexReverse256());
        bf.clear();
        chainBlock.serialization(bf);
        assertEquals(s, ByteUtil.hex(ByteUtil.readAll(bf)));
    }
}