package com.github.microwww.bitcoin.chain.sign;

import com.github.microwww.bitcoin.chain.HashType;
import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxIn;
import com.github.microwww.bitcoin.chain.TxOut;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// uint256 SignatureHash(const CScript& scriptCode
public class WitnessAnyOneCanPaySingleSignatureTransaction extends AbstractWitnessSignatureTransaction {
    private static final Logger logger = LoggerFactory.getLogger(WitnessAnyOneCanPaySingleSignatureTransaction.class);

    public WitnessAnyOneCanPaySingleSignatureTransaction(RawTransaction transaction, int inIndex, TxOut preout) {
        super(transaction, inIndex, preout);
    }

    @Override
    public HashType supportType() {
        return HashType.SINGLE_ANYONECANPAY;
    }

    @Override
    public byte[] data4signature(byte[] preScript) {
        RawTransaction tx = this.transaction.clone();
        tx.setTxIns(new TxIn[]{});
        TxOut[] txOuts = tx.getTxOuts();
        if (txOuts.length > inIndex) {
            tx.setTxOuts(new TxOut[]{txOuts[inIndex]});
        } else {
            tx.setTxOuts(new TxOut[]{});
        }
        return super.data4signature(tx, preScript);
    }

    @Override
    public RawTransaction writeSignatureScript(byte[] privateKey, byte[] preScript) {
        throw new UnsupportedOperationException();
    }
}
