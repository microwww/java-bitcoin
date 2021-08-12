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
public class WitnessNoneSignatureTransaction extends AbstractWitnessSignatureTransaction {
    private static final Logger logger = LoggerFactory.getLogger(WitnessNoneSignatureTransaction.class);

    public WitnessNoneSignatureTransaction(RawTransaction transaction, int inIndex, TxOut preout) {
        super(transaction, inIndex, preout);
    }

    @Override
    public HashType supportType() {
        return HashType.NONE;
    }

    /**
     * 所有输入做签名, 不对输出做签名(0个)
     * @param preScript
     * @return
     */
    @Override
    public byte[] data4signature(byte[] preScript) {
        RawTransaction tx = this.transaction;
        ByteBuf txIns = Unpooled.buffer();
        for (TxIn txIn : tx.getTxIns()) {
            txIns.writeBytes(txIn.getPreTxHash().fill256bit()).writeIntLE(txIn.getPreTxOutIndex());
        }
        byte[] bytes = ByteUtil.readAll(txIns);
        byte[] hashPrevouts = ByteUtil.sha256sha256(bytes);
        if (logger.isDebugEnabled()) {
            logger.info("TX-in : \n SHA256(SHA256({})) \n = {}", ByteUtil.hex(bytes), ByteUtil.hex(hashPrevouts));
        }
        byte[] hashSequence = bytes = _READ_ONLY_32_ZERO;// 000...000
        if (logger.isDebugEnabled()) {
            logger.info("TX-sequence : \n SHA256(SHA256({})) \n = {}", ByteUtil.hex(bytes), ByteUtil.hex(hashSequence));
        }

        //
        byte[] hashOutputs = bytes = _READ_ONLY_32_ZERO;
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
        throw new UnsupportedOperationException();
    }
}
