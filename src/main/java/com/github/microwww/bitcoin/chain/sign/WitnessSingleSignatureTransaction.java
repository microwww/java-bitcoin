package com.github.microwww.bitcoin.chain.sign;

import com.github.microwww.bitcoin.chain.HashType;
import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxIn;
import com.github.microwww.bitcoin.chain.TxOut;
import io.netty.buffer.ByteBuf;

// uint256 SignatureHash(const CScript& scriptCode
public class WitnessSingleSignatureTransaction extends AbstractWitnessSignatureTransaction {

    public WitnessSingleSignatureTransaction(RawTransaction transaction, int inIndex, TxOut preout) {
        super(transaction, inIndex, preout);
    }

    @Override
    public HashType supportType() {
        if (isAnyOneCanPay()) {
            return HashType.SINGLE_ANYONECANPAY;
        }
        return HashType.SINGLE;
    }

    /**
     * 所有输入和对应的输出(单个)做签名
     *
     * @param preScript
     * @return
     */
    @Override
    public byte[] data4signature(byte[] preScript) {
        RawTransaction tx = this.transaction.clone();
        if (isAnyOneCanPay()) {
            tx.setTxIns(new TxIn[]{});
        }
        if (tx.getTxOuts().length > inIndex) {
            tx.setTxOuts(new TxOut[]{tx.getTxOuts()[inIndex]});
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
