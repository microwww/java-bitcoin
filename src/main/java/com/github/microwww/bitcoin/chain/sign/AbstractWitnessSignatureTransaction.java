package com.github.microwww.bitcoin.chain.sign;

import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxOut;
import org.springframework.util.Assert;

public abstract class AbstractWitnessSignatureTransaction extends AbstractSignatureTransaction {
    protected static final byte[] _READ_ONLY_32_ZERO = new byte[32];
    protected static final byte[] _ZERO = new byte[0];
    protected final TxOut preout;

    public AbstractWitnessSignatureTransaction(RawTransaction transaction, int inIndex, TxOut preout) {
        super(transaction, inIndex);
        Assert.isTrue(preout != null, "Preout not null");
        this.preout = preout;
    }

    public int getInIndex() {
        return inIndex;
    }

    public int getPreoutIndex() {
        return preoutIndex;
    }
}
