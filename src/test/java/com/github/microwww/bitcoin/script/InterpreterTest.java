package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.util.ClassPath;
import com.github.microwww.bitcoin.wallet.CoinAccount;
import com.github.microwww.bitcoin.wallet.Env;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {

    // https://www.blockchain.com/btc/tx/6359f0868171b1d194cbee1af2f16ea598ae8fad666d9b012c8ed2b79a236ec4
    @Test
    public void testHeight_1w_6359F08() {
        List<String> strings = ClassPath.readClassPathFile("/data/line-data.txt");
        RawTransaction tx1 = new RawTransaction();
        {
            byte[] dt = ByteUtil.hex(strings.get(68));
            ByteBuf bf = Unpooled.copiedBuffer(dt);
            tx1.read(bf);
            assertEquals("6359f0868171b1d194cbee1af2f16ea598ae8fad666d9b012c8ed2b79a236ec4", tx1.hash().toHexReverse256());
        }
        RawTransaction tx2 = new RawTransaction();
        {
            byte[] dt = ByteUtil.hex(strings.get(69));
            ByteBuf bf = Unpooled.copiedBuffer(dt);
            tx2.read(bf);
            assertEquals("cf4e2978d0611ce46592e02d7e7daf8627a316ab69759a9f3df109a7f2bf3ec3", tx2.hash().toHexReverse256());
        }

        Interpreter interpreter = new Interpreter(tx1).indexTxIn(0)
                .executor(tx1.getTxIns()[0].getScript()).executor(tx2.getTxOuts()[1].getScriptPubKey());
        byte[] bytes = interpreter.pop().get();
        assertArrayEquals(new byte[]{1}, bytes);
    }

    @Test
    public void testV() {
        List<String> strings = ClassPath.readClassPathFile("/data/line-data.txt");
        RawTransaction tx1 = new RawTransaction();
        {
            byte[] dt = ByteUtil.hex(strings.get(64));
            ByteBuf bf = Unpooled.copiedBuffer(dt);
            tx1.read(bf);
            assertEquals("0437cd7f8525ceed2324359c2d0ba26006d92d856a9c20fa0241106ee5a597c9", tx1.hash().toHexReverse256());
        }
        RawTransaction tx2 = new RawTransaction();
        {
            byte[] dt = ByteUtil.hex(strings.get(65));
            ByteBuf bf = Unpooled.copiedBuffer(dt);
            tx2.read(bf);
            assertEquals("f4184fc596403b9d638783cf57adfe4c75c605f6356fbc91338530e9831e9e16", tx2.hash().toHexReverse256());
        }
        byte[] spk = tx1.getTxOuts()[0].getScriptPubKey();
        byte[] addr = Arrays.copyOfRange(spk, 1, 66);
        assertEquals("12cbQLTFMXRnSzktFkuoG3eHoMeFtpTu3S", new CoinAccount.KeyPublic(addr).getAddress().toBase58Address(Env.MAIN));
        Interpreter interpreter = new Interpreter(tx2).indexTxIn(0)
                .executor(tx2.getTxIns()[0].getScript()).executor(tx1.getTxOuts()[0].getScriptPubKey());
        byte[] bytes = interpreter.pop().get();
        assertArrayEquals(new byte[]{1}, bytes);
    }
}