package com.github.microwww.bitcoin.script.ins;

import com.github.microwww.bitcoin.chain.HashType;
import com.github.microwww.bitcoin.math.UintVar;
import com.github.microwww.bitcoin.script.Instruction;
import com.github.microwww.bitcoin.script.Interpreter;
import com.github.microwww.bitcoin.script.ScriptOperation;
import com.github.microwww.bitcoin.script.TemplateTransaction;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Arrays;

public enum Instruction_A6_AF implements Instruction {

    // crypto
    OP_RIPEMD160 { // 166

        @Override
        public void exec(Interpreter executor, Object data) {
            byte[] pop = executor.stack.assertNotEmpty().pop();
            byte[] bytes = ByteUtil.ripemd160(pop);
            executor.stack.push(bytes);
        }
    },
    OP_SHA1,
    OP_SHA256 {
        @Override
        public void exec(Interpreter executor, Object data) {
            byte[] pop = executor.stack.assertNotEmpty().pop();
            byte[] bytes = ByteUtil.sha256(pop);
            executor.stack.push(bytes);
        }
    },
    OP_HASH160 {
        @Override
        public ScriptOperation compile(ByteBuf bf) {
            return new ScriptOperation(this, UintVar.parseAndRead(bf));
        }

        @Override
        public void exec(Interpreter executor, Object data, int pc) {
            byte[] pop = executor.stack.assertNotEmpty().pop();
            byte[] bytes = ByteUtil.sha256ripemd160(pop);
            executor.stack.push(bytes);
            bytes = (byte[]) data;
            executor.stack.push(bytes);
        }
    },
    OP_HASH256 { // 170

        @Override
        public void exec(Interpreter executor, Object data) {
            byte[] pop = executor.stack.assertNotEmpty().pop();
            byte[] bytes = ByteUtil.sha256sha256(pop);
            executor.stack.push(bytes);
        }
    },
    OP_CODESEPARATOR() {
        @Override
        public ScriptOperation compile(ByteBuf bf) {
            int i = bf.readerIndex();
            return new ScriptOperation(this, i);
        }

        @Override
        public void exec(Interpreter executor, Object data) {
            executor.setLastCodeSeparator((Integer) data);
        }
    },
    OP_CHECKSIG() {
        @Override
        public void exec(Interpreter executor, Object data) {
            byte[] pk = executor.stack.assertSizeGE(2).pop();
            byte[] sn = executor.stack.pop();
            Assert.isTrue(sn.length >= 50, "signature.length > 1, general == 71 / 72");
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
    },
    OP_CHECKSIGVERIFY {
        @Override
        public void exec(Interpreter executor, Object data) {
            OP_CHECKSIG.exec(executor, data);
            Instruction_61_6A.OP_VERIFY.exec(executor, data);
        }
    },
    OP_CHECKMULTISIG {
        @Override
        public ScriptOperation compile(ByteBuf bf) {
            return new ScriptOperation(this, ZERO);
        }

        @Override
        public void exec(Interpreter executor, Object data) {
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
            for (int i = 0; i < pks.length; i++) {
                byte[] bytes = executor.stack.pop();
                byte type = bytes[bytes.length - 1];
                for (; i < pks.length; i++) {
                    boolean b = HashType.select(type).signatureVerify(executor.transaction, executor.getIndexTxIn(), executor.getPreout(),
                            pks[i], Arrays.copyOf(bytes, bytes.length - 1), executor.getScriptsFromLastCodeSeparator());
                    if (b) {
                        count++;
                        if (logger.isDebugEnabled()) {
                            logger.debug("Signature-Verify success : \n pk   : {}, \n sign : {}", ByteUtil.hex(pks[i]), ByteUtil.hex(bytes));
                        }
                        break;
                    } else {
                        if (logger.isDebugEnabled())
                            logger.debug("Signature-Verify fail : \n pk   : {}, \n sign : {}", ByteUtil.hex(pks[i]), ByteUtil.hex(bytes));
                    }
                }
            }
            executor.stack.push(count >= req ? 1 : 0);
        }
    },
    OP_CHECKMULTISIGVERIFY, // 175
    ;
    private static final Logger logger = LoggerFactory.getLogger(Instruction_A6_AF.class);

    @Override
    public ScriptOperation compile(ByteBuf bf) {
        return new ScriptOperation(this, ZERO);
    }

    @Override
    public void exec(Interpreter executor, Object data, int pc) {
        this.exec(executor, data);
    }

    public void exec(Interpreter executor, Object data) {
        throw new UnsupportedOperationException(this.name());
    }

    public byte opcode() {
        return (byte) (0xA6 + this.ordinal());
    }
}
