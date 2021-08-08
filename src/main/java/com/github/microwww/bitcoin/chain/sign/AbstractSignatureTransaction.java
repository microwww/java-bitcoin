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

public abstract class AbstractSignatureTransaction implements SignatureTransaction {
    private static final Logger logger = LoggerFactory.getLogger(AbstractSignatureTransaction.class);

    protected final RawTransaction transaction;// 防止修改, 不设置 get/set 方法
    protected final int inIndex;
    protected final int preoutIndex;

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
}
