package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.wallet.Secp256k1;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class SignTransaction {
    private static final Logger logger = LoggerFactory.getLogger(SignTransaction.class);
    private final RawTransaction transaction;

    /**
     * 自动 clone, 防止修改
     *
     * @param transaction
     */
    public SignTransaction(RawTransaction transaction) {
        this.transaction = transaction.clone();
    }

    public byte[] signature(byte type, byte[] privateKey, int indexTxIn, byte[] preScript) {
        byte[] bytes = signData(type, indexTxIn, preScript);
        byte[] signature = Secp256k1.signature(privateKey, bytes);
        if (logger.isDebugEnabled()) {
            logger.debug("Will sign private-key: {}, public-key: {}, \n data: {}",
                    "-", // 安全问题, 不显示
                    ByteUtil.hex(Secp256k1.getPublicKey(privateKey)),
                    ByteUtil.hex(signature),
                    ByteUtil.hex(bytes));
        }
        return signature;
    }

    /**
     * preScript TODO :: 需要处理 OP_CODESEPARATOR
     *
     * @param type
     * @param publicKey
     * @param si
     * @param indexTxIn
     * @param preScript
     * @return
     */
    public boolean signatureVerify(byte type, byte[] publicKey, byte[] si, int indexTxIn, byte[] preScript) {
        if (logger.isDebugEnabled()) {
            logger.debug("Verify signature PK: {}", ByteUtil.hex(publicKey));
        }
        byte[] bytes = signData(type, indexTxIn, preScript);
        return Secp256k1.signatureVerify(publicKey, si, bytes);
    }

    public byte[] signData(byte type, int indexTxIn, byte[] preScript) {
        Assert.isTrue(type == 1, "暂时仅支持签名 type = 1 (ALL)的交易, 文档: https://en.bitcoin.it/wiki/OP_CHECKSIG");
        // System.arraycopy(new byte[]{sn[sn.length - 1], 0, 0, 0}, 0, sign, sn.length - 1, 4);
        RawTransaction tx = transaction.clone();
        for (int i = 0; i < tx.getTxIns().length; i++) {
            TxIn txIn = tx.getTxIns()[i];
            if (i != indexTxIn) {
                txIn.setScript(new byte[]{});
            } else {
                txIn.setScript(preScript);
            }
        }
        ByteBuf sr = tx.serialize(0).writeBytes(new byte[]{type, 0, 0, 0});
        byte[] data = ByteUtil.readAll(sr);
        byte[] sha = ByteUtil.sha256(data); // !!  文档是 sha256两次, 实际是一次 !!!
        if (logger.isDebugEnabled()) {
            logger.debug("Will sign data origin: {}, \n ready: {}, \n sha256: {}",
                    ByteUtil.hex(ByteUtil.readAll(transaction.serialize(0))),
                    ByteUtil.hex(data),
                    ByteUtil.hex(sha));
        }
        return sha;
    }
}
