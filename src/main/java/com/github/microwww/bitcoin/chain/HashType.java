package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.chain.sign.*;

public enum HashType {
    ALL(1) {
        @Override
        public boolean verify(RawTransaction transaction, int indexTxIn, TxOut preout, byte[] pk, byte[] sign, byte[] scripts) {
            return new HashAllSignatureTransaction(transaction, indexTxIn).signatureVerify(pk, sign, scripts);
        }

        @Override
        public boolean witnessVerify(RawTransaction transaction, int indexTxIn, TxOut preout, byte[] pk, byte[] sign, byte[] scripts) {
            return new WitnessHashAllSignatureTransaction(transaction, indexTxIn, preout).signatureVerify(pk, sign, scripts);
        }
    },
    NONE(2) {
        @Override
        public boolean verify(RawTransaction transaction, int indexTxIn, TxOut preout, byte[] pk, byte[] sign, byte[] scripts) {
            return new HashNoneSignatureTransaction(transaction, indexTxIn).signatureVerify(pk, sign, scripts);
        }

        @Override
        public boolean witnessVerify(RawTransaction transaction, int indexTxIn, TxOut preout, byte[] pk, byte[] sign, byte[] scripts) {
            return new WitnessNoneSignatureTransaction(transaction, indexTxIn, preout).signatureVerify(pk, sign, scripts);
        }
    },
    SINGLE(3) {
        @Override
        public boolean verify(RawTransaction transaction, int indexTxIn, TxOut preout, byte[] pk, byte[] sign, byte[] scripts) {
            return new HashSingleSignatureTransaction(transaction, indexTxIn).signatureVerify(pk, sign, scripts);
        }

        @Override
        public boolean witnessVerify(RawTransaction transaction, int indexTxIn, TxOut preout, byte[] pk, byte[] sign, byte[] scripts) {
            return new WitnessSingleSignatureTransaction(transaction, indexTxIn, preout).signatureVerify(pk, sign, scripts);
        }
    },
    ALL_ANYONECANPAY(0x81) {
        @Override
        public boolean witnessVerify(RawTransaction transaction, int indexTxIn, TxOut preout, byte[] pk, byte[] sign, byte[] scripts) {
            return new WitnessHashAllSignatureTransaction(transaction, indexTxIn, preout).setAnyOneCanPay(true).signatureVerify(pk, sign, scripts);
        }
    },
    NONE_ANYONECANPAY(0x82) {
        @Override
        public boolean witnessVerify(RawTransaction transaction, int indexTxIn, TxOut preout, byte[] pk, byte[] sign, byte[] scripts) {
            return new WitnessNoneSignatureTransaction(transaction, indexTxIn, preout).setAnyOneCanPay(true).signatureVerify(pk, sign, scripts);
        }
    },
    SINGLE_ANYONECANPAY(0x83) {
        @Override
        public boolean witnessVerify(RawTransaction transaction, int indexTxIn, TxOut preout, byte[] pk, byte[] sign, byte[] scripts) {
            return new WitnessSingleSignatureTransaction(transaction, indexTxIn, preout).setAnyOneCanPay(true).signatureVerify(pk, sign, scripts);
        }
    },
    ;
    public static final int ANY_ONE_CAN_PAY = 0x80;
    public final int TYPE;

    HashType(byte type) {
        this.TYPE = Byte.toUnsignedInt(type);
    }

    HashType(int type) {
        this.TYPE = type;
    }

    public static HashType select(byte type) {
        int index = Byte.toUnsignedInt(type);
        for (HashType value : HashType.values()) {
            if (value.TYPE == index) {
                return value;
            }
        }
        throw new IllegalArgumentException("Not support Hash-type : " + type);
    }

    public boolean signatureVerify(RawTransaction transaction, int indexTxIn, TxOut preout, byte[] pk, byte[] sign, byte[] scripts) {
        byte[][] txWitness = transaction.getTxIns()[indexTxIn].getTxWitness();
        if (txWitness != null && txWitness.length > 0) {
            return witnessVerify(transaction, indexTxIn, preout, pk, sign, scripts);
        } else {
            return verify(transaction, indexTxIn, preout, pk, sign, scripts);
        }
    }

    public boolean verify(RawTransaction transaction, int indexTxIn, TxOut preout, byte[] pk, byte[] sign, byte[] scripts) {
        throw new UnsupportedOperationException();
    }

    public boolean witnessVerify(RawTransaction transaction, int indexTxIn, TxOut preout, byte[] pk, byte[] sign, byte[] scripts) {
        throw new UnsupportedOperationException();
    }

    public byte toByte() {
        return (byte) TYPE;
    }
}