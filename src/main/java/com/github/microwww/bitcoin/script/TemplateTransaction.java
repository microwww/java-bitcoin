package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.script.ex.TransactionInvalidException;
import com.github.microwww.bitcoin.script.ins.Instruction_61_6A;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.wallet.CoinAccount;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Arrays;

import static com.github.microwww.bitcoin.script.ins.Instruction_6B_7D.OP_DROP;
import static com.github.microwww.bitcoin.script.ins.Instruction_6B_7D.OP_DUP;
import static com.github.microwww.bitcoin.script.ins.Instruction_83_8A.OP_EQUAL;
import static com.github.microwww.bitcoin.script.ins.Instruction_83_8A.OP_EQUALVERIFY;
import static com.github.microwww.bitcoin.script.ins.Instruction_A6_AF.*;

public enum TemplateTransaction {
    P2PK {
        @Override
        public byte[] scriptPubKey(byte[]... args) {
            Assert.isTrue(args.length > 0, "one arg for address");
            ByteBuf bf = Unpooled.buffer().writeByte(OP_DUP.opcode())
                    .writeByte(OP_HASH160.opcode())
                    .writeByte(args[0].length).writeBytes(args[0])
                    .writeByte(OP_EQUALVERIFY.opcode())
                    .writeByte(OP_CHECKSIG.opcode());
            Assert.isTrue(25 == bf.readableBytes(), "Length 25");
            return ByteUtil.readAll(bf);
        }
    },
    P2PKH {
        @Override
        public byte[] scriptPubKey(byte[]... args) {
            return P2PK.scriptPubKey(args);
        }
    },
    MN {// static bool MatchMultisig(const CScript& script, unsigned int& required, std::vector<valtype>& pubkeys)

        /**
         * M <Public Key 1> <Public Key 2> … <Public Key N>
         * @param args
         * @return
         */
        @Override
        public byte[] scriptPubKey(byte[]... args) {
            // M <Public Key 1> <Public Key 2> … <Public Key N> N CHECKMULTISIG
            Assert.isTrue(args.length > 0, "one arg for address");
            Assert.isTrue(args[0].length == 1, "args[0].length == 1");
            byte m = args[0][0];
            ByteBuf bf = Unpooled.buffer()
                    .writeByte(m);
            for (int i = 1; i < args.length; i++) {
                bf.writeBytes(args[i]);
            }
            int n = args.length - 1;
            bf.writeByte(n)
                    .writeByte(OP_CHECKMULTISIG.opcode());
            Assert.isTrue(m <= n, "M <= N :  M <Public Key 1> <Public Key 2> … <Public Key N>");
            return ByteUtil.readAll(bf);
        }
    },
    P2SH { // bool CScript::IsPayToScriptHash() const

        @Override
        public byte[] scriptPubKey(byte[]... args) {
            Assert.isTrue(args.length > 0, "one arg for address");
            ByteBuf bf = Unpooled.buffer()
                    .writeByte(OP_HASH160.opcode())
                    .writeByte(args[0].length).writeBytes(args[0])
                    .writeByte(OP_EQUAL.opcode());
            Assert.isTrue(23 == bf.readableBytes(), "Length 23");
            return ByteUtil.readAll(bf);
        }
    },
    P2WPKH() {
        @Override
        public boolean isSupport(byte[] data) {// [0x00][0x14] [Hash160(PK)]
            return TemplateTransaction.p2wSupport(data, 0x14 + 2, (byte) 0, (byte) 0x14);
        }

        @Override
        public byte[] scriptPubKey(byte[]... args) {
            Assert.isTrue(args.length > 0, "one arg for address");
            // 0x00 0x14 20
            ByteBuf bf = Unpooled.buffer()
                    .writeByte(0)
                    .writeByte(0x14)
                    .writeBytes(args[0]);
            Assert.isTrue(22 == bf.readableBytes(), "Length 22");
            return ByteUtil.readAll(bf);
        }

        @Override
        public void executor(Interpreter interpreter) {
            byte[] pop = interpreter.stack.peek(2);
            byte[] bytes = CoinAccount.sha256ripemd160(pop);
            if (log.isDebugEnabled())
                log.debug("PK : sha256ripemd160({}), {}", ByteUtil.hex(pop), ByteUtil.hex(bytes));
            // CScript witScriptPubkey = CScript() << OP_DUP << OP_HASH160 << ToByteVector(pubkeyHash) << OP_EQUALVERIFY << OP_CHECKSIG;
            // ByteUtil.hex("1976a914 <public-key-hash> 88ac");
            byte[] addr = interpreter.stack.pop();
            byte[] scr = new byte[26];
            scr[0] = 0x19;
            scr[1] = OP_DUP.opcode();
            scr[2] = OP_HASH160.opcode();
            scr[3] = (byte) addr.length;
            System.arraycopy(addr, 0, scr, 4, addr.length);
            scr[24] = OP_EQUALVERIFY.opcode();
            scr[25] = OP_CHECKSIG.opcode();
            interpreter.stack.pop();
            interpreter.executor(scr, 1);
        }
    },
    P2WSH() {// bool CScript::IsPayToWitnessScriptHash() const

        @Override
        public boolean isSupport(byte[] data) {// [0x00][0x20] [SHA256(witnessScript)]
            return TemplateTransaction.p2wSupport(data, 0x20 + 2, (byte) 0, (byte) 0x20);
        }

        @Override
        public byte[] scriptPubKey(byte[]... args) {
            Assert.isTrue(args.length > 0, "one arg for address");
            // VER OPCODE_LEN ADDR 34
            ByteBuf bf = Unpooled.buffer()
                    .writeByte(0)
                    .writeByte(0x20)
                    .writeBytes(args[0]);
            Assert.isTrue(34 == bf.readableBytes(), "Length 34");
            return ByteUtil.readAll(bf);
        }

        @Override
        public void executor(Interpreter interpreter) {
            byte[] sha256 = interpreter.stack.assertSizeGE(2).pop();
            OP_DROP.exec(interpreter, ZERO);
            byte[] sc = interpreter.stack.pop();
            if (!Arrays.equals(sha256, ByteUtil.sha256(sc))) {
                throw new TransactionInvalidException("sha256(script) != P2WSH");
            }
            byte[] bytes = ByteUtil.concat(new byte[]{(byte) sc.length}, sc);
            interpreter.subScript().executor(bytes, 1);
        }
    },

    P2SH_P2WPKH {
        @Override
        public byte[] scriptPubKey(byte[]... args) {
            return P2SH.scriptPubKey(args);
        }
    },
    P2SH_P2WSH {
        @Override
        public byte[] scriptPubKey(byte[]... args) {
            return P2SH.scriptPubKey(args);
        }

        @Override
        public void executor(Interpreter interpreter) {
            P2WSH.executor(interpreter);
        }
    },
    OP_RETURN {
        @Override
        public byte[] scriptPubKey(byte[]... args) {
            Assert.isTrue(args.length > 0, "one arg for address");
            ByteBuf bf = Unpooled.buffer()
                    .writeByte(Instruction_61_6A.OP_RETURN.opcode())
                    .writeByte(args[0].length).writeBytes(args[0]);
            Assert.isTrue(20 + 2 <= bf.readableBytes(), "Length 22");
            return ByteUtil.readAll(bf);
        }
    },
    ;

    private static final Logger log = LoggerFactory.getLogger(TemplateTransaction.class);

    public boolean isSupport(byte[] data) {
        return false;
    }

    public byte[] scriptPubKey(byte[]... args) {// for out script
        throw new UnsupportedOperationException();
    }

    public byte[] scriptSig(byte[]... args) {// for in script, signature and public-key
        throw new UnsupportedOperationException();
    }

    private static boolean p2wSupport(byte[] data, int len, byte... bts) {
        if (data.length == len) {
            for (int i = 0; i < bts.length; i++) {
                if (data[i] != bts[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public void executor(Interpreter interpreter) {
        throw new UnsupportedOperationException("TemplateTransaction." + this.name());
    }
}
