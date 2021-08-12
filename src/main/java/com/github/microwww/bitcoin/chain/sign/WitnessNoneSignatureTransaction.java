package com.github.microwww.bitcoin.chain.sign;

import com.github.microwww.bitcoin.chain.HashType;
import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxOut;
import io.netty.buffer.ByteBuf;
import org.springframework.util.Assert;

// uint256 SignatureHash(const CScript& scriptCode
public class WitnessNoneSignatureTransaction extends AbstractWitnessSignatureTransaction {

    public WitnessNoneSignatureTransaction(RawTransaction transaction, int inIndex, TxOut preout) {
        super(transaction, inIndex, preout);
    }

    @Override
    public HashType supportType() {
        return HashType.NONE;
    }

    /**
     * 所有输入做签名, 不对输出做签名(0个)
     *
     * @param preScript
     * @return
     */
    @Override
    public byte[] data4signature(byte[] preScript) {
        RawTransaction tx = this.transaction.clone();
        Assert.isTrue(tx.getTxOuts().length > 0, "empty it next, so check it first");
        tx.setTxOuts(new TxOut[]{});
        return super.data4signature(tx, preScript);
    }

    @Override
    public RawTransaction writeSignatureScript(byte[] privateKey, byte[] preScript) {
        throw new UnsupportedOperationException();
    }
}
