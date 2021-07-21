package com.github.microwww.bitcoin.wallet;

import com.github.microwww.bitcoin.wallet.cash.account.BechCashUtil;
import com.github.microwww.bitcoin.wallet.util.Base58;
import com.github.microwww.bitcoin.wallet.util.Bech32;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.util.encoders.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Account4bitcoin {

    private final BitAccountConfig config;
    private final byte[] privateKey;
    private byte[] publicKey;

    public static Account4bitcoin createBitcoinAccount(BitAccountConfig config) {
        return new Account4bitcoin(config);
    }

    private Account4bitcoin(BitAccountConfig config) {
        this(null, config);
    }

    public Account4bitcoin(byte[] privateKey, BitAccountConfig config) {
        if (privateKey == null) {
            this.privateKey = Secp256k1.generatePrivateKey();
        } else {
            this.privateKey = privateKey;
        }
        this.config = config;
    }

    public String getPrivateKeyBase58() {
        byte[] bts = new byte[privateKey.length + 1];
        System.arraycopy(this.privateKey, 0, bts, 0, this.privateKey.length);
        bts[privateKey.length] = 1;
        return toBase58(config.getDumpedPrivateKeyHeader(), bts);
    }

    public String getPublicKeyHex() {
        return Hex.toHexString(getPublicKey());
    }

    public String getPrivateKeyHex() {
        return Hex.toHexString(this.privateKey);
    }

    public synchronized byte[] getPublicKey() {
        if (publicKey == null) {
            publicKey = Secp256k1.getPublicKey(privateKey);
        }
        return publicKey;
    }

    public byte[] getPublicHash() {
        return sha256hash160(this.getPublicKey());
    }

    public String toCashBech32() {
        byte[] sha160 = getPublicHash();
        byte[] comp = new byte[1 + sha160.length];
        comp[0] = config.cashAddressHeader();
        System.arraycopy(sha160, 0, comp, 1, sha160.length);
        byte[] payload = Bech32.BECH.payloadEncode(comp);
        return BechCashUtil.instance.bechEncode(payload, config.cashPrefix());
    }

    public String toBase58Address() {
        byte[] sha160 = getPublicHash();
        return toBase58(config.getAddressHeader(), sha160);
    }

    public static String toBase58(byte version, byte[] bytes) {
        // A stringified buffer is:
        // 1 byte version + data bytes + 4 bytes check code (a truncated hash)
        byte[] addressBytes = new byte[1 + bytes.length + 4];
        addressBytes[0] = version;
        System.arraycopy(bytes, 0, addressBytes, 1, bytes.length);
        byte[] checksum = hashTwice(addressBytes, 0, bytes.length + 1);
        System.arraycopy(checksum, 0, addressBytes, bytes.length + 1, 4);
        return Base58.encode(addressBytes);
    }

    public static byte[] sha256hash160(byte[] input) {
        byte[] sha256 = hash(input, 0, input.length);
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(sha256, 0, sha256.length);
        byte[] out = new byte[20];
        digest.doFinal(out, 0);
        return out;
    }

    public static byte[] hashTwice(byte[] input, int offset, int length) {
        MessageDigest digest = newDigest();
        digest.update(input, offset, length);
        return digest.digest(digest.digest());
    }

    public static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e); // Can't happen.
        }
    }

    public static byte[] hash(byte[] input, int offset, int length) {
        MessageDigest digest = newDigest();
        digest.update(input, offset, length);
        return digest.digest();
    }
}
