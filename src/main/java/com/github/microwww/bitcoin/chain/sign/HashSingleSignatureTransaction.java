package com.github.microwww.bitcoin.chain.sign;

import com.github.microwww.bitcoin.chain.HashType;
import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxIn;
import com.github.microwww.bitcoin.chain.TxOut;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.util.ByteUtil;

import java.util.Arrays;

public class HashSingleSignatureTransaction extends AbstractSignatureTransaction {

    public HashSingleSignatureTransaction(RawTransaction transaction, int inIndex) {
        super(transaction, inIndex);
    }

    @Override
    public HashType supportType() {
        return HashType.NONE;
    }

    /**
     * The transaction that uses SIGHASH_SINGLE type of signature should not have more inputs than outputs.
     */
    @Override
    public byte[] data4signature(byte[] preScript) {
        if (transaction.getTxOuts().length <= inIndex) {
            // TODO :: 未知是否正确
            return ByteUtil.hex("0000000000000000000000000000000000000000000000000000000000000001");
        }
        RawTransaction tx = transaction.clone();
        for (int i = 0; i < tx.getTxIns().length; i++) {
            if (i != inIndex) {
                TxIn txIn = tx.getTxIns()[i];
                txIn.setSequence(Uint32.ZERO);
            }
        }
        int size = inIndex + 1;
        TxOut[] replace = new TxOut[size];
        Arrays.fill(replace, new TxOut(-1));
        replace[inIndex] = tx.getTxOuts()[inIndex];
        tx.setTxOuts(replace);
        return data4signature(tx, preScript);
    }
}
