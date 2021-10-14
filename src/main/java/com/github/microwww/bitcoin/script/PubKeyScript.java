package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.script.ex.TransactionInvalidException;
import com.github.microwww.bitcoin.script.instruction.Script;
import com.github.microwww.bitcoin.script.instruction.ScriptNames;
import com.github.microwww.bitcoin.script.instruction.StackOps;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.wallet.CoinAccount;
import com.github.microwww.bitcoin.wallet.Env;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Predicate;

import static com.github.microwww.bitcoin.script.instruction.ScriptNames.*;

public class PubKeyScript {
    private static final Logger log = LoggerFactory.getLogger(PubKeyScript.class);

    public enum Type {
        /**
         * [PUBLIC-KEY] OP_CHECKSIG
         */
        P2PK {
            private final Mather mather = new Mather().desc(ScriptNames.OP_CHECKSIG).asc(script -> {
                return CoinAccount.KeyPublic.checkFormat(script.getOperand());
            });

            @Override
            public boolean match(List<Script> scripts) {
                if (scripts.size() != 2) {
                    return false;
                }
                return mather.test(scripts);
            }

            @Override
            public CoinAccount.Address parseAddress(List<Script> scripts) {
                return new CoinAccount.KeyPublic(scripts.get(0).getOperand()).getAddress();
            }

            @Override
            public ByteBuf scriptPubKey(byte[]... args) {
                Assert.isTrue(args.length == 1, "One arg for address");
                int length = args[0].length;
                Assert.isTrue(CoinAccount.KeyPublic.checkFormat(args[0]), "PK 04[32+][32+] / [02|03][32+]");
                ByteBuf bf = Unpooled.buffer()
                        .writeByte(length)
                        .writeBytes(args[0])
                        .writeByte(OP_CHECKSIG.opcode());
                return bf;
            }
        },
        /**
         * OP_DUP OP_HASH160 [0×14][PKHash] OP_EQUALVERIFY OP_CHECKSIG
         * <br/>
         * 76 a9 14-c825a1ecf2a6830c4401620c3a16f1995057c2ab 88 ac
         * <br/>
         * c++ MatchPayToPubkeyHash
         */
        P2PKH {
            private final Mather mather = new Mather()
                    .asc(ScriptNames.OP_DUP)
                    .asc(ScriptNames.OP_HASH160)
                    // .asc(ScriptNames._20)
                    .asc(ScriptNames.OP_EQUALVERIFY)
                    .asc(ScriptNames.OP_CHECKSIG);

            @Override
            public boolean match(List<Script> scripts) {
                if (scripts.size() != 4) {
                    return false;
                }
                return this.mather.test(scripts);
            }

            @Override
            public CoinAccount.Address parseAddress(List<Script> scripts) {
                return new CoinAccount.Address(scripts.get(1).getOperand());
            }

            @Override
            public ByteBuf scriptPubKey(byte[]... args) {
                Assert.isTrue(args.length == 1, "one arg for address");
                ByteBuf bf = Unpooled.buffer().writeByte(OP_DUP.opcode())
                        .writeByte(OP_HASH160.opcode())
                        .writeByte(args[0].length)
                        .writeBytes(args[0])
                        .writeByte(OP_EQUALVERIFY.opcode())
                        .writeByte(OP_CHECKSIG.opcode());
                Assert.isTrue(25 == bf.readableBytes(), "Length 25");
                return bf;
            }
        },
        /**
         * static bool MatchMultisig(const CScript& script, unsigned int& required, std::vector<valtype>& pubkeys)
         * <p>
         * M <Public Key 1> <Public Key 2> … <Public Key N> OP_CHECKMULTISIG
         */
        MN {
            private final Mather mather = new Mather().asc(script -> {
                return ScriptNames.OP_1.ordinal() <= script.opcode()
                        && script.opcode() <= ScriptNames.OP_16.ordinal();
            }).desc(ScriptNames.OP_CHECKMULTISIG);

            @Override
            public boolean match(List<Script> scripts) {
                if (scripts.size() < 2) {
                    return false;
                }
                return this.mather.test(scripts);
            }

            @Override
            public int getRequestSigns(List<Script> scripts) {
                return scripts.get(0).getOperand()[0];
            }

            @Override
            public ByteBuf scriptPubKey(byte[]... args) {
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
                return bf;
            }
        },
        /**
         * OP_HASH160 [Hash160(redeemScript)] OP_EQUAL
         */
        P2SH {
            private final Mather mather = new Mather().asc(ScriptNames.OP_HASH160)
                    // .asc(ScriptNames._20)
                    .desc(ScriptNames.OP_EQUAL);

            @Override
            public boolean match(List<Script> scripts) {
                if (scripts.size() != 2) {
                    return false;
                }
                return this.mather.test(scripts);
            }

            @Override
            public CoinAccount.Address parseAddress(List<Script> scripts) {
                return new CoinAccount.Address(scripts.get(0).getOperand());
            }

            @Override
            public ByteBuf scriptPubKey(byte[]... args) {
                Assert.isTrue(args.length == 1, "one arg for address");
                ByteBuf bf = Unpooled.buffer()
                        .writeByte(OP_HASH160.opcode())
                        .writeByte(args[0].length).writeBytes(args[0])
                        .writeByte(OP_EQUAL.opcode());
                Assert.isTrue(23 == bf.readableBytes(), "Length 23");
                return bf;
            }

            @Override
            public void executor(Interpreter interpreter) {
                byte[] redeemScript = interpreter.stack.peek();
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
                    log.debug("P2SH script hash160 equals: {}", ByteUtil.hex(ByteUtil.sha256ripemd160(redeemScript)));
                // TODO: 如果在解锁脚本中存在任何的push data操作码以外的操作码，验证失败。
                // redeemScript
                interpreter.executor(redeemScript);
            }
        },
        /**
         * [0x00][0x14] [Hash160(PK)]
         */
        P2WPKH {
            private final Mather mather = new Mather().asc(ScriptNames.OP_0)
                    .asc(ScriptNames._20);

            @Override
            public boolean match(List<Script> scripts) {
                if (scripts.size() != 2) {
                    return false;
                }
                return this.mather.test(scripts);
            }

            @Override
            public CoinAccount.Address parseAddress(List<Script> scripts) {
                return new CoinAccount.Address(scripts.get(1).getOperand());
            }

            @Override
            public ByteBuf scriptPubKey(byte[]... args) {
                Assert.isTrue(args.length > 0, "one arg for address");
                // 0x00 0x14 20
                ByteBuf bf = Unpooled.buffer()
                        .writeByte(0)
                        .writeByte(0x14)
                        .writeBytes(args[0]);
                Assert.isTrue(22 == bf.readableBytes(), "Length 22");
                return bf;
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
        P2WSH {
            private final Mather mather = new Mather().asc(ScriptNames.OP_0)
                    .asc(ScriptNames._32);

            @Override
            public boolean match(List<Script> scripts) {
                if (scripts.size() != 2) {
                    return false;
                }
                return this.mather.test(scripts);
            }

            @Override
            public CoinAccount.Address parseAddress(List<Script> scripts) {
                return new CoinAccount.Address(scripts.get(1).getOperand());
            }

            @Override
            public ByteBuf scriptPubKey(byte[]... args) {
                Assert.isTrue(args.length > 0, "one arg for address");
                // VER OPCODE_LEN ADDR 34
                ByteBuf bf = Unpooled.buffer()
                        .writeByte(0)
                        .writeByte(0x20)
                        .writeBytes(args[0]);
                Assert.isTrue(34 == bf.readableBytes(), "Length 34");
                return bf;
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
        /**
         * OP_HASH160 [Hash160(redeemScript)] OP_EQUAL,
         * redeemScript : [0x00][0x14][Hash160(PK)]
         */
        P2SH_P2WPKH {
            @Override
            public ByteBuf scriptPubKey(byte[]... args) {
                Assert.isTrue(args.length == 1, "one arg for Hash160(PK)");
                ByteBuf bf = Unpooled.buffer()
                        .writeByte(0)
                        .writeByte(0x14)
                        .writeBytes(args[0]);
                Assert.isTrue(0x16 == bf.readableBytes(), "Length 20");
                return bf;
            }
        },
        /**
         * OP_HASH160 [Hash160(redeemScript)] OP_EQUAL
         * redeemScript : [0x00][0x20][Hash256(WitnessScript)]
         */
        P2SH_P2WSH {
            @Override
            public ByteBuf scriptPubKey(byte[]... args) {
                Assert.isTrue(args.length == 1, "one arg for Hash256(WitnessScript)");
                ByteBuf bf = Unpooled.buffer()
                        .writeByte(0)
                        .writeByte(0x20)
                        .writeBytes(args[0]);
                Assert.isTrue(0x22 == bf.readableBytes(), "Length 20");
                return bf;
            }
        },
        /**
         * OP_RETURN
         * Default setting for nMaxDatacarrierBytes. 80 bytes of data, +1 for OP_RETURN,
         * +2 for the pushdata opcodes.
         */
        RETURN {
            public static final int max = 0x50 + 1 + 2;
            private final Mather mather = new Mather().asc(ScriptNames.OP_RETURN);

            @Override
            public boolean match(List<Script> scripts) {
                if (scripts.size() < 2) {
                    return false;
                }
                return this.mather.test(scripts);
            }

            @Override
            public ByteBuf scriptPubKey(byte[]... args) {
                Assert.isTrue(args.length > 0, "one arg for address");
                ByteBuf bf = Unpooled.buffer()
                        .writeByte(OP_RETURN.opcode())
                        .writeByte(args[0].length).writeBytes(args[0]);
                Assert.isTrue(max >= bf.readableBytes(), "Length 0x53(83)");
                return bf;
            }
        },
        UNKNOWN {
            @Override
            public boolean match(List<Script> scripts) {
                return true;
            }
        };

        public boolean match(List<Script> scripts) {
            return false;
        }

        public ByteBuf scriptPubKey(byte[]... args) {// for tx-out pk script
            throw new UnsupportedOperationException();
        }

        public void executor(Interpreter interpreter) {
            interpreter.runNow();
        }

        public CoinAccount.Address parseAddress(List<Script> scripts) {
            throw new UnsupportedOperationException();
        }

        /**
         * request out to sign count, generate 1, MN have more !
         *
         * @return
         */
        public int getRequestSigns(List<Script> scripts) {
            return 1;
        }
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
    private final Type type;
    private final List<Script> scripts;

    public PubKeyScript(Type type, List<Script> scripts) {
        this.type = type;
        this.scripts = scripts;
    }

    public static PubKeyScript parseScript(byte[] bytes) {
        List<Script> scripts = compile(Unpooled.copiedBuffer(bytes));
        return parseScript(scripts);
    }

    public static PubKeyScript parseScript(List<Script> scripts) {
        Type type = null;
        for (Type value : Type.values()) {
            if (value.match(scripts)) {
                type = value;
                break;
            }
        }
        Assert.isTrue(type != null, "NOT NULL, Type.UNKNOWN will match all");
        return new PubKeyScript(type, scripts);
    }

    public Optional<CoinAccount.Address> getAddress() {
        try {
            return Optional.of(type.parseAddress(scripts));
        } catch (UnsupportedOperationException ex) {
            return Optional.empty();
        }
    }

    public Optional<String> getAddress(RawTransaction tx, Env env) {
        return this.getAddress().map(addr -> {
            if (PubKeyScript.Type.P2SH.equals(this.getType()) || PubKeyScript.Type.P2WSH.equals(this.getType())) {
                return addr.toP2SHAddress(env);
            } else if (tx.isWitness()) {
                return addr.toBech32Address(env);
            }
            return addr.toBase58Address(env);
        });
    }

    public int getRequestSigns() {
        return type.getRequestSigns(scripts);
    }

    public Type getType() {
        return type;
    }

    public List<Script> getScripts() {
        return Collections.unmodifiableList(scripts);
    }

    static class Mather {
        List<Predicate<Script>> asc = new ArrayList<>();
        List<Predicate<Script>> desc = new ArrayList<>();

        public Mather asc(Predicate<Script> predicate) {
            asc.add(predicate);
            return this;
        }

        public Mather asc(ScriptNames script) {
            asc.add(sr -> {
                return sr.opcode() == script.ordinal();
            });
            return this;
        }

        public Mather desc(ScriptNames script) {
            desc.add(sr -> {
                return sr.opcode() == script.ordinal();
            });
            return this;
        }

        public Mather desc(Predicate<Script> predicate) {
            desc.add(predicate);
            return this;
        }

        public boolean test(List<Script> scripts) {
            int size = scripts.size();
            if (size < asc.size() || size < desc.size()) {
                return false;
            }
            for (int i = 0; i < asc.size(); i++) {
                if (!asc.get(i).test(scripts.get(i))) {
                    return false;
                }
            }
            for (int i = 0; i < desc.size(); i++) {
                if (!desc.get(i).test(scripts.get(size - 1 - i))) {
                    return false;
                }
            }
            return true;
        }
    }
}
