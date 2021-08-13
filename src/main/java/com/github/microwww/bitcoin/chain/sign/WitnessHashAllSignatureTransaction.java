package com.github.microwww.bitcoin.chain.sign;

import com.github.microwww.bitcoin.chain.HashType;
import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxIn;
import com.github.microwww.bitcoin.chain.TxOut;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.wallet.CoinAccount;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WitnessHashAllSignatureTransaction extends AbstractWitnessSignatureTransaction {
    private static final Logger logger = LoggerFactory.getLogger(WitnessHashAllSignatureTransaction.class);

    public WitnessHashAllSignatureTransaction(RawTransaction transaction, int inIndex, TxOut preout) {
        super(transaction, inIndex, preout);
    }

    @Override
    public HashType supportType() {
        if (isAnyOneCanPay()) {
            return HashType.ALL_ANYONECANPAY;
        }
        return HashType.ALL;
    }

    @Override
    public byte[] data4signature(byte[] preScript) {
        RawTransaction tx = this.transaction.clone();
        if (isAnyOneCanPay()) {
            tx.setTxIns(new TxIn[]{});
        }
        return super.data4signature(tx, preScript);
    }

    protected byte[] hashSequence(ByteBuf buffer, RawTransaction tx) {
        {// 3.hashSequence
            buffer.clear();
            for (TxIn txIn : tx.getTxIns()) {
                buffer.writeIntLE(txIn.getSequence().intValue());
            }
            byte[] bytes = ByteUtil.readAll(buffer);
            byte[] hashSequence = ByteUtil.sha256sha256(bytes);
            if (logger.isDebugEnabled()) {
                logger.info("TX-sequence : \n SHA256(SHA256({})) \n = {}", ByteUtil.hex(bytes), ByteUtil.hex(hashSequence));
            }
            return hashSequence;
        }
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
