package com.github.microwww.bitcoin.chain.sign;

import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.SignatureTransaction;
import com.github.microwww.bitcoin.chain.TxIn;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.wallet.CoinAccount;
import com.github.microwww.bitcoin.wallet.Secp256k1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSignatureTransaction implements SignatureTransaction {
    private static final Logger logger = LoggerFactory.getLogger(AbstractSignatureTransaction.class);

    protected final RawTransaction transaction;// 防止修改, 不设置 get/set 方法
    protected final int inIndex;
    protected final int preoutIndex;
    protected boolean anyOneCanPay = false;

    public AbstractSignatureTransaction(RawTransaction transaction, int inIndex) {
        this.transaction = transaction;
        this.inIndex = inIndex;
        this.preoutIndex = transaction.getTxIns()[inIndex].getPreTxOutIndex();
    }

    @Override
    public byte[] getSignature(byte[] privateKey, byte[] preScript) {
        byte[] bytes = data4signature(preScript);
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

    public byte[] data4signature(RawTransaction tx, byte[] preScript) {
        // System.arraycopy(new byte[]{sn[sn.length - 1], 0, 0, 0}, 0, sign, sn.length - 1, 4);
        for (int i = 0; i < tx.getTxIns().length; i++) {
            TxIn txIn = tx.getTxIns()[i];
            if (i != inIndex) {
                txIn.setScript(new byte[]{});
            } else {
                txIn.setScript(preScript);
            }
        }
        ByteBuf sr = tx.serialize(0).writeBytes(new byte[]{this.supportType().toByte(), 0, 0, 0});
        byte[] data = ByteUtil.readAll(sr);
        byte[] sha = ByteUtil.sha256sha256(data);
        if (logger.isDebugEnabled()) {
            logger.debug("Will sign data \n origin : {}, \n ready  : {}, \n dSha256: {}",
                    ByteUtil.hex(ByteUtil.readAll(this.transaction.serialize(0))),
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

    /**
     * @param publicKey
     * @param signature 先去掉最后一个字节的标识位
     * @param preScript // TODO :: 需要处理 OP_CODESEPARATOR
     * @return
     */
    @Override
    public boolean signatureVerify(byte[] publicKey, byte[] signature, byte[] preScript) {
        //HashType type = HashType.select(signature[signature.length - 1]);
        //Assert.isTrue(type.TYPE == 1, "暂时仅支持签名 type = 1 (ALL)的交易, 文档: https://en.bitcoin.it/wiki/OP_CHECKSIG");
        if (logger.isDebugEnabled()) {
            logger.debug("Verify {} signature PK: {}", this.getClass().getSimpleName(), ByteUtil.hex(publicKey));
        }
        byte[] bytes = data4signature(preScript);
        return Secp256k1.signatureVerify(publicKey, signature, bytes);
    }

    public int getInIndex() {
        return inIndex;
    }

    public int getPreoutIndex() {
        return preoutIndex;
    }

    public boolean isAnyOneCanPay() {
        return anyOneCanPay;
    }

    public AbstractSignatureTransaction setAnyOneCanPay(boolean anyOneCanPay) {
        this.anyOneCanPay = anyOneCanPay;
        return this;
    }
}
