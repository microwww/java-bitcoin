package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxOut;
import com.github.microwww.bitcoin.script.instruction.ScriptNames;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.util.ClassPath;
import com.github.microwww.bitcoin.wallet.CoinAccount;
import com.github.microwww.bitcoin.wallet.Env;
import com.github.microwww.bitcoin.wallet.util.Base58;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PubKeyScriptTest {
    private static List<String> strings = Collections.EMPTY_LIST;

    @Test
    void getScriptForMultiSig() {
        byte[] pk1 = ByteUtil.hex("026477115981fe981a6918a6297d9803c4dc04f328f22041bedff886bbc2962e01");
        byte[] pk2 = ByteUtil.hex("02c96db2302d19b43d4c69368babace7854cc84eb9e061cde51cfa77ca4a22b8b9");
        byte[] pk3 = ByteUtil.hex("03c6103b3b83e4a24a0e33a4df246ef11772f9992663db0c35759a5e2ebf68d8e9");

        byte[] ss = PubKeyScript.getScriptForMultiSig(2, pk1, pk2, pk3);
        String encode = Base58.encodeAddress(Env.MAIN.addressP2SH, ByteUtil.sha256ripemd160(ss));
        assertEquals("36NUkt6FWUi3LAWBqWRdDmdTWbt91Yvfu7", encode);

        String s = new CoinAccount.KeyPublic(ss).getAddress().toP2SHAddress(Env.MAIN);
        assertEquals("36NUkt6FWUi3LAWBqWRdDmdTWbt91Yvfu7", s);
    }

    @Test
    void testMNVersion1() {
        RawTransaction tx = readTx(11);
        RawTransaction from = readTx(13);
        Interpreter exec = new Interpreter(tx);
        int in = 0;
        {
            TxOut inTx = from.getTxOuts()[tx.getTxIns()[in].getPreTxOutIndex()];
            exec.indexTxIn(in)
                    .executor(tx.getTxIns()[in].getScript())
                    .printStack()
                    .executor(inTx.getScriptPubKey())
                    .printStack();
            assertTrue(exec.isSuccess(true));
            assertEquals(1, exec.stack.size());
            assertArrayEquals(new byte[]{}, exec.stack.pop());
        }
        in++;
        {
            TxOut inTx = from.getTxOuts()[tx.getTxIns()[in].getPreTxOutIndex()];
            exec.indexTxIn(in)
                    .executor(tx.getTxIns()[in].getScript())
                    .printStack()
                    .executor(inTx.getScriptPubKey())
                    .printStack();
            assertTrue(exec.isSuccess(true));
            assertEquals(1, exec.stack.size());
            assertArrayEquals(new byte[]{}, exec.stack.pop());
        }
    }

    @Test
    void scriptPubKeyP2PKH() {
        byte[] addr = ByteUtil.hex("c825a1ecf2a6830c4401620c3a16f1995057c2ab");
        byte[] bytes = ByteUtil.readAll(PubKeyScript.Type.P2PKH.scriptPubKey(addr));
        assertArrayEquals(ByteUtil.hex("76a914c825a1ecf2a6830c4401620c3a16f1995057c2ab88ac"), bytes);
        System.out.println(ScriptNames.beautify(bytes));
    }

    @Test
    void testMNVersion2() {
        RawTransaction tx = readTx(22);
        RawTransaction from = readTx(23);
        int in = 0;
        TxOut inTx = from.getTxOuts()[tx.getTxIns()[in].getPreTxOutIndex()];
        Interpreter exec = new Interpreter(tx).indexTxIn(in)
                .executor(tx.getTxIns()[in].getScript())
                .printStack()
                .executor(inTx.getScriptPubKey())
                .printStack();
        assertTrue(exec.isSuccess(true));
        assertEquals(1, exec.stack.size());
        assertArrayEquals(new byte[]{}, exec.stack.pop());
    }

    @BeforeAll
    public static void init() {
        strings = ClassPath.readClassPathFile("/data/online.data.txt");
    }

    private static RawTransaction readTx(int index) {
        RawTransaction tx = new RawTransaction();
        byte[] dt = ByteUtil.hex(strings.get(index));
        ByteBuf bf = Unpooled.copiedBuffer(dt);
        tx.deserialization(bf);
        return tx;
    }

}