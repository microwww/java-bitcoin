package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.wallet.CoinAccount;
import com.github.microwww.bitcoin.wallet.Secp256k1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class SignTransaction {
    private static final Logger logger = LoggerFactory.getLogger(SignTransaction.class);

    public enum HashType {
        ALL(1),
        // NONE(2),
        // SINGLE(3),
        // ANYONECANPAY(0x80),
        ;
        public final byte TYPE;

        HashType(byte type) {
            this.TYPE = type;
        }

        HashType(int type) {
            this.TYPE = (byte) type;
        }
    }


    private final RawTransaction transaction;

    /**
     * @param transaction
     */
    public SignTransaction(RawTransaction transaction) {
        this.transaction = transaction;
    }

    public byte[] signature(HashType type, byte[] privateKey, int indexTxIn, byte[] preScript) {
        byte[] bytes = data2signP2PKH(type, indexTxIn, preScript);
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
    public boolean signatureVerify(HashType type, byte[] publicKey, byte[] si, int indexTxIn, byte[] preScript) {
        Assert.isTrue(type.TYPE == 1, "暂时仅支持签名 type = 1 (ALL)的交易, 文档: https://en.bitcoin.it/wiki/OP_CHECKSIG");
        if (logger.isDebugEnabled()) {
            logger.debug("Verify signature PK: {}", ByteUtil.hex(publicKey));
        }
        byte[] bytes = data2signP2PKH(type, indexTxIn, preScript);
        return Secp256k1.signatureVerify(publicKey, si, bytes);
    }

    public byte[] data2signP2PKH(HashType type, int indexTxIn, byte[] preScript) {
        Assert.isTrue(type.TYPE == 1, "暂时仅支持签名 type = 1 (ALL)的交易, 文档: https://en.bitcoin.it/wiki/OP_CHECKSIG");
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
        ByteBuf sr = tx.serialize(0).writeBytes(new byte[]{type.TYPE, 0, 0, 0});
        byte[] data = ByteUtil.readAll(sr);
        byte[] sha = ByteUtil.sha256(data); // !!  文档是 sha256两次, 实际是一次 !!!
        if (logger.isDebugEnabled()) {
            logger.debug("Will sign data origin: {}, \n ready: {}, \n sha256: {}",
                    ByteUtil.hex(ByteUtil.readAll(tx.serialize(0))),
                    ByteUtil.hex(data),
                    ByteUtil.hex(sha));
        }
        return sha;
    }

    public byte[] data2signP2WPKH(HashType type, int indexTxIn, byte[] preScript, long preMount) {
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
                .writeBytes(tx.getTxIns()[indexTxIn].getPreTxHash().fill256bit()).writeIntLE(tx.getTxIns()[indexTxIn].getPreTxOutIndex())
                .writeBytes(preScript)
                .writeLongLE(preMount)
                .writeIntLE(tx.getTxIns()[indexTxIn].getSequence().intValue())
                .writeBytes(hashOutputs)
                .writeIntLE(tx.getLockTime().intValue())
                .writeIntLE(type.TYPE);
        bytes = ByteUtil.readAll(sn);
        byte[] sha256 = ByteUtil.sha256(bytes);
        if (logger.isDebugEnabled()) {
            String hex = ByteUtil.hex(sha256);
            logger.info("TX-sign : \n SHA256({}) \n = {} \n & SHA256({}) \n = {}",
                    ByteUtil.hex(bytes), hex, hex, ByteUtil.hex(ByteUtil.sha256(sha256)));
        }
        return sha256;
    }

    public SignTransaction setScriptP2PKH(HashType type, byte[] privateKey, int indexTxIn, byte[] preScript) {
        byte[] signature = this.signature(type, privateKey, indexTxIn, preScript);
        int sLen = signature.length;
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeByte(sLen);
        buffer.writeBytes(signature);
        byte[] pk = new CoinAccount.KeyPrivate(privateKey).getKeyPublic().getKey();
        int pLen = pk.length;
        buffer.writeByte(pLen);
        buffer.writeBytes(pk);
        transaction.getTxIns()[indexTxIn].setScript(ByteUtil.readAll(buffer));
        return this;
    }

    public SignTransaction setScriptP2WKH(HashType type, byte[] privateKey, int indexTxIn, byte[] preScript) {
        byte[] signature = this.signature(type, privateKey, indexTxIn, preScript);
        int sLen = signature.length;
        byte[] pk = new CoinAccount.KeyPrivate(privateKey).getAddress().getKeyPublicHash();
        int pLen = pk.length;
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeByte(1 + sLen + 1 + pLen);
        buffer.writeByte(sLen);
        buffer.writeBytes(signature);
        buffer.writeByte(pLen);
        buffer.writeBytes(pk);
        return this;
    }
}
