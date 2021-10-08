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
        ByteBuf bf = Unpooled.buffer().writeBytes(dt);
        RawTransaction tx = new RawTransaction();
        tx.deserialization(bf);
        assertEquals(0, bf.readableBytes());
        tx.serialization(bf.clear());
        byte[] b2 = ByteUtil.readAll(bf);
        assertArrayEquals(dt, b2);
        assertEquals("887e1d2a500264d5f5329c623fa64604415ae7627cb17097d07769a932e2df87", tx.hash().toHexReverse256());
        assertEquals(2, tx.getTxIns()[0].getTxWitness().length);

        dt = ByteUtil.hex(strings.get(113));
        bf.clear().writeBytes(dt);
        tx = new RawTransaction();
        tx.deserialization(bf);
        assertEquals("628cc923b14aee7d9b41416b3a25c4b6fe5ca1218fb1fe7b3bd92da4eb945a4b", tx.hash().toHexReverse256());
        assertEquals("c83bd789fd6a542f4cf5f0788b7d65932cf9754e0bde727df5f502aba0799e0c", tx.whash().toHexReverse256());
    }

    @Test
    public void testStyle() {
        List<String> strings = ClassPath.readClassPathFile("/data/line-data.txt");
        int[] c = new int[]{91, 113};
        for (int i : c) {
            byte[] dt = ByteUtil.hex(strings.get(i));
            ByteBuf bf = Unpooled.buffer().writeBytes(dt);
            bf.markReaderIndex();
            RawTransaction tr = new RawTransaction();
            tr.deserialization(bf);
            // System.out.println(tr.beautify().toString());
            StringBuilder sb = new StringBuilder();
            for (String s : tr.beautify().toString().split("\n")) {
                sb.append(s.substring(14).replace(" ", "").split("->")[0]);
            }
            assertEquals(ByteUtil.hex(dt), sb.toString());
        }
    }
}