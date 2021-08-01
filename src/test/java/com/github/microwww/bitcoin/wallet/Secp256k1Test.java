package com.github.microwww.bitcoin.wallet;

import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.wallet.util.Base58;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
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

        System.out.println(ByteUtil.hex(prv));

        String s = Account4bitcoin.toBase58(BitAccountConfig.REG_TEST.getAddressHeader(), prv);
        System.out.println(s);

        byte[] decode = Base58.decode(s);
        System.out.println(ByteUtil.hex(decode));
    }

    @Test
    void signatureKey() {
        Account4bitcoin ab = Account4bitcoin.fromBase58("cQvNa1VcqAeUtrhwaJp7s3ExQyNWkjK5QvuLJ2Zxx696d3ZgcSDs");
        byte[] prv = ab.getPrivateKey();
        byte[] publicKey = Secp256k1.getPublicKey(prv);
        System.out.println(ByteUtil.hex(publicKey));
        System.out.println(ab.toBase58Address());
        String hex = "010000000151786ed6d5750dd39efc922aafa80076cb4c0041b5fa577adc44db9d368278a1000000001976a91470b9b9273750addaeb29fb47dfd70d3c638971e588acfeffffff021e2f1a1e010000001976a9141206cce0cd071c77fbbe79b7e661d0075b3317e188ac00c2eb0b000000001976a914b0b1681de54a771dc3976f73c1a3ad91236c110288ac4e00000001000000";
        byte[] dat = ByteUtil.hex(hex);
        byte[] sha = ByteUtil.sha256sha256(dat);
        byte[] signature = Secp256k1.signature(prv, sha);
        byte[] pk = ByteUtil.hex("035fdc15923923e8cd035b8ed01c2cae29703a9ae01443fc6a7ce85a889efd5939");
        assertTrue(Secp256k1.signatureVerify(pk, signature, sha));
    }

    @Test
    void signatureKe2y() {
        byte[] publicKey = ByteUtil.hex("0411db93e1dcdb8a016b49840f8c53bc1eb68a382e97b1482ecad7b148a6909a5cb2e0eaddfb84ccf9744464f82e160bfa9b8b64f9d4c03f999b8643f656b412a3");
        BCECPublicKey pk = (BCECPublicKey) Secp256k1.converterPublicKey(publicKey);
        System.out.println( Account4bitcoin.publicToBase58Address(BitAccountConfig.MAIN, publicKey));
        System.out.println(ByteUtil.hex(pk.getEncoded()));

        String hex = "0100000001c997a5e56e104102fa209c6a852dd90660a20b2d9c352423edce25857fcd37040000000043410411db93e1dcdb8a016b49840f8c53bc1eb68a382e97b1482ecad7b148a6909a5cb2e0eaddfb84ccf9744464f82e160bfa9b8b64f9d4c03f999b8643f656b412a3acffffffff0200ca9a3b00000000434104ae1a62fe09c5f51b13905f07f06b99a2f7159b2225f374cd378d71302fa28414e7aab37397f554a7df5f142c21c1b7303b8a0626f1baded5c72a704f7e6cd84cac00286bee0000000043410411db93e1dcdb8a016b49840f8c53bc1eb68a382e97b1482ecad7b148a6909a5cb2e0eaddfb84ccf9744464f82e160bfa9b8b64f9d4c03f999b8643f656b412a3ac0000000001000000";
        byte[] sha = ByteUtil.sha256sha256(ByteUtil.hex(hex));
        byte[] signature = ByteUtil.hex("3045022100fa52fc92c2ce34c0c0f410f146ee20fd0d17b4b104780eaa9f4a419fd3ab452302202d842760913f089408ed1df044443ff17cfb1252167ffcf70f15be2e3a755ab4");
        assertTrue(Secp256k1.signatureVerify(publicKey, signature, sha));
    }
}