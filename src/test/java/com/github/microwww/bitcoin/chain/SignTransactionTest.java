package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.script.Interpreter;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.util.ClassPath;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SignTransactionTest {

    @Test
    void signature() {
        byte[] hex = ByteUtil.hex("01000000ef546acf4a020de3898d1b8956176bb507e6211b5ed3619cd08b6ea7e2a09d4100000000000000000000000000000000000000000000000000000000000000000815cf020f013ed6cf91d29f4202e8a58726b1ac6c79da47c23d1bee0a6925f8000000004721026dccc749adc2a9d0d89497ac511f760f45c47dc5ed9cf352a58ac706453880aeadab210255a9626aebf5e29c0e6538428ba0d1dcf6ca98ffdf086aa8ced5e0d0215ea465ac0011102401000000ffffffff00000000000000000000000000000000000000000000000000000000000000000000000003000000");
        byte[] bytes = ByteUtil.sha256sha256(hex);
        assertArrayEquals(bytes, ByteUtil.hex("82dde6e4f1e94d02c2b7ad03d2115d691f48d064e9d52f58194a6637e4194391"));
    }

    @Test
    void testHashType0() {
        ByteBuf bf = Unpooled.buffer();
        RawTransaction from = new RawTransaction();
        {
            byte[] hex = ByteUtil.hex("01000000017fd8dfdb54b5212c4e3151a39f4ffe279fd7f238d516a2ca731529c095d97449010000008b483045022100b6a7fe5eea81894bbdd0df61043e42780543457fa5581ac1af023761a098e92202201d4752785be5f9d1b9f8d362b8cf3b05e298a78c4abff874b838bb500dcf2a120141042e3c4aeac1ffb1c86ce3621afb1ca92773e02badf0d4b1c836eb26bd27d0c2e59ffec3d6ab6b8bbeca81b0990ab5224ebdd73696c4255d1d0c6b3c518a1a053effffffff01404b4c00000000001976a914dc44b1164188067c3a32d4780f5996fa14a4f2d988ac00000000");
            from.deserialization(bf.clear().writeBytes(hex));
            assertEquals(from.hash().toHexReverse256(), "406b2b06bcd34d3c8733e6b79f7a394c8a431fbf4ff5ac705c93f4076bb77602");
        }

        RawTransaction tx = new RawTransaction();
        {
            byte[] hex = ByteUtil.hex("01000000010276b76b07f4935c70acf54fbf1f438a4c397a9fb7e633873c4dd3bc062b6b40000000008c493046022100d23459d03ed7e9511a47d13292d3430a04627de6235b6e51a40f9cd386f2abe3022100e7d25b080f0bb8d8d5f878bba7d54ad2fda650ea8d158a33ee3cbd11768191fd004104b0e2c879e4daf7b9ab68350228c159766676a14f5815084ba166432aab46198d4cca98fa3e9981d0a90b2effc514b76279476550ba3663fdcaff94c38420e9d5000000000100093d00000000001976a9149a7b0f3b80c6baaeedce0a0842553800f832ba1f88ac00000000");
            bf.writeBytes(hex);
            tx.deserialization(bf);
            assertEquals(tx.hash().toHexReverse256(), "c99c49da4c38af669dea436d3e73780dfdb6c1ecf9958baa52960e8baee30e73");
        }

        int in = 0;
        assertEquals(tx.getTxIns()[in].getPreTxHash(), from.hash());
        TxOut txOut = from.getTxOuts()[tx.getTxIns()[in].getPreTxOutIndex()];
        Interpreter interpreter = new Interpreter(tx).indexTxIn(in, txOut)//.witnessPushStack()
                .executor(tx.getTxIns()[in].getScript())
                .executor(txOut.getScriptPubKey());
        assertTrue(interpreter.isSuccess());
    }

    // JDK bug : https://bugs.openjdk.java.net/browse/JDK-8175251
    @Test
    public void test0xFB0A1D8D34FA5537E() {
        ByteBuf bf = Unpooled.buffer();

        RawTransaction tx = new RawTransaction();
        {
            byte[] hex = ByteUtil.hex("01000000012316aac445c13ff31af5f3d1e2cebcada83e54ba10d15e01f49ec28bddc285aa000000008e4b3048022200002b83d59c1d23c08efd82ee0662fec23309c3adbcbd1f0b8695378db4b14e736602220000334a96676e58b1bb01784cb7c556dd8ce1c220171904da22e18fe1e7d1510db5014104d0fe07ff74c9ef5b00fed1104fad43ecf72dbab9e60733e4f56eacf24b20cf3b8cd945bcabcc73ba0158bf9ce769d43e94bd58c5c7e331a188922b3fe9ca1f5affffffff01c0c62d00000000001976a9147a2a3b481ca80c4ba7939c54d9278e50189d94f988ac00000000");
            tx.deserialization(bf.clear().writeBytes(hex));
            assertEquals(tx.hash().toHexReverse256(), "fb0a1d8d34fa5537e461ac384bac761125e1bfa7fec286fa72511240fa66864d");
        }
        RawTransaction from = new RawTransaction();
        {
            byte[] hex = ByteUtil.hex("0100000001ba988c49d024d5ec33b49f74071b2157b1530e1301c3210d92c5dc08e04b63d0010000008b48304502200f18c2d1fe6513b90f44513e975e05cc498e7f5a565b46c65b1d448734392c6f022100917766d14f2e9933eb269c83b3ad440ed8432da8beb5733f34046509e48b1d850141049ba39856eec011b79f1acb997760ed9d3f90d477077d17df2571d94b2fa2137bf0976d786b6aabc903746e269628b2c28e4b5db753845e5713a48ee7d6b97aafffffffff01c0c62d00000000001976a9147a2a3b481ca80c4ba7939c54d9278e50189d94f988ac00000000");
            bf.writeBytes(hex);
            from.deserialization(bf);
            assertEquals(from.hash().toHexReverse256(), "aa85c2dd8bc29ef4015ed110ba543ea8adbccee2d1f3f51af33fc145c4aa1623");
        }
        int in = 0;
        assertEquals(tx.getTxIns()[in].getPreTxHash(), from.hash());
        TxOut txOut = from.getTxOuts()[tx.getTxIns()[in].getPreTxOutIndex()];
        Interpreter interpreter = new Interpreter(tx).indexTxIn(in, txOut)//.witnessPushStack()
                .executor(tx.getTxIns()[in].getScript())
                .executor(txOut.getScriptPubKey());
        assertTrue(interpreter.isSuccess());
    }
    //         strings = ClassPath.readClassPathFile("/data/online.data.txt");

    @Test
    public void test_OP_NOP_0x8ebe1df6ebf008f7ec42ccd022478c9afaec3ca0444322243b745aa2e317c272() {
        ByteBuf bf = Unpooled.buffer();

        RawTransaction tx = new RawTransaction();
        {
            String x = ClassPath.readClassPathFile("/data/online.data.txt").get(29);
            byte[] hex = ByteUtil.hex(x);
            tx.deserialization(bf.clear().writeBytes(hex));
            assertEquals(tx.hash().toHexReverse256(), "8ebe1df6ebf008f7ec42ccd022478c9afaec3ca0444322243b745aa2e317c272");
        }
        RawTransaction from = new RawTransaction();
        {
            String x = ClassPath.readClassPathFile("/data/online.data.txt").get(31);
            byte[] hex = ByteUtil.hex(x);
            from.deserialization(bf.clear().writeBytes(hex));
            assertEquals(from.hash().toHexReverse256(), "db3f14e43fecc80eb3e0827cecce85b3499654694d12272bf91b1b2b8c33b5cb");
        }
        int in = 89;
        assertEquals(tx.getTxIns()[in].getPreTxHash(), from.hash());
        TxOut txOut = from.getTxOuts()[tx.getTxIns()[in].getPreTxOutIndex()];
        Interpreter interpreter = new Interpreter(tx).indexTxIn(in, txOut)//.witnessPushStack()
                .executor(tx.getTxIns()[in].getScript())
                .executor(txOut.getScriptPubKey());
        assertTrue(interpreter.isSuccess());
    }

    @Test
    public void test_OP_CHECKMULTISIG_0xEB3B82C0884E3EFA6D8B0BE55B() {
        ByteBuf bf = Unpooled.buffer();

        RawTransaction tx = new RawTransaction();
        {
            byte[] hex = ByteUtil.hex("01000000024de8b0c4c2582db95fa6b3567a989b664484c7ad6672c85a3da413773e63fdb8000000006b48304502205b282fbc9b064f3bc823a23edcc0048cbb174754e7aa742e3c9f483ebe02911c022100e4b0b3a117d36cab5a67404dddbf43db7bea3c1530e0fe128ebc15621bd69a3b0121035aa98d5f77cd9a2d88710e6fc66212aff820026f0dad8f32d1f7ce87457dde50ffffffff4de8b0c4c2582db95fa6b3567a989b664484c7ad6672c85a3da413773e63fdb8010000006f004730440220276d6dad3defa37b5f81add3992d510d2f44a317fd85e04f93a1e2daea64660202200f862a0da684249322ceb8ed842fb8c859c0cb94c81e1c5308b4868157a428ee01ab51210232abdc893e7f0631364d7fd01cb33d24da45329a00357b3a7886211ab414d55a51aeffffffff02e0fd1c00000000001976a914380cb3c594de4e7e9b8e18db182987bebb5a4f7088acc0c62d000000000017142a9bc5447d664c1d0141392a842d23dba45c4f13b17500000000");
            tx.deserialization(bf.clear().writeBytes(hex));
            assertEquals(tx.hash().toHexReverse256(), "eb3b82c0884e3efa6d8b0be55b4915eb20be124c9766245bcc7f34fdac32bccb");
        }
        RawTransaction from = new RawTransaction();
        {
            byte[] hex = ByteUtil.hex("01000000017ea56cd68c74b4cd1a2f478f361b8a67c15a6629d73d95ef21d96ae213eb5b2d010000006a4730440220228e4deb3bc5b47fc526e2a7f5e9434a52616f8353b55dbc820ccb69d5fbded502206a2874f7f84b20015614694fe25c4d76f10e31571f03c240e3e4bbf1f9985be201210232abdc893e7f0631364d7fd01cb33d24da45329a00357b3a7886211ab414d55affffffff0230c11d00000000001976a914709dcb44da534c550dacf4296f75cba1ba3b317788acc0c62d000000000017142a9bc5447d664c1d0141392a842d23dba45c4f13b17500000000");
            bf.writeBytes(hex);
            from.deserialization(bf);
            assertEquals(from.hash().toHexReverse256(), "b8fd633e7713a43d5ac87266adc78444669b987a56b3a65fb92d58c2c4b0e84d");
        }
        int in = 1;
        assertEquals(tx.getTxIns()[in].getPreTxHash(), from.hash());
        TxOut txOut = from.getTxOuts()[tx.getTxIns()[in].getPreTxOutIndex()];
        Interpreter interpreter = new Interpreter(tx).indexTxIn(in, txOut)//.witnessPushStack()
                .executor(tx.getTxIns()[in].getScript())
                .executor(txOut.getScriptPubKey());
        assertTrue(interpreter.isSuccess());
    }

    @Test
    public void test_OP_CHECKMULTISIG_0x1CC1ECDF5C05765DF() {
        ByteBuf bf = Unpooled.buffer();

        RawTransaction tx = new RawTransaction();
        {
            String x = ClassPath.readClassPathFile("/data/online.data.txt").get(37);
            byte[] hex = ByteUtil.hex(x);
            tx.deserialization(bf.clear().writeBytes(hex));
            assertEquals(tx.hash().toHexReverse256(), "1cc1ecdf5c05765df3d1f59fba24cd01c45464c329b0f0a25aa9883adfcf7f29");
        }
        RawTransaction from = new RawTransaction();
        {
            String x = ClassPath.readClassPathFile("/data/online.data.txt").get(40);
            byte[] hex = ByteUtil.hex(x);
            bf.clear().writeBytes(hex);
            from.deserialization(bf);
            assertEquals(from.hash().toHexReverse256(), "0322877bf93a707e7efb7fb86098db108e4d0065f1af30e43af24ac71462cc4a");
        }
        int in = 0;
        assertEquals(tx.getTxIns()[in].getPreTxHash(), from.hash());
        TxOut txOut = from.getTxOuts()[tx.getTxIns()[in].getPreTxOutIndex()];
        Interpreter interpreter = new Interpreter(tx).indexTxIn(in, txOut)//.witnessPushStack()
                .executor(tx.getTxIns()[in].getScript())
                .printStack()
                .executor(txOut.getScriptPubKey());
        assertTrue(interpreter.isSuccess());
    }

}