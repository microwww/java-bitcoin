package com.github.microwww.bitcoin.wallet;

import com.github.microwww.bitcoin.wallet.cash.account.BechCashUtil;
import com.github.microwww.bitcoin.wallet.util.Base58;
import com.github.microwww.bitcoin.wallet.util.Bech32;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.util.Assert;

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
        this(Secp256k1.generatePrivateKey(), config);
    }

    public Account4bitcoin(byte[] privateKey, BitAccountConfig config) {
        Assert.isTrue(privateKey != null, "NOT NULL");
        this.privateKey = privateKey;
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
        byte[] data = this.getPublicKey();
        data = sha256(data);
        return ripemd160(data);
    }

    public static String publicToBase58Address(BitAccountConfig acc, byte[] pk) {
        pk = sha256(pk);
        return toBase58(acc.getAddressHeader(), ripemd160(pk));
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
        byte[] checksum = sha256sha256(addressBytes, 0, bytes.length + 1);
        System.arraycopy(checksum, 0, addressBytes, bytes.length + 1, 4);
        return Base58.encode(addressBytes);
    }

    public static byte[] sha256hash160(byte[] input) {
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
