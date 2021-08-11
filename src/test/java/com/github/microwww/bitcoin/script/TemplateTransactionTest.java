package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxOut;
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

class TemplateTransactionTest {
    private static List<String> strings = Collections.EMPTY_LIST;

    @Test
    void getScriptForMultiSig() {
        byte[] pk1 = ByteUtil.hex("026477115981fe981a6918a6297d9803c4dc04f328f22041bedff886bbc2962e01");
        byte[] pk2 = ByteUtil.hex("02c96db2302d19b43d4c69368babace7854cc84eb9e061cde51cfa77ca4a22b8b9");
        byte[] pk3 = ByteUtil.hex("03c6103b3b83e4a24a0e33a4df246ef11772f9992663db0c35759a5e2ebf68d8e9");

        byte[] ss = TemplateTransaction.getScriptForMultiSig(2, pk1, pk2, pk3);
        String encode = Base58.encodeAddress(Env.MAIN.addressP2SH, ByteUtil.sha256ripemd160(ss));
        assertEquals("36NUkt6FWUi3LAWBqWRdDmdTWbt91Yvfu7", encode);

        String s = new CoinAccount.KeyPublic(ss).getAddress().toP2SHAddress(Env.MAIN);
        assertEquals("36NUkt6FWUi3LAWBqWRdDmdTWbt91Yvfu7", s);
    }

    @Test
    void mn() {
        RawTransaction tx = readTx(11);
        RawTransaction from = readTx(13);
        int in = 1;
        TxOut inTx = from.getTxOuts()[tx.getTxIns()[in].getPreTxOutIndex()];
        Interpreter executor = new Interpreter(tx)
                .executor(tx.getTxIns()[in].getScript())
                .printStack()
                .executor(inTx.getScriptPubKey())
                .printStack();
        assertTrue(executor.isSuccess());
    }

    @BeforeAll
    public static void init() {
        strings = ClassPath.readClassPathFile("/data/online.data.txt");
    }

    private static RawTransaction readTx(int index) {
        RawTransaction tx = new RawTransaction();
        byte[] dt = ByteUtil.hex(strings.get(index));
        ByteBuf bf = Unpooled.copiedBuffer(dt);
        tx.read(bf);
        return tx;
    }

}