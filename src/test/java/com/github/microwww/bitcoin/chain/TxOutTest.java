package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.script.TemplateTransaction;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.util.ClassPath;
import com.github.microwww.bitcoin.wallet.Env;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TxOutTest {

    @Test
    void loadAddress1() {
        List<String> strings = ClassPath.readClassPathFile("/data/line-data.txt");
        byte[] dt = ByteUtil.hex(strings.get(107));
        ByteBuf bf = Unpooled.copiedBuffer(dt);
        RawTransaction tx = new RawTransaction();
        tx.deserialization(bf);
        assertEquals("1df429446c40ea5bb4f65330ef3765a9276fc270918ff2e2af72d54fa2cfbfe4", tx.hash().toHexReverse256());

        String s = tx.getTxOuts()[0].loadAddress().get().toBase58Address(Env.MAIN);
        assertEquals("18cBEMRxXHqzWWCxZNtU91F5sbUNKhL5PX", s);

        TemplateTransaction tt = tx.getTxOuts()[1].loadType().get();
        assertEquals(TemplateTransaction.RETURN, tt);
    }

    @Test
    void loadAddress2() {
        List<String> strings = ClassPath.readClassPathFile("/data/line-data.txt");
        byte[] dt = ByteUtil.hex(strings.get(64));
        ByteBuf bf = Unpooled.copiedBuffer(dt);
        RawTransaction tx = new RawTransaction();
        tx.deserialization(bf);
        assertEquals("0437cd7f8525ceed2324359c2d0ba26006d92d856a9c20fa0241106ee5a597c9", tx.hash().toHexReverse256());

        String s = tx.getTxOuts()[0].loadAddress().get().toBase58Address(Env.MAIN);
        assertEquals("12cbQLTFMXRnSzktFkuoG3eHoMeFtpTu3S", s);
    }
}