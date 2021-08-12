package com.github.microwww.bitcoin.chain.sign;

import com.github.microwww.bitcoin.chain.HashType;
import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxIn;
import com.github.microwww.bitcoin.chain.TxOut;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

// uint256 SignatureHash(const CScript& scriptCode
public class WitnessAnyOneCanPayNoneSignatureTransaction extends AbstractWitnessSignatureTransaction {
    private static final Logger logger = LoggerFactory.getLogger(WitnessAnyOneCanPayNoneSignatureTransaction.class);

    public WitnessAnyOneCanPayNoneSignatureTransaction(RawTransaction transaction, int inIndex, TxOut preout) {
        super(transaction, inIndex, preout);
    }

    @Override
    public HashType supportType() {
        return HashType.NONE_ANYONECANPAY;
    }

    @Override
    public byte[] data4signature(byte[] preScript) {
        RawTransaction tx = this.transaction.clone();
        Assert.isTrue(tx.getTxIns().length > 0, "TX-in not empty");
        Assert.isTrue(tx.getTxOuts().length > 0, "TX-out not empty");
        tx.setTxIns(new TxIn[]{});
        tx.setTxOuts(new TxOut[]{});
        return super.data4signature(tx, preScript);
    }

    @Override
    public RawTransaction writeSignatureScript(byte[] privateKey, byte[] preScript) {
        throw new UnsupportedOperationException();
    }
}
