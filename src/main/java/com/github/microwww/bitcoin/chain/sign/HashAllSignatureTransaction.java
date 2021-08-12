package com.github.microwww.bitcoin.chain.sign;

import com.github.microwww.bitcoin.chain.HashType;
import com.github.microwww.bitcoin.chain.RawTransaction;

public class HashAllSignatureTransaction extends AbstractSignatureTransaction {

    public HashAllSignatureTransaction(RawTransaction transaction, int inIndex) {
        super(transaction, inIndex);
    }

    @Override
    public HashType supportType() {
        return HashType.ALL;
    }

    @Override
    public byte[] data4signature(byte[] preScript) {
        return data4signature(transaction.clone(), preScript);
    }

}
