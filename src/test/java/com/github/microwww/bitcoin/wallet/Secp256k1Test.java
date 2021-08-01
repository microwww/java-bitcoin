package com.github.microwww.bitcoin.wallet;

import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;

class Secp256k1Test {

    @Test
    void signature() {
        byte[] prv = Secp256k1.generatePrivateKey();
        PrivateKey privateKey = Secp256k1.converterPrivateKey(prv);
        byte[] pk = Secp256k1.getPublicKey(prv);
        PublicKey publicKey = Secp256k1.converterPublicKey(pk);
        byte[] d = "test".getBytes();
        byte[] signature = Secp256k1.signature(privateKey, d);
        {
            boolean b = Secp256k1.signatureVerify(publicKey, signature, d);
            assertTrue(b);
        }
        {
            PublicKey pk2 = Secp256k1.converterPublicKey(Secp256k1.getPublicKey(Secp256k1.generatePrivateKey()));
            boolean b = Secp256k1.signatureVerify(pk2, signature, d);
            assertFalse(b);
        }

    }
}