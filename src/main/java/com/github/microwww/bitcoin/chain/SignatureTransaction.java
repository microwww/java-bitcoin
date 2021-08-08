package com.github.microwww.bitcoin.chain;

public interface SignatureTransaction {
    HashType supportType();

    byte[] getSignature(byte[] privateKey, byte[] preScript);

    boolean signatureVerify(byte[] publicKey, byte[] signature, byte[] preScript);

    byte[] data4signature(byte[] preScript);

    RawTransaction writeSignatureScript(byte[] privateKey, byte[] preScript);
}
