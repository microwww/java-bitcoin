package com.github.microwww.bitcoin.chain.sign;

import com.github.microwww.bitcoin.chain.HashType;
import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxOut;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// uint256 SignatureHash(const CScript& scriptCode
public class WitnessAnyOneCanPayAllSignatureTransaction extends AbstractWitnessSignatureTransaction {
    private static final Logger logger = LoggerFactory.getLogger(WitnessAnyOneCanPayAllSignatureTransaction.class);

    public WitnessAnyOneCanPayAllSignatureTransaction(RawTransaction transaction, int inIndex, TxOut preout) {
        super(transaction, inIndex, preout);
    }

    @Override
    public HashType supportType() {
        return HashType.ALL_ANYONECANPAY;
    }

    @Override
    public byte[] data4signature(byte[] preScript) {
        RawTransaction tx = this.transaction;

        ByteBuf buffer = Unpooled.buffer();
        for (TxOut out : tx.getTxOuts()) {
            out.write(buffer);
        }
        byte[] bytes = ByteUtil.readAll(buffer);
        byte[] hashOutputs = ByteUtil.sha256sha256(bytes);

        if (logger.isDebugEnabled()) {
            logger.info("TX-out : \n SHA256(SHA256({})) \n = {}", ByteUtil.hex(bytes), ByteUtil.hex(hashOutputs));
        }

        buffer.clear()
                .writeIntLE(tx.getVersion())
                .writeBytes(_READ_ONLY_32_ZERO) // hashPrevouts
                .writeBytes(_READ_ONLY_32_ZERO) // hashSequence
                // outpoint
                .writeBytes(tx.getTxIns()[inIndex].getPreTxHash().fill256bit()).writeIntLE(tx.getTxIns()[inIndex].getPreTxOutIndex())
                .writeBytes(preScript)
                .writeLongLE(preout.getValue())
                .writeIntLE(tx.getTxIns()[inIndex].getSequence().intValue())
                .writeBytes(hashOutputs)
                .writeIntLE(tx.getLockTime().intValue())
                .writeIntLE(this.supportType().toUnsignedInt());
        bytes = ByteUtil.readAll(buffer);
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
