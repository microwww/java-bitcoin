package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.script.ex.TransactionInvalidException;
import com.github.microwww.bitcoin.script.instruction.StackOps;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.wallet.CoinAccount;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Arrays;

import static com.github.microwww.bitcoin.script.instruction.ScriptNames.*;

public enum TemplateTransaction {

    /**
     * 0x??[0x41|0x21]...0xAC
     * [PUBLIC-KEY] OP_CHECKSIG
     *
     * 04b0bd634234abbb1ba1e986e884185c61cf43e001f9137f23c2c409273eb16e6537a576782eba668a7ef8bd3b3cfb1edb7117ab65129b8a2e681f3c1e0908ef7b
     * OP_CHECKSIG
     */
    P2PK {
        @Override
        public boolean isSupport(byte[] data) {
            return parseAddress(data) != null;
        }

        @Override
        public byte[] parseAddress(byte[] data) {
            byte[] v = null;
            if (data.length >= 0x21 + 1) {
                if (data[data.length - 1] == OP_CHECKSIG.opcode()) {
                    if (data.length == 1 + 0x41 + 1) {
                        if (data[1] == 0x04) { // 不压缩的公钥 04 + <64位>
                            v = Arrays.copyOfRange(data, 1, data.length - 1);
                        }
                    } else if (data.length == 1 + 0x21 + 1) {
                        if (data[1] == 0x02 || data[1] == 0x03) { // 压缩的公钥 02/03 + <32位>
                            v = Arrays.copyOfRange(data, 1, data.length - 1);
                        }
                    }
                }
            }
            if (v != null) {
                v = new CoinAccount.KeyPublic(v).getAddress().getKeyPublicHash();
            }
            return v;
        }

        @Override
        public byte[] scriptPubKey(byte[]... args) {
            Assert.isTrue(args.length == 1, "one arg for address");
            ByteBuf bf = Unpooled.buffer()
                    .writeByte(args[0].length)
                    .writeBytes(args[0])
                    .writeByte(OP_CHECKSIG.opcode());
            Assert.isTrue(22 == bf.readableBytes(), "Length 25");
            return ByteUtil.readAll(bf);
        }
    },
    /**
     * OP_DUP OP_HASH160 [0×14][PKHash] OP_EQUALVERIFY OP_CHECKSIG
     * 76 a9 14-c825a1ecf2a6830c4401620c3a16f1995057c2ab 88 ac
     * <p>
     * c++ MatchPayToPubkeyHash
     */
    P2PKH {
        @Override
        public boolean isSupport(byte[] data) {
            return parseAddress(data) != null;
        }

        @Override
        public byte[] parseAddress(byte[] data) {
            if (data.length == 0x14 + 5) {
                boolean padding = TemplateTransaction.paddingWith(data,
                        new byte[]{OP_DUP.opcode(), OP_HASH160.opcode(), 0x14},
                        new byte[]{OP_EQUALVERIFY.opcode(), OP_CHECKSIG.opcode()});
                if (padding) {
                    return Arrays.copyOfRange(data, 3, 3 + 0x14);
                }
            }
            return null;
        }

        @Override
        public byte[] scriptPubKey(byte[]... args) {
            Assert.isTrue(args.length == 1, "one arg for address");
            ByteBuf bf = Unpooled.buffer().writeByte(OP_DUP.opcode())
                    .writeByte(OP_HASH160.opcode())
                    .writeByte(args[0].length)
                    .writeBytes(args[0])
                    .writeByte(OP_EQUALVERIFY.opcode())
                    .writeByte(OP_CHECKSIG.opcode());
            Assert.isTrue(25 == bf.readableBytes(), "Length 25");
            return ByteUtil.readAll(bf);
        }
    },
    /**
     * static bool MatchMultisig(const CScript& script, unsigned int& required, std::vector<valtype>& pubkeys)
     * <p>
     * M <Public Key 1> <Public Key 2> … <Public Key N>
     */
    MN {
        @Override
        public boolean isSupport(byte[] data) {
            if (data != null && data.length > 1) {
                if (data[data.length - 1] == OP_CHECKMULTISIG.opcode()) {
                    int code = data[0] & 0x0F;
                    if (IsSmallInteger(code)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean IsSmallInteger(int opcode) {
            return opcode >= _1.opcode() && opcode < _16.opcode();
        }

        @Override
        public void executor(Interpreter interpreter) {
            interpreter.runNow();
        }

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
    /**
     * bool CScript::IsPayToScriptHash() const
     * <p>
     * OP_HASH160 [Hash160(redeemScript)] OP_EQUAL
     */
    P2SH {
        @Override
        public boolean isSupport(byte[] data) {
            return parseAddress(data) != null;
        }

        @Override
        public byte[] parseAddress(byte[] data) {
            if (data.length == 23) {
                boolean padding = TemplateTransaction.paddingWith(data, new byte[]{OP_HASH160.opcode(), 0x14}, new byte[]{OP_EQUAL.opcode()});
                if (padding) {
                    return Arrays.copyOfRange(data, 2, 0x14 + 2);
                }
            }
            return null;
        }

        @Override
        public void executor(Interpreter interpreter) {
            byte[] pop = interpreter.stack.peek();
            interpreter.runNow();
            long time = interpreter.getBlock().map(e -> e.header.getTime().longValue())
                    .orElse(System.currentTimeMillis() / 1000).longValue();
            if (time < 1_333_238_400) { // 软分叉交易, height 173_750, blocks with timestamps >= 1333238400, 2012.04.01
                // https://www.blockchain.com/btc/tx/6a26d2ecb67f27d1fa5524763b49029d7106e91e3cc05743073461a719776192
                log.info("OLD P2SH M2N transaction : {}", interpreter.transaction.hash());
                return;
            }
            Assert.isTrue(interpreter.isSuccess(true), "P2SH address is hash160 not equals");
            if (log.isDebugEnabled())
                log.debug("P2SH script hash160 equals: {}", ByteUtil.hex(ByteUtil.sha256ripemd160(pop)));
            // TODO: 如果在解锁脚本中存在任何的push data操作码以外的操作码，验证失败。
            interpreter.executor(pop);
        }

        @Override
        public byte[] scriptPubKey(byte[]... args) {
            Assert.isTrue(args.length == 1, "one arg for address");
            ByteBuf bf = Unpooled.buffer()
                    .writeByte(OP_HASH160.opcode())
                    .writeByte(args[0].length).writeBytes(args[0])
                    .writeByte(OP_EQUAL.opcode());
            Assert.isTrue(23 == bf.readableBytes(), "Length 23");
            return ByteUtil.readAll(bf);
        }
    },
    /**
     * [0x00][0x14] [Hash160(PK)]
     */
    P2WPKH() {
        @Override
        public boolean isSupport(byte[] data) {
            return parseAddress(data) != null;
        }

        @Override
        public byte[] parseAddress(byte[] data) {
            if (data.length == 0x14 + 2) {
                boolean padding = TemplateTransaction.paddingWith(data, new byte[]{0, 0x14}, new byte[]{});
                if (padding) {
                    return Arrays.copyOfRange(data, 2, data.length);
                }
            }
            return null;
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
            interpreter.runNow();
            byte[] pop = interpreter.stack.peek(2);
            byte[] bytes = ByteUtil.sha256ripemd160(pop);
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
    /**
     * [0x00][0x20] [SHA256(witnessScript)]
     */
    P2WSH() {// bool CScript::IsPayToWitnessScriptHash() const

        @Override
        public boolean isSupport(byte[] data) {
            return parseAddress(data) != null;
        }

        @Override
        public byte[] parseAddress(byte[] data) {
            if (data.length == 0x20 + 2) {
                boolean padding = TemplateTransaction.paddingWith(data, new byte[]{0, 0x20}, new byte[]{});
                if (padding) {
                    return Arrays.copyOfRange(data, 2, data.length);
                }
            }
            return null;
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
            interpreter.runNow();
            byte[] sha256 = interpreter.stack.assertSizeGE(2).pop();
            new StackOps.OP_DROP(OP_DROP.opcode()).exec(interpreter);
            byte[] sc = interpreter.stack.pop();
            if (!Arrays.equals(sha256, ByteUtil.sha256(sc))) {
                throw new TransactionInvalidException("sha256(script) != P2WSH");
            }
            if (log.isDebugEnabled())
                log.debug("sha256(script) != P2WSH success : {}", ByteUtil.hex(sha256));
            byte[] bytes = ByteUtil.concat(new byte[]{(byte) sc.length}, sc);
            //TODO::这个规则需要确认
            Assert.isTrue(bytes.length - 1 == Byte.toUnsignedInt(bytes[0]), "这个规则需要确认");
            interpreter.executor(bytes, 1);
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
    /**
     * OP_RETURN
     * Default setting for nMaxDatacarrierBytes. 80 bytes of data, +1 for OP_RETURN,
     * +2 for the pushdata opcodes.
     */
    RETURN {
        private int max = 0x53;

        @Override
        public boolean isSupport(byte[] data) {
            if (data.length <= max) {
                if (data.length > 1) {
                    if (data[0] == OP_RETURN.opcode()) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public byte[] scriptPubKey(byte[]... args) {
            Assert.isTrue(args.length > 0, "one arg for address");
            ByteBuf bf = Unpooled.buffer()
                    .writeByte(OP_RETURN.opcode())
                    .writeByte(args[0].length).writeBytes(args[0]);
            Assert.isTrue(max >= bf.readableBytes(), "Length 0x53(83)");
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

    private static boolean paddingWith(byte[] data, byte[] start, byte[] end) {
        if (data.length < start.length + end.length) {
            return false;
        }
        for (int i = 0; i < start.length; i++) {
            if (start[i] != data[i]) {
                return false;
            }
        }
        int offset = data.length - end.length;
        for (int i = 0; i < end.length; i++) {
            if (end[i] != data[offset + i]) {
                return false;
            }
        }
        return true;
    }

    public void executor(Interpreter interpreter) {
        interpreter.runNow();
    }

    public byte[] parseAddress(byte[] data) {
        throw new UnsupportedOperationException();
    }

    public static byte[] getScriptForMultiSig(int nRequest, byte[]... pks) {
        ByteBuf bf = Unpooled.buffer().writeByte(0x50 | nRequest);
        for (byte[] pk : pks) {
            bf.writeByte(pk.length);
            bf.writeBytes(pk);
        }
        bf.writeByte(0x50 | pks.length);
        bf.writeByte(OP_CHECKMULTISIG.opcode());
        Assert.isTrue(bf.readableBytes() <= BytesStack.MAX_SCRIPT_ELEMENT_SIZE, "Max 520 byte");
        return ByteUtil.readAll(bf);
    }

    public static final int M2N_MAX = 0x16;
}
