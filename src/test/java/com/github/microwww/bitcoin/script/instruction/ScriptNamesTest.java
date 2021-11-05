package com.github.microwww.bitcoin.script.instruction;

import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ScriptNamesTest {

    @Test
    void beautify() {
        ByteBuf bf = Unpooled.buffer().writeBytes(ByteUtil.hex("76a914c825a1ecf2a6830c4401620c3a16f1995057c2ab88ac"));
        List<Script> cm = ScriptNames.compile(bf);
        // System.out.println(ScriptNames.beautify(cm));
        Assertions.assertEquals("OP_DUP OP_HASH160 c825a1ecf2a6830c4401620c3a16f1995057c2ab OP_EQUALVERIFY OP_CHECKSIG ", ScriptNames.beautify(cm).toString());
    }

    @Test
    void decompile() {
        // 4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b
        List<Script> list = new ArrayList<>();

        list.add(Constants.pushValue(ByteUtil.hex("ffff001d")));
        list.add(new Constants.PushCode(ScriptNames._1.ordinal()));
        list.add(new Constants.PushCode(ScriptNames._4.ordinal()));
        //GenTransaction.G.genCoinbaseTransaction(str);
        String str = "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks";
        list.add(Constants.pushValue(ByteUtil.UTF8(str)));

        byte[] bt = ByteUtil.hex("04" + "ffff001d" +
                "01" +
                "04" +
                "45" + "5468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73"
        );
        assertArrayEquals(bt, ScriptNames.decompile(list));
    }
}