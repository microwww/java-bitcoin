package com.github.microwww.bitcoin.chain.sign;

import com.github.microwww.bitcoin.chain.HashType;
import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxOut;
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

    @Override
    public byte[] data4signature(byte[] preScript) {
        RawTransaction tx = transaction.clone();
        int size = tx.getTxIns().length + 1;
        TxOut[] txOuts = new TxOut[size];
        Arrays.fill(txOuts, new TxOut(-1));
        if (tx.getTxOuts().length > inIndex) {
            txOuts[inIndex] = tx.getTxOuts()[inIndex];
        } else {
            // TODO :: 未知
            return ByteUtil.hex("0000000000000000000000000000000000000000000000000000000000000001");
        }
        tx.getTxOuts();
        return data4signature(tx, preScript);
    }
}
