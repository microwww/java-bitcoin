package com.github.microwww.bitcoin.script.instruction;

import com.github.microwww.bitcoin.chain.HashType;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.math.UintVar;
import com.github.microwww.bitcoin.script.Interpreter;
import com.github.microwww.bitcoin.script.TemplateTransaction;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Arrays;

public class Crypto {
    private static final Logger logger = LoggerFactory.getLogger(Crypto.class);

    // crypto
    static class OP_RIPEMD160 extends AbstractScriptNoOperand {
        public OP_RIPEMD160(int code) {
            super(code);
        } // 166

        @Override
        public void exec(Interpreter executor) {
            byte[] pop = executor.stack.assertNotEmpty().pop();
            byte[] bytes = ByteUtil.ripemd160(pop);
            executor.stack.push(bytes);
        }
    }

    // OP_SHA1,
    static class OP_SHA256 extends AbstractScriptNoOperand {
        public OP_SHA256(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            byte[] pop = executor.stack.assertNotEmpty().pop();
            byte[] bytes = ByteUtil.sha256(pop);
            executor.stack.push(bytes);
        }
    }

    static class OP_HASH160 extends AbstractScriptOperand {
        public OP_HASH160(int code) {
            super(code);
        }

        @Override
        public void operand(ByteBuf bf) {
            this.operand = UintVar.parseAndRead(bf);
        }

        @Override
        public void exec(Interpreter executor) {
            byte[] pop = executor.stack.assertNotEmpty().pop();
            byte[] bytes = ByteUtil.sha256ripemd160(pop);
            executor.stack.push(bytes);
            executor.stack.push(this.operand);
        }
    }

    static class OP_HASH256 extends AbstractScriptNoOperand {
        public OP_HASH256(int code) {
            super(code);
        } // 170

        @Override
        public void exec(Interpreter executor) {
            byte[] pop = executor.stack.assertNotEmpty().pop();
            byte[] bytes = ByteUtil.sha256sha256(pop);
            executor.stack.push(bytes);
        }
    }

    static class OP_CODESEPARATOR extends AbstractScriptOperand {
        public OP_CODESEPARATOR(int code) {
            super(code);
        }

        @Override
        public void operand(ByteBuf bf) {
            int i = bf.readerIndex();
            this.operand = new Uint32(i).toBytes();
        }

        @Override
        public void exec(Interpreter executor) {
            executor.setLastCodeSeparator(new Uint32(this.operand).intValue());
        }
    }

    static class OP_CHECKSIG extends AbstractScriptNoOperand {
        public OP_CHECKSIG(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            byte[] pk = executor.stack.assertSizeGE(2).pop();
            byte[] sn = executor.stack.pop();
            Assert.isTrue(sn.length >= 50, "signature.length > 1, Usually 70-72 bytes.");
            byte[] sign = Arrays.copyOf(sn, sn.length - 1);
            byte type = sn[sn.length - 1];
            HashType select = HashType.select(type);
            byte[] scr = executor.getScriptsFromLastCodeSeparator();
            boolean verify = select.signatureVerify(executor.transaction, executor.getIndexTxIn(), executor.getPreout(), pk, sign, scr);
            if (logger.isDebugEnabled() && !verify) {
                logger.debug("Verify signature : {} \n script: {} \n pk    : {} \n sign  : {}",
                        verify, ByteUtil.hex(scr), ByteUtil.hex(pk), ByteUtil.hex(sign));
            }
            executor.stack.push(verify ? 1 : 0);
        }
    }

    static class OP_CHECKSIGVERIFY extends AbstractScriptNoOperand {
        public OP_CHECKSIGVERIFY(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            new OP_CHECKSIG(ScriptNames.OP_CHECKSIG.ordinal()).exec(executor);
            new FlowControl.OP_VERIFY(ScriptNames.OP_VERIFY.ordinal()).exec(executor);
        }
    }

    static class OP_CHECKMULTISIG extends AbstractScriptNoOperand {

        public OP_CHECKMULTISIG(int code) {
            super(code);
        }

        @Override
        public void exec(Interpreter executor) {
            byte[] pop = executor.stack.assertSizeGE(1).pop();
            int mm = TemplateTransaction.M2N_MAX;
            Assert.isTrue(pop.length == 1, "Must 1");
            int max = Byte.toUnsignedInt(pop[0]);
            Assert.isTrue(max <= mm, "M2N max " + mm);
            executor.stack.assertSizeGE(max);
            byte[][] pks = new byte[max][];
            for (int i = 0; i < max; i++) {
                pks[i] = executor.stack.pop();
            }
            pop = executor.stack.assertSizeGE(1).pop();
            Assert.isTrue(pop.length == 1, "Must 1");
            int req = Byte.toUnsignedInt(pop[0]);
            Assert.isTrue(req <= max, "M2N , request <= max ");
            executor.stack.assertSizeGE(req, "stack data < M2N request :" + req);
            int count = 0;
            for (int i = 0; i < pks.length && count < req; i++) {
                byte[] bytes = executor.stack.pop();
                byte type = bytes[bytes.length - 1];
                for (; i < pks.length; i++) {
                    boolean b = HashType.select(type).signatureVerify(executor.transaction, executor.getIndexTxIn(), executor.getPreout(),
                            pks[i], Arrays.copyOf(bytes, bytes.length - 1), executor.getScriptsFromLastCodeSeparator());
                    if (b) {
                        count++;
                        if (logger.isDebugEnabled()) {
                            logger.debug("Signature-Verify success : \n pk   : {} \n sign : {}", ByteUtil.hex(pks[i]), ByteUtil.hex(bytes));
                        }
                        break;
                    } else {
                        if (logger.isDebugEnabled())
                            logger.debug("Signature-Verify fail : \n pk   : {} \n sign : {}", ByteUtil.hex(pks[i]), ByteUtil.hex(bytes));
                    }
                }
            }
            executor.stack.push(count >= req ? 1 : 0);
        }
    }
    // OP_CHECKMULTISIGVERIFY, // 175
}
