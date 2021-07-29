package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.util.ClassPath;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RawTransactionTest {

    @Test
    public void read() {
        List<String> strings = ClassPath.readClassPathFile("/data/line-data.txt");
        byte[] dt = ByteUtil.hex(strings.get(46));
        ByteBuf bf = Unpooled.copiedBuffer(dt);
        RawTransaction tx = new RawTransaction();
        tx.read(bf);
        assertEquals(0, bf.readableBytes());
        tx.write(bf.clear());
        byte[] b2 = ByteUtil.readAll(bf);
        assertArrayEquals(dt, b2);

    }
}