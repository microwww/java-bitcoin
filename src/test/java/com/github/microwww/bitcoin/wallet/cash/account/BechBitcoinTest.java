package com.github.microwww.bitcoin.wallet.cash.account;

import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.wallet.util.Bech32;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BechBitcoinTest {

    @Test
    void sha256ripemd160() {// demo for https://en.bitcoin.it/wiki/Bech32
        byte[] encoded = ByteUtil.hex("0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798");
        byte[] bytes = ByteUtil.sha256ripemd160(encoded);
        assertArrayEquals(ByteUtil.hex("751e76e8199196d454941c45d1b3a323f1433bd6"), bytes);
        byte[] bytes1 = Bech32.BECH.payloadEncode(bytes);
        byte[] hex = ByteUtil.hex("0e140f070d1a001912060b0d081504140311021d030c1d03040f1814060e1e16");
        assertArrayEquals(bytes1, hex);
        byte[] nhex = new byte[hex.length + 1];
        System.arraycopy(hex, 0, nhex, 1, hex.length);
        byte[] bcs = BechBitcoin.BECH.createChecksum("bc", nhex, BechBitcoin.Encode.BECH32);
        assertArrayEquals(ByteUtil.hex("0c0709110b15"), bcs);
        String s = Bech32.BECH.bechEncode(nhex, bcs);
        assertEquals("qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4", s);
    }
}