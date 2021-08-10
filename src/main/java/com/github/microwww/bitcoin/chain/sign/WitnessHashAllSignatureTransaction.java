package com.github.microwww.bitcoin.chain.sign;

import com.github.microwww.bitcoin.chain.*;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.wallet.CoinAccount;
import com.github.microwww.bitcoin.wallet.Secp256k1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class WitnessHashAllSignatureTransaction extends AbstractWitnessSignatureTransaction {
    private static final Logger logger = LoggerFactory.getLogger(WitnessHashAllSignatureTransaction.class);

    public WitnessHashAllSignatureTransaction(RawTransaction transaction, int inIndex, TxOut preout) {
        super(transaction, inIndex, preout);
    }

    @Override
    public HashType supportType() {
        return HashType.ALL;
    }

    @Override
    public byte[] data4signature(byte[] preScript) {
        RawTransaction tx = this.transaction;
        ByteBuf txIns = Unpooled.buffer();
        ByteBuf txSequence = Unpooled.buffer();
        for (TxIn txIn : tx.getTxIns()) {
            txIns.writeBytes(txIn.getPreTxHash().fill256bit()).writeIntLE(txIn.getPreTxOutIndex());
            txSequence.writeIntLE(txIn.getSequence().intValue());
        }
        ByteBuf txOuts = Unpooled.buffer();
        for (TxOut out : tx.getTxOuts()) {
            out.write(txOuts);
        }
        byte[] bytes = ByteUtil.readAll(txIns);
        byte[] hashPrevouts = ByteUtil.sha256sha256(bytes);
        if (logger.isDebugEnabled()) {
            logger.info("TX-in : \n SHA256(SHA256({})) \n = {}", ByteUtil.hex(bytes), ByteUtil.hex(hashPrevouts));
        }
        bytes = ByteUtil.readAll(txSequence);
        byte[] hashSequence = ByteUtil.sha256sha256(bytes);
        if (logger.isDebugEnabled()) {
            logger.info("TX-sequence : \n SHA256(SHA256({})) \n = {}", ByteUtil.hex(bytes), ByteUtil.hex(hashSequence));
        }
        bytes = ByteUtil.readAll(txOuts);
        byte[] hashOutputs = ByteUtil.sha256sha256(bytes);
        if (logger.isDebugEnabled()) {
            logger.info("TX-out : \n SHA256(SHA256({})) \n = {}", ByteUtil.hex(bytes), ByteUtil.hex(hashOutputs));
        }
        ByteBuf sn = Unpooled.buffer();
        sn
                .writeIntLE(tx.getVersion())
                .writeBytes(hashPrevouts)
                .writeBytes(hashSequence)
                // outpoint
                .writeBytes(tx.getTxIns()[inIndex].getPreTxHash().fill256bit()).writeIntLE(tx.getTxIns()[inIndex].getPreTxOutIndex())
                .writeBytes(preScript)
                .writeLongLE(preout.getValue())
                .writeIntLE(tx.getTxIns()[inIndex].getSequence().intValue())
                .writeBytes(hashOutputs)
                .writeIntLE(tx.getLockTime().intValue())
                .writeIntLE(supportType().TYPE);
        bytes = ByteUtil.readAll(sn);
        byte[] sha256 = ByteUtil.sha256sha256(bytes);
        if (logger.isDebugEnabled()) {
            String hex = ByteUtil.hex(sha256);
            logger.info("TX-sign : \n SHA256(SHA256({})) \n = {}", ByteUtil.hex(bytes), hex);
        }
        return sha256;
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
