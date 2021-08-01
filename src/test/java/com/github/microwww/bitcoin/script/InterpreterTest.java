package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxIn;
import com.github.microwww.bitcoin.math.Uint8;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.util.ClassPath;
import com.github.microwww.bitcoin.wallet.Account4bitcoin;
import com.github.microwww.bitcoin.wallet.BitAccountConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {

    // https://www.blockchain.com/btc/tx/6359f0868171b1d194cbee1af2f16ea598ae8fad666d9b012c8ed2b79a236ec4
    @Test
    public void testV() {
        List<String> strings = ClassPath.readClassPathFile("/data/line-data.txt");
        RawTransaction tx1 = new RawTransaction();
        {
            byte[] dt = ByteUtil.hex(strings.get(64));
            ByteBuf bf = Unpooled.copiedBuffer(dt);
            tx1.read(bf);
            assertEquals("a17882369ddb44dc7a57fab541004ccb7600a8af2a92fc9ed30d75d5d66e7851", tx1.hash().toHexReverse256());
        }
        RawTransaction tx2 = new RawTransaction();
        {
            byte[] dt = ByteUtil.hex(strings.get(65));
            ByteBuf bf = Unpooled.copiedBuffer(dt);
            tx2.read(bf);
            assertEquals("e9de9754c80567d79ab039db60142341747deafb9741edc380c7cdb41c0967a0", tx2.hash().toHexReverse256());
        }
        byte[] spk = tx1.getTxOuts()[0].getScriptPubKey();
        byte[] addr = Arrays.copyOfRange(spk, 3, 23);
        assertEquals("mqnzTpLbk5a51KdJXuWXTu3yGRG7x8Dyt6", Account4bitcoin.toBase58(BitAccountConfig.REG_TEST.getAddressHeader(), addr));

        TxIn.SignatureScript signatureScript = tx2.getTxIns()[0].parseSignatureScript();
        assertEquals("mqnzTpLbk5a51KdJXuWXTu3yGRG7x8Dyt6", Account4bitcoin.publicToBase58Address(BitAccountConfig.REG_TEST, signatureScript.getPk()));

        Interpreter interpreter = new Interpreter(tx2).indexTxIn(0)
                .executor(tx2.getTxIns()[0].getScript()).executor(tx1.getTxOuts()[0].scriptPubKey);
        byte[] bytes = interpreter.pop().get();
        assertArrayEquals(new byte[]{1}, bytes);
    }
}