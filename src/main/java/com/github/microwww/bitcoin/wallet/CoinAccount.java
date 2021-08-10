package com.github.microwww.bitcoin.wallet;

import com.github.microwww.bitcoin.wallet.cash.account.BechBitcoin;
import com.github.microwww.bitcoin.wallet.util.Base58;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.springframework.util.Assert;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
            return toBase58(config.getDumpedPrivateKeyHeader(), bts);
        }

        public KeyPublic getKeyPublic() {
            byte[] pk = Secp256k1.getPublicKey(key);
            return new KeyPublic(Arrays.copyOfRange(pk, 0, pk.length));
        }

        public Address getAddress() {
            byte[] pk = Secp256k1.getPublicKey(key);
            return new KeyPublic(Arrays.copyOfRange(pk, 0, pk.length)).getAddress();
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
            return new Address(sha256ripemd160(key));
        }

        public byte[] getKey() {
            return Arrays.copyOf(key, key.length);
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
            return toBase58(acc.getAddressHeader(), this.keyPublicHash);
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

    public static String toBase58(byte version, byte[] bytes) {
        // A stringified buffer is:
        // 1 byte version + data bytes + 4 bytes check code (a truncated hash)
        byte[] addressBytes = new byte[1 + bytes.length + 4];
        addressBytes[0] = version;
        System.arraycopy(bytes, 0, addressBytes, 1, bytes.length);
        byte[] checksum = sha256sha256(addressBytes, 0, bytes.length + 1);
        System.arraycopy(checksum, 0, addressBytes, bytes.length + 1, 4);
        return Base58.encode(addressBytes);
    }

    public static byte[] sha256ripemd160(byte[] input) {
        return ripemd160(sha256(input));
    }

    public static byte[] ripemd160(byte[] data) {
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(data, 0, data.length);
        byte[] out = new byte[20];
        digest.doFinal(out, 0);
        return out;
    }

    public static byte[] sha256sha256(byte[] input, int offset, int length) {
        MessageDigest digest = sha256digest();
        digest.update(input, offset, length);
        return digest.digest(digest.digest());
    }

    public static byte[] sha256(byte[] input) {
        MessageDigest digest = sha256digest();
        digest.update(input, 0, input.length);
        return digest.digest();
    }

    private static MessageDigest sha256digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e); // Can't happen.
        }
    }
}
