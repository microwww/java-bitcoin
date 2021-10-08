package com.github.microwww.bitcoin.chain.sign;

import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxOut;
import com.github.microwww.bitcoin.script.Interpreter;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.util.ClassPath;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WitnessHashAllSignatureTransactionTest {
    private static List<String> strings = Collections.EMPTY_LIST;

    @BeforeAll
    public static void init() {
        strings = ClassPath.readClassPathFile("/data/online.data.txt");
    }

    @Test
    void data4signature() {
        RawTransaction tx = readTx(6);
        RawTransaction from = readTx(8);
        int index = 0;
        TxOut txOut = from.getTxOuts()[tx.getTxIns()[index].getPreTxOutIndex()];
        Interpreter exe = new Interpreter(tx).executor(tx.getTxIns()[index].getScript()).witnessPushStack().indexTxIn(0, txOut)
                .executor(txOut.getScriptPubKey());
        assertTrue(exe.isSuccess());
    }

    private static RawTransaction readTx(int index) {
        RawTransaction tx = new RawTransaction();
        byte[] dt = ByteUtil.hex(strings.get(index));
        ByteBuf bf = Unpooled.copiedBuffer(dt);
        tx.deserialization(bf);
        return tx;
    }
}