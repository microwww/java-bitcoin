package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.chain.HashType;
import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxIn;
import com.github.microwww.bitcoin.chain.TxOut;
import com.github.microwww.bitcoin.chain.sign.HashAllSignatureTransaction;
import com.github.microwww.bitcoin.chain.sign.WitnessHashAllSignatureTransaction;
import com.github.microwww.bitcoin.chain.sign.WitnessSingleSignatureTransaction;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.util.ClassPath;
import com.github.microwww.bitcoin.wallet.CoinAccount;
import com.github.microwww.bitcoin.wallet.Env;
import com.github.microwww.bitcoin.wallet.Secp256k1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {
    private static List<String> strings = Collections.EMPTY_LIST;

    @BeforeAll
    public static void init() {
        strings = ClassPath.readClassPathFile("/data/line-data.txt");
    }

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
        RawTransaction tx1 = readTx(transaction);
        RawTransaction tx2 = readTx(txOut);

        Interpreter interpreter = new Interpreter(tx1).indexTxIn(txInIndex)
                .executor(tx1.getTxIns()[txInIndex].getScript()).executor(tx2.getTxOuts()[txOutIndex].getScriptPubKey());
        assertTrue(interpreter.isSuccess());
    }

    @Test
    public void testV() {
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

        assertTrue(interpreter.isSuccess());
    }

    // https://github.com/bitcoin/bips/blob/master/bip-0143.mediawiki#Native_P2WPKH
    // c37af31116d1b27caf68aae9e3ac82f1477929014d5b917657d0eb49478cb670
    @Test
    public void P2WPKH() {
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
        long amount = Unpooled.buffer().writeLong(0x0046c32300000000L).readLongLE();
        sn
                .writeIntLE(tx1.getVersion())
                .writeBytes(hashIn)
                .writeBytes(hashSeq)
                // outpoint
                .writeBytes(tx1.getTxIns()[nIn].getPreTxHash().fill256bit()).writeIntLE(tx1.getTxIns()[nIn].getPreTxOutIndex())
                .writeBytes(scriptCode)
                .writeLongLE(amount)
                .writeIntLE(tx1.getTxIns()[nIn].getSequence().intValue())
                .writeBytes(hashOut)
                .writeIntLE(tx1.getLockTime().intValue())
                .writeIntLE(1);
        byte[] bytes = ByteUtil.readAll(sn);
        byte[] sha256 = ByteUtil.sha256sha256(bytes);
        byte[] pk = ByteUtil.hex("025476c2e83188368da1ff3e292e7acafcdb3566bb0ad253f62fc70f07aeee6357");
        CoinAccount.KeyPrivate keyPrivate = new CoinAccount.KeyPrivate(ByteUtil.hex("619c335025c7f4012e556c2a58b2506e30b8511b53ade95ea316fd8c3286feb9"));
        {
            byte[] bt = Secp256k1.signature(keyPrivate.getKey(), sha256);
            assertTrue(Secp256k1.signatureVerify(pk, bt, sha256));
            CoinAccount.KeyPrivate kr = new CoinAccount.KeyPrivate(ByteUtil.hex("bbc27228ddcb9209d7fd6f36b02f7dfa6252af40bb2f1cbc7a557da8027ff866"));
            new HashAllSignatureTransaction(tx1, 0).writeSignatureScript(
                    kr.getKey(),
                    ByteUtil.hex("2103c9f4836b9a4f77fc0d81f7bcb01b7f1b35916864b9476c241ce9fc198bd25432ac")
            );
        }

        byte[] dt = new WitnessHashAllSignatureTransaction(tx1, nIn, new TxOut().setValue(amount)).data4signature(scriptCode);
        assertArrayEquals(dt, sha256);

        byte[] snn = ByteUtil.hex("304402203609e17b84f6a7d30c80bfa610b5b4542f32a8a0d5447a12fb1366d7f01cc44a0220573a954c4518331561406f90300e8f3358f51928d43c212a8caed02de67eebee"); //
        boolean b = Secp256k1.signatureVerify(pk, snn, sha256);
        assertTrue(b);

        // 01000000000102fff7f7881a8099afa6940d42d1e7f6362bec38171ea3edf433541db4e4ad969f00000000494830450221008b9d1dc26ba6a9cb62127b02742fa9d754cd3bebf337f7a55d114c8e5cdd30be022040529b194ba3f9281a99f2b1c0a19c0489bc22ede944ccf4ecbab4cc618ef3ed01eeffffffef51e1b804cc89d182d279655c3aa89e815b1b309fe287d9b2b55d57b90ec68a0100000000ffffffff02202cb206000000001976a9148280b37df378db99f66f85c95a783a76ac7a6d5988ac9093510d000000001976a9143bde42dbee7e4dbe6a21b2d50ce2f0167faa815988ac000247304402203609e17b84f6a7d30c80bfa610b5b4542f32a8a0d5447a12fb1366d7f01cc44a0220573a954c4518331561406f90300e8f3358f51928d43c212a8caed02de67eebee0121025476c2e83188368da1ff3e292e7acafcdb3566bb0ad253f62fc70f07aeee635711000000

        tx1.getTxIns()[1].setScript(new byte[]{});

        byte[] pubKey = keyPrivate.getKeyPublic().getKey();
        tx1.getTxIns()[1].setTxWitness(new byte[][]{ByteUtil.concat(snn, new byte[]{HashType.ALL.TYPE}), pubKey});

        byte[][] txWitness = tx1.getTxIns()[1].getTxWitness();
        assertEquals(2, txWitness.length);
        assertEquals(71, txWitness[0].length);
        assertArrayEquals(ByteUtil.concat(snn, new byte[]{1}), txWitness[0]);
        assertArrayEquals(ByteUtil.hex("025476c2e83188368da1ff3e292e7acafcdb3566bb0ad253f62fc70f07aeee6357"), txWitness[1]);
    }

    @Test
    public void verifyP2WPKH() {
        RawTransaction tx = readTx(91);
        Interpreter in = new Interpreter(tx).executor(tx.getTxIns()[0].getScript())
                .executor(ByteUtil.hex("2103c9f4836b9a4f77fc0d81f7bcb01b7f1b35916864b9476c241ce9fc198bd25432ac"));

        assertTrue(in.isSuccess(true));

        in.nextTxIn(new TxOut( 6_0000_0000L)).executor(tx.getTxIns()[1].getScript()).witnessPushStack()
                .executor(ByteUtil.hex("1976a9141d0f172a0ecb48aee1be1f2687d2963ae33f71a188ac"), 1);
        assertTrue(in.isSuccess());
    }

    @Test
    public void signature() {
        // https://github.com/bitcoin/bips/blob/master/bip-0143.mediawiki#Native_P2WPKH
        // c37af31116d1b27caf68aae9e3ac82f1477929014d5b917657d0eb49478cb670
        //byte[] hash = ByteUtil.hex("c304d56804b24a6801a77803281a497f5526e20f14e65df1006887fc57f0ee39");
        byte[] hashTwice = ByteUtil.hex("c37af31116d1b27caf68aae9e3ac82f1477929014d5b917657d0eb49478cb670");
        byte[] snn = ByteUtil.hex("304402203609e17b84f6a7d30c80bfa610b5b4542f32a8a0d5447a12fb1366d7f01cc44a0220573a954c4518331561406f90300e8f3358f51928d43c212a8caed02de67eebee"); //
        CoinAccount.KeyPrivate kp = new CoinAccount.KeyPrivate(ByteUtil.hex("619c335025c7f4012e556c2a58b2506e30b8511b53ade95ea316fd8c3286feb9"));
        //System.out.println("signature : " + ByteUtil.hex(Secp256k1.signature(kp.getKey(), hashTwice)));
        assertTrue(Secp256k1.signatureVerify(kp.getKeyPublic().getKey(), snn, hashTwice));
    }

    // https://github.com/bitcoin/bips/blob/master/bip-0143.mediawiki#p2sh-p2wpkh
    @Test
    public void P2SH_P2WPKH() { // 同 P2WPKH
        List<String> strings = ClassPath.readClassPathFile("/data/line-data.txt");
        RawTransaction tx = new RawTransaction();
        {
            byte[] dt = ByteUtil.hex(strings.get(81));
            ByteBuf bf = Unpooled.copiedBuffer(dt);
            tx.read(bf);
            assertEquals("321a59707939041eeb0d524f34432c0c46ca3920f0964e6c23697581f176b6c0", tx.hash().toHexReverse256());
        }

        ByteBuf txins = Unpooled.buffer();
        ByteBuf txinSeq = Unpooled.buffer();
        for (TxIn txIn : tx.getTxIns()) {
            txins.writeBytes(txIn.getPreTxHash().fill256bit()).writeIntLE(txIn.getPreTxOutIndex());
            txinSeq.writeIntLE(txIn.getSequence().intValue());
        }
        ByteBuf txout = Unpooled.buffer();
        for (TxOut out : tx.getTxOuts()) {
            out.write(txout);
        }
        byte[] hashIn = ByteUtil.sha256sha256(ByteUtil.readAll(txins));
        assertArrayEquals(ByteUtil.hex("b0287b4a252ac05af83d2dcef00ba313af78a3e9c329afa216eb3aa2a7b4613a"), hashIn);
        byte[] hashSeq = ByteUtil.sha256sha256(ByteUtil.readAll(txinSeq));
        assertArrayEquals(ByteUtil.hex("18606b350cd8bf565266bc352f0caddcf01e8fa789dd8a15386327cf8cabe198"), hashSeq);
        byte[] hashOut = ByteUtil.sha256sha256(ByteUtil.readAll(txout));
        assertArrayEquals(ByteUtil.hex("de984f44532e2173ca0d64314fcefe6d30da6f8cf27bafa706da61df8a226c83"), hashOut);
        ByteBuf sn = Unpooled.buffer();
        int nIn = 0;
        byte[] scriptCode = ByteUtil.hex("1976a91479091972186c449eb1ded22b78e40d009bdf008988ac");
        long amount = Unpooled.buffer().writeLong(0x00ca9a3b00000000L).readLongLE();
        sn
                .writeIntLE(tx.getVersion())
                .writeBytes(hashIn)
                .writeBytes(hashSeq)
                // outpoint
                .writeBytes(tx.getTxIns()[nIn].getPreTxHash().fill256bit()).writeIntLE(tx.getTxIns()[nIn].getPreTxOutIndex())
                .writeBytes(scriptCode)
                .writeLongLE(amount)
                .writeIntLE(tx.getTxIns()[nIn].getSequence().intValue())
                .writeBytes(hashOut)
                .writeIntLE(tx.getLockTime().intValue())
                .writeIntLE(HashType.ALL.TYPE);
        byte[] bytes = ByteUtil.readAll(sn);
        byte[] sha256 = ByteUtil.sha256sha256(bytes);
        assertArrayEquals(ByteUtil.hex("64f3b0f4dd2bb3aa1ce8566d220cc74dda9df97d8490cc81d89d735c92e59fb6"), sha256);
        CoinAccount.KeyPrivate keyPrivate = new CoinAccount.KeyPrivate(ByteUtil.hex("eb696a065ef48a2192da5b28b694f87544b30fae8327c4510137a922f32c6dcf"));
        byte[] pk = keyPrivate.getKeyPublic().getKey();
        assertArrayEquals(ByteUtil.hex("03ad1d8e89212f0b92c74d23bb710c00662ad1470198ac48c43f7d6f93a2a26873"), pk);

        byte[] dt = new WitnessHashAllSignatureTransaction(tx, nIn, new TxOut().setValue(amount)).data4signature(scriptCode);
        assertArrayEquals(dt, sha256);

        byte[] snn = ByteUtil.hex("3044022047ac8e878352d3ebbde1c94ce3a10d057c24175747116f8288e5d794d12d482f0220217f36a485cae903c713331d877c1f64677e3622ad4010726870540656fe9dcb"); //
        boolean b = Secp256k1.signatureVerify(pk, snn, sha256);
        assertTrue(b);

        // 01000000000102fff7f7881a8099afa6940d42d1e7f6362bec38171ea3edf433541db4e4ad969f00000000494830450221008b9d1dc26ba6a9cb62127b02742fa9d754cd3bebf337f7a55d114c8e5cdd30be022040529b194ba3f9281a99f2b1c0a19c0489bc22ede944ccf4ecbab4cc618ef3ed01eeffffffef51e1b804cc89d182d279655c3aa89e815b1b309fe287d9b2b55d57b90ec68a0100000000ffffffff02202cb206000000001976a9148280b37df378db99f66f85c95a783a76ac7a6d5988ac9093510d000000001976a9143bde42dbee7e4dbe6a21b2d50ce2f0167faa815988ac000247304402203609e17b84f6a7d30c80bfa610b5b4542f32a8a0d5447a12fb1366d7f01cc44a0220573a954c4518331561406f90300e8f3358f51928d43c212a8caed02de67eebee0121025476c2e83188368da1ff3e292e7acafcdb3566bb0ad253f62fc70f07aeee635711000000

        TxIn in0 = tx.getTxIns()[0];
        in0.setScript(new byte[]{});
        in0.setTxWitness(new byte[][]{ByteUtil.concat(snn, new byte[]{HashType.ALL.TYPE}), pk});

        byte[][] txWitness = in0.getTxWitness();
        assertEquals(2, txWitness.length);
        assertEquals(71, txWitness[0].length);
        assertArrayEquals(ByteUtil.concat(snn, new byte[]{1}), txWitness[0]);
        assertArrayEquals(ByteUtil.hex("03ad1d8e89212f0b92c74d23bb710c00662ad1470198ac48c43f7d6f93a2a26873"), txWitness[1]);
    }

    // https://github.com/bitcoin/bips/blob/master/bip-0143.mediawiki#native-p2wsh
    @Test
    public void P2WSH() { // OP_CODESEPARATOR
        RawTransaction tx = readTx(84);
        CoinAccount.KeyPrivate rp = new CoinAccount.KeyPrivate(ByteUtil.hex("b8f28a772fccbf9b4f58a4f027e07dc2e35e7cd80529975e292ea34f84c4580c"));
        byte[] ss = ByteUtil.hex("21036d5c20fa14fb2f635474c1dc4ef5909d4568e5569b79fc94d3448486e14685f8ac");
        // The first input comes from an ordinary P2PK:
        int index = 0;
        byte[] data = new HashAllSignatureTransaction(tx, index).data4signature(ss);
        byte[] sign = ByteUtil.hex("304402200af4e47c9b9629dbecc21f73af989bdaa911f7e6f6c2e9394588a3aa68f81e9902204f3fcf6ade7e5abb1295b6774c8e0abd94ae62217367096bc02ee5e435b67da2");
        boolean rs = rp.getKeyPublic().signatureVerify(sign, data);
        assertTrue(rs);
        new HashAllSignatureTransaction(tx, 0).writeSignatureScript(rp.getKey(), ss);

        // The second input comes from a native P2WSH witness program:
        index = 1;
        ss = ByteUtil.hex("4721026dccc749adc2a9d0d89497ac511f760f45c47dc5ed9cf352a58ac706453880aeadab210255a9626aebf5e29c0e6538428ba0d1dcf6ca98ffdf086aa8ced5e0d0215ea465ac");
        data = new WitnessSingleSignatureTransaction(tx, index, new TxOut().setValue(49_0000_0000L)).data4signature(ss);
        rp = new CoinAccount.KeyPrivate(ByteUtil.hex("8e02b539b1500aa7c81cf3fed177448a546f19d2be416c0c61ff28e577d8d0cd"));
        sign = ByteUtil.hex("3044022027dc95ad6b740fe5129e7e62a75dd00f291a2aeb1200b84b09d9e3789406b6c002201a9ecd315dd6a0e632ab20bbb98948bc0c6fb204f2c286963bb48517a7058e27");
        rs = Secp256k1.signatureVerify(rp.getKeyPublic().getKey(), sign, data);
        assertTrue(rs);
        // ?? 不需要 amount ?
        new WitnessHashAllSignatureTransaction(tx, 0, new TxOut().setValue(49_0000_0000L)).writeSignatureScript(rp.getKey(), ss);

        index = 1;
        ss = ByteUtil.hex("23210255a9626aebf5e29c0e6538428ba0d1dcf6ca98ffdf086aa8ced5e0d0215ea465ac");
        data = new WitnessSingleSignatureTransaction(tx, index, new TxOut().setValue(49_0000_0000L)).data4signature(ss);
        rp = new CoinAccount.KeyPrivate(ByteUtil.hex("86bf2ed75935a0cbef03b89d72034bb4c189d381037a5ac121a70016db8896ec"));
        sign = ByteUtil.hex("304402200de66acf4527789bfda55fc5459e214fa6083f936b430a762c629656216805ac0220396f550692cd347171cbc1ef1f51e15282e837bb2b30860dc77c8f78bc8501e5");
        rs = Secp256k1.signatureVerify(rp.getKeyPublic().getKey(), sign, data);
        assertTrue(rs);
    }

    @Test
    void txP2WSH() {
        RawTransaction tx = readTx(94);
        byte[] ss = ByteUtil.hex("21036d5c20fa14fb2f635474c1dc4ef5909d4568e5569b79fc94d3448486e14685f8ac");
        int i = 0;
        Interpreter executor = new Interpreter(tx).executor(tx.getTxIns()[i].getScript()).witnessPushStack().executor(ss);
        assertTrue(executor.isSuccess(true));

        i = 1;
        byte[] hex = ByteUtil.hex("00205d1b56b63d714eebe542309525f484b7e9d6f686b3781b6f61ef925d66d6f6a0");
        executor.nextTxIn(new TxOut(49_0000_0000L)).executor(tx.getTxIns()[i].getScript()).witnessPushStack().executor(hex);
        assertTrue(executor.isSuccess());
    }

    @Test
    public void exeP2WPKH() {
        RawTransaction tx = readTx(87);
        // CScript witScriptPubkey = CScript() << OP_DUP << OP_HASH160 << ToByteVector(pubkeyHash) << OP_EQUALVERIFY << OP_CHECKSIG;
        assertEquals("0517e82c799730212b226676dd2feb3f0e6b2ba808a32f2968b78399b209df43", tx.hash().toHexReverse256());
        RawTransaction preouts = readTx(88);
        assertEquals("887e1d2a500264d5f5329c623fa64604415ae7627cb17097d07769a932e2df87", preouts.hash().toHexReverse256());
        int in = 0;
        int out = tx.getTxIns()[in].getPreTxOutIndex();
        TxOut txOut = preouts.getTxOuts()[out];
        Interpreter interpreter = new Interpreter(tx).indexTxIn(0, txOut).witnessPushStack()
                .executor(tx.getTxIns()[in].getScript())
                .executor(txOut.getScriptPubKey());
        assertTrue(interpreter.isSuccess());
    }

    private static RawTransaction readTx(int index) {
        RawTransaction tx = new RawTransaction();
        byte[] dt = ByteUtil.hex(strings.get(index));
        ByteBuf bf = Unpooled.copiedBuffer(dt);
        tx.read(bf);
        return tx;
    }
}