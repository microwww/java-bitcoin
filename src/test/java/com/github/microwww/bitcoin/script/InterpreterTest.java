package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxIn;
import com.github.microwww.bitcoin.chain.TxOut;
import com.github.microwww.bitcoin.math.UintVar;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.util.ClassPath;
import com.github.microwww.bitcoin.wallet.CoinAccount;
import com.github.microwww.bitcoin.wallet.Env;
import com.github.microwww.bitcoin.wallet.Secp256k1;
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
        test_OP_CHECKSIG(68, 0, 69, 1);
    }

    @Test
    public void testHeight_108267_e411c2c6635754() {
        test_OP_CHECKSIG(77, 0, 78, 0);
    }

    public void test_OP_CHECKSIG(int transaction, int txInIndex, int txOut, int txOutIndex) {
        List<String> strings = ClassPath.readClassPathFile("/data/line-data.txt");
        RawTransaction tx1 = new RawTransaction();
        {
            byte[] dt = ByteUtil.hex(strings.get(transaction));
            ByteBuf bf = Unpooled.copiedBuffer(dt);
            tx1.read(bf);
            // assertEquals("6359f0868171b1d194cbee1af2f16ea598ae8fad666d9b012c8ed2b79a236ec4", tx1.hash().toHexReverse256());
        }
        RawTransaction tx2 = new RawTransaction();
        {
            byte[] dt = ByteUtil.hex(strings.get(txOut));
            ByteBuf bf = Unpooled.copiedBuffer(dt);
            tx2.read(bf);
            // assertEquals("cf4e2978d0611ce46592e02d7e7daf8627a316ab69759a9f3df109a7f2bf3ec3", tx2.hash().toHexReverse256());
        }

        Interpreter interpreter = new Interpreter(tx1).indexTxIn(txInIndex)
                .executor(tx1.getTxIns()[txInIndex].getScript()).executor(tx2.getTxOuts()[txOutIndex].getScriptPubKey());
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

    // https://github.com/bitcoin/bips/blob/master/bip-0143.mediawiki#Native_P2WPKH
    // c37af31116d1b27caf68aae9e3ac82f1477929014d5b917657d0eb49478cb670
    @Test
    public void P2WPKH() {
        List<String> strings = ClassPath.readClassPathFile("/data/line-data.txt");
        RawTransaction tx1 = new RawTransaction();
        {
            byte[] dt = ByteUtil.hex(strings.get(72));
            ByteBuf bf = Unpooled.copiedBuffer(dt);
            tx1.read(bf);
            assertEquals("3335ffae0df20c5407e8de12b49405c8e912371f00fe4132bfaf95ad49c40243", tx1.hash().toHexReverse256());
        }

        ByteBuf txins = Unpooled.buffer();
        ByteBuf txinSeq = Unpooled.buffer();
        for (TxIn txIn : tx1.getTxIns()) {
            txins.writeBytes(txIn.getPreTxHash().fill256bit()).writeIntLE(txIn.getPreTxOutIndex());
            txinSeq.writeIntLE(txIn.getSequence().intValue());
        }
        ByteBuf txout = Unpooled.buffer();
        for (TxOut out : tx1.getTxOuts()) {
            out.write(txout);
        }
        byte[] hashIn = ByteUtil.sha256sha256(ByteUtil.readAll(txins));
        assertArrayEquals(ByteUtil.hex("96b827c8483d4e9b96712b6713a7b68d6e8003a781feba36c31143470b4efd37"), hashIn);
        byte[] hashSeq = ByteUtil.sha256sha256(ByteUtil.readAll(txinSeq));
        assertArrayEquals(ByteUtil.hex("52b0a642eea2fb7ae638c36f6252b6750293dbe574a806984b8e4d8548339a3b"), hashSeq);
        byte[] hashOut = ByteUtil.sha256sha256(ByteUtil.readAll(txout));
        assertArrayEquals(ByteUtil.hex("863ef3e1a92afbfdb97f31ad0fc7683ee943e9abcf2501590ff8f6551f47e5e5"), hashOut);
        ByteBuf sn = Unpooled.buffer();
        int nIn = 1;
        byte[] scriptCode = ByteUtil.hex("1976a9141d0f172a0ecb48aee1be1f2687d2963ae33f71a188ac");
        sn
                .writeIntLE(tx1.getVersion())
                .writeBytes(hashIn)
                .writeBytes(hashSeq)
                // outpoint
                .writeBytes(tx1.getTxIns()[nIn].getPreTxHash().fill256bit()).writeIntLE(tx1.getTxIns()[nIn].getPreTxOutIndex())
                .writeBytes(scriptCode)
                .writeLong(0x0046c32300000000L)
                .writeIntLE(tx1.getTxIns()[nIn].getSequence().intValue())
                .writeBytes(hashOut)
                .writeIntLE(tx1.getLockTime().intValue())
                .writeIntLE(1);
        byte[] bytes = ByteUtil.readAll(sn);
        byte[] bytes1 = ByteUtil.sha256(bytes);
        System.out.println(ByteUtil.hex(bytes1));
        assertArrayEquals(ByteUtil.hex("c37af31116d1b27caf68aae9e3ac82f1477929014d5b917657d0eb49478cb670"), ByteUtil.sha256(bytes1));
        byte[] snn = ByteUtil.hex("304402203609e17b84f6a7d30c80bfa610b5b4542f32a8a0d5447a12fb1366d7f01cc44a0220573a954c4518331561406f90300e8f3358f51928d43c212a8caed02de67eebee"); //
        byte[] pk = ByteUtil.hex("025476c2e83188368da1ff3e292e7acafcdb3566bb0ad253f62fc70f07aeee6357");
        {
            CoinAccount.KeyPrivate keyPrivate = new CoinAccount.KeyPrivate(ByteUtil.hex("619c335025c7f4012e556c2a58b2506e30b8511b53ade95ea316fd8c3286feb9"));
            byte[] bt = Secp256k1.signature(keyPrivate.getKey(), bytes1);
            assertTrue(Secp256k1.signatureVerify(pk, bt, bytes1));
        }
        boolean b = Secp256k1.signatureVerify(pk, snn, bytes1);
        assertTrue(b);
    }

    @Test
    public void signature() {
        // https://github.com/bitcoin/bips/blob/master/bip-0143.mediawiki#Native_P2WPKH
        // c37af31116d1b27caf68aae9e3ac82f1477929014d5b917657d0eb49478cb670
        byte[] hash = ByteUtil.hex("c304d56804b24a6801a77803281a497f5526e20f14e65df1006887fc57f0ee39");
        byte[] hashTwice = ByteUtil.hex("c37af31116d1b27caf68aae9e3ac82f1477929014d5b917657d0eb49478cb670");
        byte[] snn = ByteUtil.hex("304402203609e17b84f6a7d30c80bfa610b5b4542f32a8a0d5447a12fb1366d7f01cc44a0220573a954c4518331561406f90300e8f3358f51928d43c212a8caed02de67eebee"); //
        CoinAccount.KeyPrivate kp = new CoinAccount.KeyPrivate(ByteUtil.hex("619c335025c7f4012e556c2a58b2506e30b8511b53ade95ea316fd8c3286feb9"));
        System.out.println("signature : " + ByteUtil.hex(Secp256k1.signature(kp.getKey(), hash)));
        assertTrue(Secp256k1.signatureVerify(kp.getKeyPublic().getKey(), snn, hash));
    }

}