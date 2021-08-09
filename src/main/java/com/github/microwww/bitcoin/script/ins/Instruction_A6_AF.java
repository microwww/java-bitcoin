package com.github.microwww.bitcoin.script.ins;

import com.github.microwww.bitcoin.chain.HashType;
import com.github.microwww.bitcoin.math.UintVar;
import com.github.microwww.bitcoin.script.Instruction;
import com.github.microwww.bitcoin.script.Interpreter;
import com.github.microwww.bitcoin.script.ScriptOperation;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.wallet.CoinAccount;
import io.netty.buffer.ByteBuf;
import org.springframework.util.Assert;

import java.util.Arrays;

public enum Instruction_A6_AF implements Instruction {

    // crypto
    OP_RIPEMD160 { // 166

        @Override
        public void exec(Interpreter executor, Object data) {
            byte[] pop = executor.stack.assertNotEmpty().pop();
            byte[] bytes = CoinAccount.ripemd160(pop);
            executor.stack.push(bytes);
        }
    },
    OP_SHA1,
    OP_SHA256 {
        @Override
        public void exec(Interpreter executor, Object data) {
            byte[] pop = executor.stack.assertNotEmpty().pop();
            byte[] bytes = CoinAccount.sha256(pop);
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
            byte[] bytes = CoinAccount.sha256ripemd160(pop);
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
            boolean verify = select.signatureVerify(executor.transaction, executor.getIndexTxIn(), executor.getPreout(), pk, sign, executor.getScriptsFromLastCodeSeparator());
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
    OP_CHECKMULTISIG,
    OP_CHECKMULTISIGVERIFY, // 175
    ;

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
