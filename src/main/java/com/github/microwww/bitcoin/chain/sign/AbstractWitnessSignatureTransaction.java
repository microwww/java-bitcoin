package com.github.microwww.bitcoin.chain.sign;

import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxIn;
import com.github.microwww.bitcoin.chain.TxOut;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public abstract class AbstractWitnessSignatureTransaction extends AbstractSignatureTransaction {
    private static final Logger logger = LoggerFactory.getLogger(AbstractWitnessSignatureTransaction.class);

    protected static final byte[] _READ_ONLY_32_ZERO = new byte[32];
    protected static final byte[] _ZERO = new byte[0];
    protected final TxOut preout;

    public AbstractWitnessSignatureTransaction(RawTransaction transaction, int inIndex, TxOut preout) {
        super(transaction, inIndex);
        Assert.isTrue(preout != null, "Preout not null");
        this.preout = preout;
    }

    @Override
    public byte[] data4signature(RawTransaction txCopy, byte[] preScript) {
        RawTransaction tx = txCopy;
        ByteBuf sign = Unpooled.buffer();

        ByteBuf buffer = Unpooled.buffer();
        // 1.nVersion
        sign.writeIntLE(tx.getVersion());

        {// 2.hashPrevouts
            byte[] hashPrevouts = _READ_ONLY_32_ZERO;
            if (tx.getTxIns().length > 0) {
                for (TxIn txIn : tx.getTxIns()) {
                    buffer.writeBytes(txIn.getPreTxHash().fill256bit()).writeIntLE(txIn.getPreTxOutIndex());
                }
                byte[] bytes = ByteUtil.readAll(buffer);
                hashPrevouts = ByteUtil.sha256sha256(bytes);
                if (logger.isDebugEnabled()) {
                    logger.info("TX-in : \n SHA256(SHA256({})) \n = {}", ByteUtil.hex(bytes), ByteUtil.hex(hashPrevouts));
                }
            }
            sign.writeBytes(hashPrevouts);
        }
        {// 3.hashSequence
            buffer.clear();
            byte[] hashSequence = hashSequence(buffer, tx);
            sign.writeBytes(hashSequence);
        }

        // 4.outpoint
        sign.writeBytes(this.transaction.getTxIns()[inIndex].getPreTxHash().fill256bit()).writeIntLE(this.transaction.getTxIns()[inIndex].getPreTxOutIndex())
                // 5.scriptCode
                .writeBytes(preScript)
                // 6.amount
                .writeLongLE(preout.getValue())
                // 7.nSequence
                .writeIntLE(this.transaction.getTxIns()[inIndex].getSequence().intValue());

        {// 8.hashOutputs
            byte[] hashOutputs = _READ_ONLY_32_ZERO;
            if (tx.getTxOuts().length > 0) {
                buffer.clear();
                for (TxOut out : tx.getTxOuts()) {
                    out.write(buffer);
                }
                byte[] bytes = ByteUtil.readAll(buffer);
                hashOutputs = ByteUtil.sha256sha256(bytes);
                if (logger.isDebugEnabled()) {
                    logger.info("TX-out : \n SHA256(SHA256({})) \n = {}", ByteUtil.hex(bytes), ByteUtil.hex(hashOutputs));
                }
            }
            sign.writeBytes(hashOutputs);
        }

        // 9.nLockTime
        sign.writeIntLE(tx.getLockTime().intValue());
        // 10.nHashType`
        sign.writeIntLE(supportType().toUnsignedInt());

        byte[] bytes = ByteUtil.readAll(sign);
        byte[] sha256 = ByteUtil.sha256sha256(bytes);
        if (logger.isDebugEnabled()) {
            String hex = ByteUtil.hex(sha256);
            logger.info("TX-sign : \n SHA256(SHA256({})) \n = {}", ByteUtil.hex(bytes), hex);
        }
        return sha256;
    }

    protected byte[] hashSequence(ByteBuf buffer, RawTransaction tx) {
        return _READ_ONLY_32_ZERO;
    }

    public int getInIndex() {
        return inIndex;
    }

    public int getPreoutIndex() {
        return preoutIndex;
    }
}
