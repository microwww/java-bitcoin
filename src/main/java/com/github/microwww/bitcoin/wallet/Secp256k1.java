package com.github.microwww.bitcoin.wallet;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECKeySpec;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class Secp256k1 {
    private static final Logger logger = LoggerFactory.getLogger(Secp256k1.class);

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String ALGORITHM = "EC";
    private static final String SIGN_ALGORITHMS = "SHA256withECDSA";
    private static final String RANDOM_NUMBER_ALGORITHM = "SHA1PRNG";
    private static final String RANDOM_NUMBER_ALGORITHM_PROVIDER = "SUN";

    /**
     * Generate a random private key that can be used with Secp256k1.
     */
    public static byte[] generatePrivateKey() {
        SecureRandom secureRandom;
        try {
            secureRandom = SecureRandom.getInstance(RANDOM_NUMBER_ALGORITHM, RANDOM_NUMBER_ALGORITHM_PROVIDER);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            secureRandom = new SecureRandom();
        }

        BigInteger privateKeyCheck = BigInteger.ZERO;
        // Bit of magic, move this maybe. This is the max key range.
        BigInteger maxKey = new BigInteger("00FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364140", 16);

        // Generate the key, skipping as many as desired.
        byte[] privateKeyAttempt = new byte[32];
        secureRandom.nextBytes(privateKeyAttempt);
        privateKeyCheck = new BigInteger(1, privateKeyAttempt);
        while (privateKeyCheck.compareTo(BigInteger.ZERO) == 0 || privateKeyCheck.compareTo(maxKey) == 1) {
            secureRandom.nextBytes(privateKeyAttempt);
            privateKeyCheck = new BigInteger(1, privateKeyAttempt);
        }

        return privateKeyAttempt;
    }

    public static byte[] getPublicKey(byte[] privateKey) {
        return getPublicKey(privateKey, true);
    }

    /**
     * Converts a private key into its corresponding public key.
     *
     * @param privateKey
     * @param compress   是否压缩
     * @return
     */
    public static byte[] getPublicKey(byte[] privateKey, boolean compress) {
        try {
            ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256k1");
            ECPoint pointQ = spec.getG().multiply(new BigInteger(1, privateKey));
            return pointQ.getEncoded(compress);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return new byte[0];
        }
    }

    public static byte[] signature(byte[] privateKey, byte[] data) {
        return signature(converterPrivateKey(privateKey), data);
    }

    public static byte[] signature(PrivateKey privateKey, byte[] data) {
        try {
            Signature signature = Signature.getInstance(SIGN_ALGORITHMS);// SHA256withECDSA
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean signatureVerify(byte[] publicKey, byte[] signed, byte[] data) {
        return signatureVerify(converterPublicKey(publicKey), signed, data);
    }

    public static boolean signatureVerify(PublicKey publicKey, byte[] signed, byte[] data) {
        try {
            Signature signature = Signature.getInstance(SIGN_ALGORITHMS);
            signature.initVerify(publicKey);
            signature.update(data);
            return signature.verify(signed);
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * private key -> PrivateKey
     *
     * @param privateKey
     * @return BCECPrivateKey
     */
    public static PrivateKey converterPrivateKey(byte[] privateKey) {
        try {
            ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256k1");
            ECKeySpec k = new ECPrivateKeySpec(new BigInteger(privateKey), spec);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePrivate(k);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * byte[] public-publicKey -> PublicKey
     *
     * @param publicKey
     * @return BCECPublicKey
     */
    public static PublicKey converterPublicKey(byte[] publicKey) {
        try {
            ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256k1");
            ECKeySpec k = new ECPublicKeySpec(spec.getG().getCurve().decodePoint(publicKey), spec);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePublic(k);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
}