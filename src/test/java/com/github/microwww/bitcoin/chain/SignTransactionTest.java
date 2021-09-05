package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.script.Interpreter;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SignTransactionTest {

    @Test
    void signature() {
        byte[] hex = ByteUtil.hex("01000000ef546acf4a020de3898d1b8956176bb507e6211b5ed3619cd08b6ea7e2a09d4100000000000000000000000000000000000000000000000000000000000000000815cf020f013ed6cf91d29f4202e8a58726b1ac6c79da47c23d1bee0a6925f8000000004721026dccc749adc2a9d0d89497ac511f760f45c47dc5ed9cf352a58ac706453880aeadab210255a9626aebf5e29c0e6538428ba0d1dcf6ca98ffdf086aa8ced5e0d0215ea465ac0011102401000000ffffffff00000000000000000000000000000000000000000000000000000000000000000000000003000000");
        byte[] bytes = ByteUtil.sha256sha256(hex);
        assertArrayEquals(bytes, ByteUtil.hex("82dde6e4f1e94d02c2b7ad03d2115d691f48d064e9d52f58194a6637e4194391"));
    }

    @Disabled
    @Test
    void sing() {
        ByteBuf bf = Unpooled.buffer();
        RawTransaction from = new RawTransaction();
        {
            byte[] hex = ByteUtil.hex("01000000017fd8dfdb54b5212c4e3151a39f4ffe279fd7f238d516a2ca731529c095d97449010000008b483045022100b6a7fe5eea81894bbdd0df61043e42780543457fa5581ac1af023761a098e92202201d4752785be5f9d1b9f8d362b8cf3b05e298a78c4abff874b838bb500dcf2a120141042e3c4aeac1ffb1c86ce3621afb1ca92773e02badf0d4b1c836eb26bd27d0c2e59ffec3d6ab6b8bbeca81b0990ab5224ebdd73696c4255d1d0c6b3c518a1a053effffffff01404b4c00000000001976a914dc44b1164188067c3a32d4780f5996fa14a4f2d988ac00000000");
            from.read(bf.clear().writeBytes(hex));
            assertEquals(from.hash().toHexReverse256(), "406b2b06bcd34d3c8733e6b79f7a394c8a431fbf4ff5ac705c93f4076bb77602");
        }

        RawTransaction tx = new RawTransaction();
        {
            byte[] hex = ByteUtil.hex("01000000010276b76b07f4935c70acf54fbf1f438a4c397a9fb7e633873c4dd3bc062b6b40000000008c493046022100d23459d03ed7e9511a47d13292d3430a04627de6235b6e51a40f9cd386f2abe3022100e7d25b080f0bb8d8d5f878bba7d54ad2fda650ea8d158a33ee3cbd11768191fd004104b0e2c879e4daf7b9ab68350228c159766676a14f5815084ba166432aab46198d4cca98fa3e9981d0a90b2effc514b76279476550ba3663fdcaff94c38420e9d5000000000100093d00000000001976a9149a7b0f3b80c6baaeedce0a0842553800f832ba1f88ac00000000");
            bf.writeBytes(hex);
            tx.read(bf);
            assertEquals(tx.hash().toHexReverse256(), "c99c49da4c38af669dea436d3e73780dfdb6c1ecf9958baa52960e8baee30e73");
        }

        int in = 0;
        assertEquals(tx.getTxIns()[in].getPreTxHash(), from.hash());
        TxOut txOut = from.getTxOuts()[tx.getTxIns()[in].getPreTxOutIndex()];
        System.out.println(ByteUtil.hex(tx.getTxIns()[in].getScript()));
        System.out.println(ByteUtil.hex(txOut.getScriptPubKey()));
        Interpreter interpreter = new Interpreter(tx).indexTxIn(in, txOut)//.witnessPushStack()
                .executor(tx.getTxIns()[in].getScript())
                .executor(txOut.getScriptPubKey());
        assertTrue(interpreter.isSuccess());
    }
}