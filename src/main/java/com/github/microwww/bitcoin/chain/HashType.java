package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.chain.sign.*;

import java.util.Optional;

public enum HashType {
    // demo : in : c99c49da4c38af669dea436d3e73780dfdb6c1ecf9958baa52960e8baee30e73, from out: 406b2b06bcd34d3c8733e6b79f7a394c8a431fbf4ff5ac705c93f4076bb77602
    //!< Taproot only; implied when sighash byte is missing, and equivalent to SIGHASH_ALL
    UNKNOWN(0) {
        @Override
        public boolean verify(RawTransaction transaction, int indexTxIn, TxOut preout, byte[] pk, byte[] sign, byte[] scripts) {
            return new HashAllSignatureTransaction(transaction, indexTxIn) {
                @Override
                public HashType supportType() {
                    return UNKNOWN;
                }
            }.signatureVerify(pk, sign, scripts);
        }

        @Override
        public boolean witnessVerify(RawTransaction transaction, int indexTxIn, TxOut preout, byte[] pk, byte[] sign, byte[] scripts) {
            return new WitnessHashAllSignatureTransaction(transaction, indexTxIn, preout) {
                @Override
                public HashType supportType() {
                    return UNKNOWN;
                }
            }.signatureVerify(pk, sign, scripts);
        }
    },
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
        Optional<byte[][]> txWitness = transaction.getTxIns()[indexTxIn].getTxWitness();
        if (txWitness.isPresent()) {
            return witnessVerify(transaction, indexTxIn, preout, pk, sign, scripts);
        } else {
            return verify(transaction, indexTxIn, preout, pk, sign, scripts);
        }
    }

    public boolean verify(RawTransaction transaction, int indexTxIn, TxOut preout, byte[] pk, byte[] sign, byte[] scripts) {
        throw new UnsupportedOperationException(this.toString());
    }

    public boolean witnessVerify(RawTransaction transaction, int indexTxIn, TxOut preout, byte[] pk, byte[] sign, byte[] scripts) {
        throw new UnsupportedOperationException(this.toString());
    }

    public byte toByte() {
        return (byte) TYPE;
    }

    @Override
    public String toString() {
        return "HashType: " + this.name() + "|" + this.TYPE;
    }
}