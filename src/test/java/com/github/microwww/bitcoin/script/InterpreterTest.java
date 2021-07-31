package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxIn;
import com.github.microwww.bitcoin.math.Uint8;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.util.ClassPath;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {

    @Test
    @Disabled
    public void testV() {
        List<String> strings = ClassPath.readClassPathFile("/data/line-data.txt");
        byte[] dt = ByteUtil.hex(strings.get(62));
        ByteBuf bf = Unpooled.copiedBuffer(dt);
        RawTransaction tx = new RawTransaction();
        tx.read(bf);
        // tx.getTxOuts()[0].setScriptPubKey(ByteUtil.hex("76a91435fbee6a3bf8d99f17724ec54787567393a8a6b188ac"));
        tx.getTxIns()[0].setScript(ByteUtil.hex("4730440220032d30df5ee6f57fa46cddb5eb8d0d9fe8de6b342d27942ae90a3231e0ba333e02203deee8060fdc70230a7f5b4ad7d7bc3e628cbe219a886b84269eaeb81e26b4fe014104ae31c31bf91278d99b8377a35bbce5b27d9fff15456839e919453fc7b3f721f0ba403ff96c9deeb680e5fd341c0fc3a7b90da4631ee39560639db462e9cb850f"));
        TxIn.SignatureScript script = tx.getTxIns()[0].parseSignatureScript();
        byte[] hex = ByteUtil.hex("76a91435fbee6a3bf8d99f17724ec54787567393a8a6b188ac");

        Interpreter interpreter = new Interpreter(hex, script.getSignature(), script.getPk());
        // TODO:: OP_CHECKSIG error
        byte[] v = interpreter.executor();
        System.out.println(ByteUtil.hex(v));
    }
}