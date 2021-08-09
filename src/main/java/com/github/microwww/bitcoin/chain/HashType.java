package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.chain.sign.HashAllSignatureTransaction;
import com.github.microwww.bitcoin.chain.sign.WitnessHashAllSignatureTransaction;
import com.github.microwww.bitcoin.chain.sign.WitnessSingleSignatureTransaction;

public enum HashType {
    ALL(1) {
        @Override
        public boolean signatureVerify(RawTransaction transaction, int indexTxIn, TxOut preout, byte[] pk, byte[] sign, byte[] scripts) {
            byte[][] txWitness = transaction.getTxIns()[indexTxIn].getTxWitness();
            if (txWitness != null && txWitness.length > 0) {
                return new WitnessHashAllSignatureTransaction(transaction, indexTxIn, preout).signatureVerify(pk, sign, scripts);
            } else {
                return new HashAllSignatureTransaction(transaction, indexTxIn).signatureVerify(pk, sign, scripts);
            }
        }
    },
    NONE(2),
    SINGLE(3),
    ANYONECANPAY(0x80),
    ALL_ANYONECANPAY(0x81),
    NONE_ANYONECANPAY(0x82),
    SINGLE_ANYONECANPAY(0x83),
    ;
    public final byte TYPE;

    HashType(byte type) {
        this.TYPE = type;
    }

    HashType(int type) {
        this.TYPE = (byte) type;
    }

    public static HashType select(int type) {
        for (HashType value : HashType.values()) {
            if (value.TYPE == type) {
                return value;
            }
        }
        throw new IllegalArgumentException("Not support Hash-type : " + type);
    }

    public boolean signatureVerify(RawTransaction transaction, int indexTxIn, TxOut preout, byte[] pk, byte[] sign, byte[] scripts) {
        if (transaction.getFlag() == 1) {
            WitnessSingleSignatureTransaction wss = new WitnessSingleSignatureTransaction(transaction, indexTxIn, preout);
            return wss.signatureVerify(pk, sign, scripts);
        } else {
            throw new UnsupportedOperationException();
        }
    }
}