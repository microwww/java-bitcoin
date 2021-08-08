package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.wallet.CoinAccount;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import static com.github.microwww.bitcoin.script.ScriptNames.*;

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
    MN,
    P2SH {
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
            return TemplateTransaction.p2wSupport(data, 0x14);
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
        public void executor(Interpreter interpreter) { // TODO:: 重写
            byte[] pop = interpreter.stack.peek(2);
            byte[] bytes = CoinAccount.sha256ripemd160(pop);
            interpreter.stack.push(bytes);
            ScriptNames.OP_EQUALVERIFY.opt(interpreter);
            ScriptNames.OP_DROP.opt(interpreter);
            ScriptNames.OP_CHECKSIG.opt(interpreter);
        }
    },
    P2WSH() {
        @Override
        public boolean isSupport(byte[] data) {// [0x00][0x20] [SHA256(witnessScript)]
            return TemplateTransaction.p2wSupport(data, 0x20);
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
    },
    OP_RETURN {
        @Override
        public byte[] scriptPubKey(byte[]... args) {
            Assert.isTrue(args.length > 0, "one arg for address");
            ByteBuf bf = Unpooled.buffer()
                    .writeByte(ScriptNames.OP_RETURN.opcode())
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

    private static boolean p2wSupport(byte[] data, int len) {
        //[0x00][0x14] [Hash160(PK)] 0x00: 版本号
        int length = len + 2;
        if (data.length >= length) {
            if (0x00 == data[0]) {
                if (len == data[1]) {
                    if (data.length == length) {
                        return true;
                    } else if (data.length > length) {
                        if (Byte.toUnsignedInt(data[length]) == ScriptNames.OP_CODESEPARATOR.ordinal()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void executor(Interpreter interpreter) {
        log.debug("Ignore : {}", this.name());
    }
}
