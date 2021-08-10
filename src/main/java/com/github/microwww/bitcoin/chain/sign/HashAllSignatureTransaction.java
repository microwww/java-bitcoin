package com.github.microwww.bitcoin.chain.sign;

import com.github.microwww.bitcoin.chain.HashType;
import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxIn;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.wallet.CoinAccount;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashAllSignatureTransaction extends AbstractSignatureTransaction {
    private static final Logger logger = LoggerFactory.getLogger(HashAllSignatureTransaction.class);

    public HashAllSignatureTransaction(RawTransaction transaction, int inIndex) {
        super(transaction, inIndex);
    }

    @Override
    public HashType supportType() {
        return HashType.ALL;
    }

    @Override
    public byte[] data4signature(byte[] preScript) {
        // System.arraycopy(new byte[]{sn[sn.length - 1], 0, 0, 0}, 0, sign, sn.length - 1, 4);
        RawTransaction tx = transaction.clone();
        for (int i = 0; i < tx.getTxIns().length; i++) {
            TxIn txIn = tx.getTxIns()[i];
            if (i != inIndex) {
                txIn.setScript(new byte[]{});
            } else {
                txIn.setScript(preScript);
            }
        }
        ByteBuf sr = tx.serialize(0).writeBytes(new byte[]{HashType.ALL.TYPE, 0, 0, 0});
        byte[] data = ByteUtil.readAll(sr);
        byte[] sha = ByteUtil.sha256sha256(data); // !!  文档是 sha256两次, 实际是一次 !!!
        if (logger.isDebugEnabled()) {
            logger.debug("Will sign data origin: {}, \n ready: {}, \n sha256sha256: {}",
                    ByteUtil.hex(ByteUtil.readAll(tx.serialize(0))),
                    ByteUtil.hex(data),
                    ByteUtil.hex(sha));
        }
        return sha;
    }

    /**
     * 会修改原始交易的属性
     *
     * @param privateKey
     * @param preScript
     * @return
     */
    @Override
    public RawTransaction writeSignatureScript(byte[] privateKey, byte[] preScript) {
        byte[] signature = this.getSignature(privateKey, preScript);
        int sLen = signature.length + 1;
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeByte(sLen);
        buffer.writeBytes(signature);
        buffer.writeByte(supportType().TYPE);
        byte[] pk = new CoinAccount.KeyPrivate(privateKey).getKeyPublic().getKey();
        int pLen = pk.length;
        buffer.writeByte(pLen);
        buffer.writeBytes(pk);
        transaction.getTxIns()[inIndex].setScript(ByteUtil.readAll(buffer));
        return transaction;
    }
}
