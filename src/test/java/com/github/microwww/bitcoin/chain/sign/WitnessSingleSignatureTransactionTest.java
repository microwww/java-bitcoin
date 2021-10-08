package com.github.microwww.bitcoin.chain.sign;

import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxOut;
import com.github.microwww.bitcoin.script.Interpreter;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.util.ClassPath;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WitnessSingleSignatureTransactionTest {
    private static List<String> strings = Collections.EMPTY_LIST;

    @BeforeAll
    public static void init() {
        strings = ClassPath.readClassPathFile("/data/line-data.txt");
    }

    // https://github.com/bitcoin/bips/blob/master/bip-0143.mediawiki#native-p2wsh
    @Test
    @Disabled
    void data4signature() {
        RawTransaction tx = readTx(94);
        Interpreter executor = new Interpreter(tx).executor(tx.getTxIns()[0].getScript())
                .executor(ByteUtil.hex("21036d5c20fa14fb2f635474c1dc4ef5909d4568e5569b79fc94d3448486e14685f8ac"));
        assertTrue(executor.stackSizeEqual(1));
        assertTrue(executor.isSuccess());

        executor = new Interpreter(tx).nextTxIn(new TxOut().setValue(49_0000_0000L)).executor(tx.getTxIns()[1].getScript()).witnessPushStack()
                .executor(ByteUtil.hex("00205d1b56b63d714eebe542309525f484b7e9d6f686b3781b6f61ef925d66d6f6a0"));

        // TODO:: 其他脚本如何获取
        // executor.executor(ByteUtil.hex("23210255a9626aebf5e29c0e6538428ba0d1dcf6ca98ffdf086aa8ced5e0d0215ea465ac"));
        // TODO:: 如何执行
        assertTrue(executor.isSuccess());
    }

    private static RawTransaction readTx(int index) {
        RawTransaction tx = new RawTransaction();
        byte[] dt = ByteUtil.hex(strings.get(index));
        ByteBuf bf = Unpooled.copiedBuffer(dt);
        tx.deserialization(bf);
        return tx;
    }
}