package com.github.microwww.bitcoin.wallet;

import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.wallet.cash.account.BechBitcoin;
import com.github.microwww.bitcoin.wallet.util.Base58;
import org.springframework.util.Assert;

import java.util.Arrays;

public class CoinAccount {

    public static class KeyPrivate {
        private final byte[] key;

        public KeyPrivate(byte[] keyPrivate) {
            this.key = keyPrivate;
        }

        public static KeyPrivate create() {
            return new KeyPrivate(Secp256k1.generatePrivateKey());
        }

        public static KeyPrivate importPrivateKey(String privateBase58) {
            Assert.isTrue(privateBase58.length() > 50, "NOT NULL");
            byte[] decode = Base58.decode(privateBase58);
            byte[] privateKey = Arrays.copyOfRange(decode, 1, decode.length - 5);
            return new KeyPrivate(privateKey);
        }

        public String dumpedPrivateKey(Env config) {
            byte[] bts = new byte[key.length + 1];
            System.arraycopy(this.key, 0, bts, 0, this.key.length);
            bts[key.length] = 1;
            return Base58.encodeAddress(config.dumpedPrivateKey, bts);
        }

        public KeyPublic getKeyPublic() {
            byte[] pk = Secp256k1.getPublicKey(key);
            return new KeyPublic(Arrays.copyOfRange(pk, 0, pk.length));
        }

        public Address getAddress() {
            byte[] pk = Secp256k1.getPublicKey(key);
            return new KeyPublic(Arrays.copyOfRange(pk, 0, pk.length)).getAddress();
        }

        public byte[] signature(byte[] data) {
            return Secp256k1.signature(this.key, data);
        }

        public byte[] getKey() {
            return Arrays.copyOf(key, key.length);
        }
    }

    public static class KeyPublic {
        private final byte[] key;

        public KeyPublic(byte[] keyPublic) {
            this.key = keyPublic;
        }

        public Address getAddress() {
            return new Address(ByteUtil.sha256ripemd160(key));
        }

        public byte[] getKey() {
            return Arrays.copyOf(key, key.length);
        }

        public boolean signatureVerify(byte[] signed, byte[] data) {
            return Secp256k1.signatureVerify(this.key, signed, data);
        }

        public static boolean checkFormat(byte[] bytes) {
            if (bytes.length == 0x21) {// 压缩的公钥 02/03 + <32位>
                return bytes[0] == 0x02 || bytes[0] == 0x03;
            } else if (bytes.length == 0x41) {// 不压缩的公钥 04 + <64位>
                return bytes[0] == 0x04;
            }
            return false;
        }
    }

    public static class Address {
        private final byte[] keyPublicHash;

        public Address(byte[] keyPublicHash) {
            this.keyPublicHash = keyPublicHash;
        }

        public static Address importAddress(String publicKeyBase58) {
            byte[] decode = Base58.decode(publicKeyBase58);
            byte[] hash = Arrays.copyOfRange(decode, 1, decode.length - 4);
            Address ca = new Address(hash);
            return ca;
        }

        public String toBase58Address(Env acc) {
            return Base58.encodeAddress(acc.address, this.keyPublicHash);
        }

        public String toP2SHAddress(Env acc) {
            return Base58.encodeAddress(acc.addressP2SH, this.keyPublicHash);
        }

        // https://en.bitcoin.it/wiki/BIP_0173
        public String toBech32Address(Env config) {
            byte[] sha160 = this.keyPublicHash;
            return BechBitcoin.BECH.toAddress(config, sha160);
        }

        public byte[] getKeyPublicHash() {
            return keyPublicHash;
        }
    }

}
