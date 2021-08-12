package com.github.microwww.bitcoin.chain.sign;

import com.github.microwww.bitcoin.chain.HashType;
import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxOut;

public class HashNoneSignatureTransaction extends AbstractSignatureTransaction {

    public HashNoneSignatureTransaction(RawTransaction transaction, int inIndex) {
        super(transaction, inIndex);
    }

    @Override
    public HashType supportType() {
        return HashType.NONE;
    }

    @Override
    public byte[] data4signature(byte[] preScript) {
        RawTransaction tx = transaction.clone();
        tx.setTxOuts(new TxOut[]{});
        return new HashAllSignatureTransaction(tx, inIndex).data4signature(preScript);
    }
}
