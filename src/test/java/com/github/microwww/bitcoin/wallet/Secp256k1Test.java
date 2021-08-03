package com.github.microwww.bitcoin.wallet;

import com.github.microwww.bitcoin.util.ByteUtil;
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

    @Test
    void signatureKey() {
        CoinAccount.KeyPrivate ab = CoinAccount.KeyPrivate.importPrivateKey("cQvNa1VcqAeUtrhwaJp7s3ExQyNWkjK5QvuLJ2Zxx696d3ZgcSDs");
        byte[] prv = ab.getKey();
        String hex = "010000000151786ed6d5750dd39efc922aafa80076cb4c0041b5fa577adc44db9d368278a1000000001976a91470b9b9273750addaeb29fb47dfd70d3c638971e588acfeffffff021e2f1a1e010000001976a9141206cce0cd071c77fbbe79b7e661d0075b3317e188ac00c2eb0b000000001976a914b0b1681de54a771dc3976f73c1a3ad91236c110288ac4e00000001000000";
        byte[] dat = ByteUtil.hex(hex);
        byte[] sha = ByteUtil.sha256sha256(dat);
        byte[] signature = Secp256k1.signature(prv, sha);
        byte[] pk = ByteUtil.hex("035fdc15923923e8cd035b8ed01c2cae29703a9ae01443fc6a7ce85a889efd5939");
        assertTrue(Secp256k1.signatureVerify(pk, signature, sha));
    }
}