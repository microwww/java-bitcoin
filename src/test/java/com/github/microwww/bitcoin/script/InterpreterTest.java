package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.chain.*;
import com.github.microwww.bitcoin.chain.sign.*;
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
            tx1.deserialization(bf);
            assertEquals("0437cd7f8525ceed2324359c2d0ba26006d92d856a9c20fa0241106ee5a597c9", tx1.hash().toHexReverse256());
        }
        RawTransaction tx2 = new RawTransaction();
        {
            byte[] dt = ByteUtil.hex(strings.get(65));
            ByteBuf bf = Unpooled.copiedBuffer(dt);
            tx2.deserialization(bf);
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
            tx1.deserialization(bf);
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
        tx1.getTxIns()[1].setTxWitness(new byte[][]{ByteUtil.concat(snn, new byte[]{HashType.ALL.toByte()}), pubKey});

        byte[][] txWitness = tx1.getTxIns()[1].getTxWitness().get();
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

        in.nextTxIn(new TxOut(6_0000_0000L)).executor(tx.getTxIns()[1].getScript()).witnessPushStack()
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
            tx.deserialization(bf);
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
        in0.setTxWitness(new byte[][]{ByteUtil.concat(snn, new byte[]{HashType.ALL.toByte()}), pk});

        byte[][] txWitness = in0.getTxWitness().get();
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
        Interpreter executor = new Interpreter(tx)
                .executor(tx.getTxIns()[i].getScript())
                .witnessPushStack()
                .executor(ss);
        assertTrue(executor.isSuccess(true));

        i = 1;
        // getScriptsFromLastCodeSeparator
        byte[] hex = ByteUtil.hex("2200205d1b56b63d714eebe542309525f484b7e9d6f686b3781b6f61ef925d66d6f6a0");
        executor.nextTxIn(new TxOut(49_0000_0000L)).executor(tx.getTxIns()[i].getScript()).witnessPushStack().executor(hex);
        assertTrue(executor.isSuccess());
    }

    @Test
    void WitnessAnyOneCanPaySingleSignatureTransaction() {
        RawTransaction tx = readTx(97);
        int inIndex = 0;
        ByteBuf sn = Unpooled.buffer();
        tx.getTxOuts()[inIndex].write(sn);
        byte[] hashOutputs = ByteUtil.sha256sha256(ByteUtil.readAll(sn));
        assertEquals("b258eaf08c39fbe9fbac97c15c7e7adeb8df142b0df6f83e017f349c2b6fe3d2", ByteUtil.hex(hashOutputs));

        {
            byte[] preScript = ByteUtil.hex("27" + "0063ab68210392972e2eb617b2388771abe27235fd5ac44af8e61693261550447a4c3e39da98ac");
            SignatureTransaction st = new WitnessSingleSignatureTransaction(tx, inIndex, new TxOut(16777215L)).setAnyOneCanPay(true);
            byte[] bytes = st.data4signature(preScript);
            assertEquals("e9071e75e25b8a1e298a72f0d2e9f4f95a0f5cdf86a533cda597eb402ed13b3a", ByteUtil.hex(bytes));
            CoinAccount.KeyPrivate kr = new CoinAccount.KeyPrivate(ByteUtil.hex("f52b3484edd96598e02a9c89c4492e9c1e2031f471c49fd721fe68b3ce37780d"));
            byte[] sign = ByteUtil.hex("3045022100f6a10b8604e6dc910194b79ccfc93e1bc0ec7c03453caaa8987f7d6c3413566002206216229ede9b4d6ec2d325be245c5b508ff0339bf1794078e20bfe0babc7ffe6");
            boolean b = kr.getKeyPublic().signatureVerify(sign, bytes);
            assertTrue(b);
        }
        {
            inIndex++;
            byte[] preScript = ByteUtil.hex("24" + "68210392972e2eb617b2388771abe27235fd5ac44af8e61693261550447a4c3e39da98ac");
            SignatureTransaction st = new WitnessSingleSignatureTransaction(tx, inIndex, new TxOut(16777215L)).setAnyOneCanPay(true);
            byte[] bytes = st.data4signature(preScript);
            assertEquals("cd72f1f1a433ee9df816857fad88d8ebd97e09a75cd481583eb841c330275e54", ByteUtil.hex(bytes));
            CoinAccount.KeyPrivate kr = new CoinAccount.KeyPrivate(ByteUtil.hex("f52b3484edd96598e02a9c89c4492e9c1e2031f471c49fd721fe68b3ce37780d"));
            byte[] sign = ByteUtil.hex("30440220032521802a76ad7bf74d0e2c218b72cf0cbc867066e2e53db905ba37f130397e02207709e2188ed7f08f4c952d9d13986da504502b8c3be59617e043552f506c46ff");// 83");
            boolean b = kr.getKeyPublic().signatureVerify(sign, bytes);
            assertTrue(b);
        }
    }

    @Test
    public void testMultiSigWitness() {
        RawTransaction tx = readTx(101);
        int inIndex = 0;
        TxOut txOut = new TxOut(987654321L);
        // 这是 6-6 的脚本
        String m6_6 = "cf" +
                "56" +
                "210307b8ae49ac90a048e9b53357a2354b3334e9c8bee813ecb98e99a7e07e8c3ba3" +
                "2103b28f0c28bfab54554ae8c658ac5c3e0ce6e79ad336331f78c428dd43eea8449b" +
                "21034b8113d703413d57761b8b9781957b8c0ac1dfe69f492580ca4195f50376ba4a" +
                "21033400f6afecb833092a9a21cfdf1ed1376e58c5d1f47de74683123987e967a8f4" +
                "2103a6d48b1131e94ba04d9737d61acdaa1322008af9602b3b14862c07a1789aac16" +
                "2102d8b661b0b3302ee2f162b09e07a55ad5dfbe673a9f01d9f0c19617681024306b" +
                "56" +
                "ae" ;
        if (true) { // ALL
            SignatureTransaction st = new WitnessHashAllSignatureTransaction(tx, inIndex, txOut);
            byte[] bytes = st.data4signature(ByteUtil.hex(m6_6));
            assertEquals("185c0be5263dce5b4bb50a047973c1b6272bfbd0103a89444597dc40b248ee7c", ByteUtil.hex(bytes));
            CoinAccount.KeyPrivate kr = new CoinAccount.KeyPrivate(ByteUtil.hex("730fff80e1413068a05b57d6a58261f07551163369787f349438ea38ca80fac6"));
            String sign = "304402206ac44d672dac41f9b00e28f4df20c52eeb087207e8d758d76d92c6fab3b73e2b0220367750dbbe19290069cba53d096f44530e4f98acaa594810388cf7409a1870ce" ;
            //+"01"; ALL
            boolean b = kr.getKeyPublic().signatureVerify(ByteUtil.hex(sign), bytes);
            assertTrue(b);
        }
        if (true) {// NONE
            SignatureTransaction st = new WitnessNoneSignatureTransaction(tx, inIndex, txOut);
            byte[] bytes = st.data4signature(ByteUtil.hex(m6_6));
            assertEquals("e9733bc60ea13c95c6527066bb975a2ff29a925e80aa14c213f686cbae5d2f36", ByteUtil.hex(bytes));
            CoinAccount.KeyPrivate kr = new CoinAccount.KeyPrivate(ByteUtil.hex("11fa3d25a17cbc22b29c44a484ba552b5a53149d106d3d853e22fdd05a2d8bb3"));
            String sign = "3044022068c7946a43232757cbdf9176f009a928e1cd9a1a8c212f15c1e11ac9f2925d9002205b75f937ff2f9f3c1246e547e54f62e027f64eefa2695578cc6432cdabce2715" ;
            // + "02"; NONE
            boolean b = kr.getKeyPublic().signatureVerify(ByteUtil.hex(sign), bytes);
            assertTrue(b);
        }
        if (true) {// SINGLE
            SignatureTransaction st = new WitnessSingleSignatureTransaction(tx, inIndex, txOut);
            byte[] bytes = st.data4signature(ByteUtil.hex(m6_6));
            assertEquals("1e1f1c303dc025bd664acb72e583e933fae4cff9148bf78c157d1e8f78530aea", ByteUtil.hex(bytes));
            CoinAccount.KeyPrivate kr = new CoinAccount.KeyPrivate(ByteUtil.hex("77bf4141a87d55bdd7f3cd0bdccf6e9e642935fec45f2f30047be7b799120661"));
            String sign = "3044022059ebf56d98010a932cf8ecfec54c48e6139ed6adb0728c09cbe1e4fa0915302e022007cd986c8fa870ff5d2b3a89139c9fe7e499259875357e20fcbb15571c767954" ;
            // + "03"; SINGLE
            boolean b = kr.getKeyPublic().signatureVerify(ByteUtil.hex(sign), bytes);
            assertTrue(b);
        }
        if (true) {// ALL|ANYONECANPAY
            SignatureTransaction st = new WitnessHashAllSignatureTransaction(tx, inIndex, txOut).setAnyOneCanPay(true);
            byte[] bytes = st.data4signature(ByteUtil.hex(m6_6));
            assertEquals("2a67f03e63a6a422125878b40b82da593be8d4efaafe88ee528af6e5a9955c6e", ByteUtil.hex(bytes));
            CoinAccount.KeyPrivate kr = new CoinAccount.KeyPrivate(ByteUtil.hex("14af36970f5025ea3e8b5542c0f8ebe7763e674838d08808896b63c3351ffe49"));
            String sign = "3045022100fbefd94bd0a488d50b79102b5dad4ab6ced30c4069f1eaa69a4b5a763414067e02203156c6a5c9cf88f91265f5a942e96213afae16d83321c8b31bb342142a14d163" ;
            // + "81"; ALL|ANYONECANPAY
            boolean b = kr.getKeyPublic().signatureVerify(ByteUtil.hex(sign), bytes);
            assertTrue(b);
        }
        if (true) {// NONE|ANYONECANPAY
            SignatureTransaction st = new WitnessNoneSignatureTransaction(tx, inIndex, txOut).setAnyOneCanPay(true);
            byte[] bytes = st.data4signature(ByteUtil.hex(m6_6));
            assertEquals("781ba15f3779d5542ce8ecb5c18716733a5ee42a6f51488ec96154934e2c890a", ByteUtil.hex(bytes));
            CoinAccount.KeyPrivate kr = new CoinAccount.KeyPrivate(ByteUtil.hex("fe9a95c19eef81dde2b95c1284ef39be497d128e2aa46916fb02d552485e0323"));
            String sign = "3045022100a5263ea0553ba89221984bd7f0b13613db16e7a70c549a86de0cc0444141a407022005c360ef0ae5a5d4f9f2f87a56c1546cc8268cab08c73501d6b3be2e1e1a8a08" ;
            // + "82"; NONE|ANYONECANPAY
            boolean b = kr.getKeyPublic().signatureVerify(ByteUtil.hex(sign), bytes);
            assertTrue(b);
        }
        if (true) {// SINGLE|ANYONECANPAY
            SignatureTransaction st = new WitnessSingleSignatureTransaction(tx, inIndex, txOut).setAnyOneCanPay(true);
            byte[] bytes = st.data4signature(ByteUtil.hex(m6_6));
            assertEquals("511e8e52ed574121fc1b654970395502128263f62662e076dc6baf05c2e6a99b", ByteUtil.hex(bytes));
            CoinAccount.KeyPrivate kr = new CoinAccount.KeyPrivate(ByteUtil.hex("428a7aee9f0c2af0cd19af3cf1c78149951ea528726989b2e83e4778d2c3f890"));
            String sign = "30440220525406a1482936d5a21888260dc165497a90a15669636d8edca6b9fe490d309c022032af0c646a34a44d1f4576bf6a4a74b67940f8faa84c7df9abe12a01a11e2b47" ;
            // + "83"; SINGLE|ANYONECANPAY
            boolean b = kr.getKeyPublic().signatureVerify(ByteUtil.hex(sign), bytes);
            assertTrue(b);
        }
    }

    @Test
    public void testMultiSigWitnessVerify() {
        RawTransaction tx = readTx(104);
        int inIndex = 0;
        TxOut txOut = new TxOut(987654321L);

        Interpreter exe = new Interpreter(tx).indexTxIn(inIndex, txOut)
                .witnessPushStack()
                .printStack()
                .executor(tx.getTxIns()[inIndex].getScript())
                .printStack()
                .executor(ByteUtil.hex("a9149993a429037b5d912407a71c252019287b8d27a587"))
                .printStack();
        assertTrue(exe.isSuccess(true));
        assertArrayEquals(new byte[]{}, exe.stack.pop());
        assertTrue(exe.stack.isEmpty());
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
        tx.deserialization(bf);
        return tx;
    }
}