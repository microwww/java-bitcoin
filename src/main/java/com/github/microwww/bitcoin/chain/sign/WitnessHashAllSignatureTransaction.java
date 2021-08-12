package com.github.microwww.bitcoin.chain.sign;

import com.github.microwww.bitcoin.chain.HashType;
import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxIn;
import com.github.microwww.bitcoin.chain.TxOut;
import com.github.microwww.bitcoin.wallet.CoinAccount;

public class WitnessHashAllSignatureTransaction extends AbstractWitnessSignatureTransaction {

    public WitnessHashAllSignatureTransaction(RawTransaction transaction, int inIndex, TxOut preout) {
        super(transaction, inIndex, preout);
    }

    @Override
    public HashType supportType() {
        return HashType.ALL;
    }

    @Override
    public byte[] data4signature(byte[] preScript) {
        return super.data4signature(this.transaction.clone(), preScript);
    }

    @Override
    public RawTransaction writeSignatureScript(byte[] privateKey, byte[] preScript) {
        CoinAccount.KeyPrivate kr = new CoinAccount.KeyPrivate(privateKey);
        byte[] pk = kr.getAddress().getKeyPublicHash();
        byte[] signature = this.getSignature(privateKey, preScript);
        TxIn in = transaction.getTxIns()[inIndex];
        in.setScript(_ZERO);
        in.setTxWitness(new byte[][]{signature, pk});
        return transaction;
    }
}
