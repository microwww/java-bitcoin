package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.math.UintVar;
import com.github.microwww.bitcoin.script.ex.ScriptDisableException;
import com.github.microwww.bitcoin.script.ex.TransactionInvalidException;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.wallet.Account4bitcoin;

import java.util.Arrays;

public enum ScriptNames {
    // push value
    OP_0,
    _1() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 1));
        }
    },
    _2() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 2));
        }
    },
    _3() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 3));
        }
    },
    _4() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 4));
        }
    },
    _5() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 5));
        }
    },
    _6() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 6));
        }
    },
    _7() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 7));
        }
    },
    _8() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 8));
        }
    },
    _9() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 9));
        }
    },
    _10() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 10));
        }
    },
    _11() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 11));
        }
    },
    _12() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 12));
        }
    },
    _13() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 13));
        }
    },
    _14() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 14));
        }
    },
    _15() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 15));
        }
    },
    _16() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 16));
        }
    },
    _17() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 17));
        }
    },
    _18() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 18));
        }
    },
    _19() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 19));
        }
    },
    _20() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 20));
        }
    },
    _21() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 21));
        }
    },
    _22() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 22));
        }
    },
    _23() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 23));
        }
    },
    _24() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 24));
        }
    },
    _25() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 25));
        }
    },
    _26() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 26));
        }
    },
    _27() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 27));
        }
    },
    _28() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 28));
        }
    },
    _29() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 29));
        }
    },
    _30() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 30));
        }
    },
    _31() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 31));
        }
    },
    _32() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 32));
        }
    },
    _33() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 33));
        }
    },
    _34() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 34));
        }
    },
    _35() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 35));
        }
    },
    _36() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 36));
        }
    },
    _37() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 37));
        }
    },
    _38() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 38));
        }
    },
    _39() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 39));
        }
    },
    _40() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 40));
        }
    },
    _41() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 41));
        }
    },
    _42() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 42));
        }
    },
    _43() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 43));
        }
    },
    _44() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 44));
        }
    },
    _45() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 45));
        }
    },
    _46() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 46));
        }
    },
    _47() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 47));
        }
    },
    _48() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 48));
        }
    },
    _49() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 49));
        }
    },
    _50() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 50));
        }
    },
    _51() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 51));
        }
    },
    _52() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 52));
        }
    },
    _53() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 53));
        }
    },
    _54() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 54));
        }
    },
    _55() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 55));
        }
    },
    _56() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 56));
        }
    },
    _57() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 57));
        }
    },
    _58() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 58));
        }
    },
    _59() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 59));
        }
    },
    _60() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 60));
        }
    },
    _61() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 61));
        }
    },
    _62() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 62));
        }
    },
    _63() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 63));
        }
    },
    _64() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 64));
        }
    },
    _65() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 65));
        }
    },
    _66() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 66));
        }
    },
    _67() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 67));
        }
    },
    _68() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 68));
        }
    },
    _69() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 69));
        }
    },
    _70() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 70));
        }
    },
    _71() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 71));
        }
    },
    _72() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 72));
        }
    },
    _73() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 73));
        }
    },
    _74() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 74));
        }
    },
    _75() {
        @Override
        public void opt(Interpreter executor) {
            executor.stack.push(ByteUtil.readLength(executor.script, 75));
        }
    },
    OP_PUSHDATA1,// 76
    OP_PUSHDATA2,
    OP_PUSHDATA4,
    OP_1NEGATE,
    OP_RESERVED,// 80
    OP_1,// OP_TRUE 81
    OP_2,
    OP_3,
    OP_4,
    OP_5,
    OP_6,
    OP_7,
    OP_8,
    OP_9,
    OP_10, // 90
    OP_11,
    OP_12,
    OP_13,
    OP_14,
    OP_15,
    OP_16, // 96

    // control
    OP_NOP,// 97
    OP_VER,
    OP_IF,// 99
    OP_NOTIF, // 100
    OP_VERIF,
    OP_VERNOTIF,
    OP_ELSE,
    OP_ENDIF,
    OP_VERIFY() {
        @Override
        public void opt(Interpreter executor) {
            int v = executor.stack.popInt();
            if (v == 0) {
                throw new TransactionInvalidException("OP_VERIFY not equal");
            }
        }
    },
    OP_RETURN, // 106

    // stack ops
    OP_TOALTSTACK, // 107
    OP_FROMALTSTACK,
    OP_2DROP, // 109
    OP_2DUP, // 110
    OP_3DUP,
    OP_2OVER,
    OP_2ROT,
    OP_2SWAP,
    OP_IFDUP, // 115
    OP_DEPTH,
    OP_DROP,
    OP_DUP {
        @Override
        public void opt(Interpreter executor) {
            byte[] peek = executor.stack.assertNotEmpty().peek();
            executor.stack.push(peek);
        }
    },
    OP_NIP,
    OP_OVER, // 120
    OP_PICK,
    OP_ROLL,
    OP_ROT,
    OP_SWAP,
    OP_TUCK, // 125

    // splice ops
    OP_CAT, // 126
    OP_SUBSTR,
    OP_LEFT,
    OP_RIGHT,
    OP_SIZE,// 130

    // bit logic
    OP_INVERT, // 131
    OP_AND,
    OP_OR,
    OP_XOR,
    OP_EQUAL,
    OP_EQUALVERIFY() {
        @Override
        public void opt(Interpreter executor) {
            byte[] x1 = executor.stack.pop();
            byte[] x2 = executor.stack.pop();
            executor.stack.push(Arrays.equals(x1, x2) ? 1 : 0);
            OP_VERIFY.opt(executor);
        }
    }, // 136
    OP_RESERVED1,
    OP_RESERVED2,

    // numeric
    OP_1ADD { // 139

        @Override
        public void opt(Interpreter executor) {
            int v = executor.stack.assertNotEmpty().popInt() + 1;
            executor.stack.push(v);
        }
    },
    OP_1SUB { // 140

        @Override
        public void opt(Interpreter executor) {
            int v = executor.stack.assertNotEmpty().popInt() - 1;
            executor.stack.push(v);
        }
    },
    OP_2MUL {
        @Override
        public void opt(Interpreter executor) {
            throw new ScriptDisableException(this.name());
        }
    },
    OP_2DIV {
        @Override
        public void opt(Interpreter executor) {
            throw new ScriptDisableException(this.name());
        }
    },
    OP_NEGATE {
        @Override
        public void opt(Interpreter executor) {
            int i = executor.stack.assertNotEmpty().popInt();
            if (i > 0) {
                executor.stack.push(-i);
            } else {
                executor.stack.push(i);
            }
        }
    },
    OP_ABS {
        @Override
        public void opt(Interpreter executor) {
            int i = executor.stack.assertNotEmpty().popInt();
            if (i >= 0) {
                executor.stack.push(i);
            } else {
                executor.stack.push(-i);
            }
        }
    },
    OP_NOT { // 145

        @Override
        public void opt(Interpreter executor) {
            int i = executor.stack.assertNotEmpty().popInt();
            if (i == 0) {
                executor.stack.push(1);
            } else {
                executor.stack.push(0);
            }
        }
    },
    OP_0NOTEQUAL {
        @Override
        public void opt(Interpreter executor) {
            int i = executor.stack.assertNotEmpty().popInt();
            if (i != 0) {
                executor.stack.push(1);
            } else {
                executor.stack.push(0);
            }
        }
    },
    OP_ADD {
        @Override
        public void opt(Interpreter executor) {
            int a = executor.stack.assertSizeGE(2).popInt();
            int b = executor.stack.popInt();
            executor.stack.push(a + b);
        }
    },
    OP_SUB {
        @Override
        public void opt(Interpreter executor) {
            int a = executor.stack.assertSizeGE(2).popInt();
            int b = executor.stack.popInt();
            executor.stack.push(a - b);
        }
    },
    OP_MUL {
        @Override
        public void opt(Interpreter executor) {
            throw new ScriptDisableException(this.name());
        }
    },
    OP_DIV { // 150

        @Override
        public void opt(Interpreter executor) {
            throw new ScriptDisableException(this.name());
        }
    },
    OP_MOD {
        @Override
        public void opt(Interpreter executor) {
            throw new ScriptDisableException(this.name());
        }
    },
    OP_LSHIFT {
        @Override
        public void opt(Interpreter executor) {
            throw new ScriptDisableException(this.name());
        }
    },
    OP_RSHIFT {
        @Override
        public void opt(Interpreter executor) {
            throw new ScriptDisableException(this.name());
        }
    },
    OP_BOOLAND {
        @Override
        public void opt(Interpreter executor) {
            int a = executor.stack.assertSizeGE(2).popInt();
            int b = executor.stack.popInt();
            if (a != 0 && b != 0) {
                executor.stack.push(1);
            } else {
                executor.stack.push(0);
            }
        }
    },
    OP_BOOLOR { // 155

        @Override
        public void opt(Interpreter executor) {
            int a = executor.stack.assertSizeGE(2).popInt();
            int b = executor.stack.popInt();
            if (a != 0 || b != 0) {
                executor.stack.push(1);
            } else {
                executor.stack.push(0);
            }
        }
    },
    OP_NUMEQUAL {
        @Override
        public void opt(Interpreter executor) {
            int a = executor.stack.assertSizeGE(2).popInt();
            int b = executor.stack.popInt();
            if (a == b) {
                executor.stack.push(1);
            } else {
                executor.stack.push(0);
            }
        }
    },
    OP_NUMEQUALVERIFY,
    OP_NUMNOTEQUAL,
    OP_LESSTHAN,
    OP_GREATERTHAN, // 160
    OP_LESSTHANOREQUAL,
    OP_GREATERTHANOREQUAL,
    OP_MIN,
    OP_MAX,
    OP_WITHIN, // 165

    // crypto
    OP_RIPEMD160 { // 166

        @Override
        public void opt(Interpreter executor) {
            byte[] pop = executor.stack.assertNotEmpty().pop();
            byte[] bytes = Account4bitcoin.ripemd160(pop);
        }
    },
    OP_SHA1,
    OP_SHA256 {
        @Override
        public void opt(Interpreter executor) {
            byte[] pop = executor.stack.assertNotEmpty().pop();
            byte[] bytes = Account4bitcoin.sha256(pop);
            executor.stack.push(bytes);
        }
    },
    OP_HASH160 {
        @Override
        public void opt(Interpreter executor) {
            byte[] pop = executor.stack.assertNotEmpty().pop();
            byte[] bytes = Account4bitcoin.sha256hash160(pop);
            executor.stack.push(bytes);
            bytes = UintVar.parseAndRead(executor.script);
            executor.stack.push(bytes);
        }
    },
    OP_HASH256 { // 170

        @Override
        public void opt(Interpreter executor) {
            byte[] pop = executor.stack.assertNotEmpty().pop();
            byte[] bytes = ByteUtil.sha256sha256(pop);
            executor.stack.push(bytes);
        }
    },
    OP_CODESEPARATOR,
    OP_CHECKSIG() {

    },
    OP_CHECKSIGVERIFY,
    OP_CHECKMULTISIG,
    OP_CHECKMULTISIGVERIFY, // 175

    // expansion
    OP_NOP1,
    OP_CHECKLOCKTIMEVERIFY,
    OP_CHECKSEQUENCEVERIFY,
    OP_NOP4,
    OP_NOP5, // 180
    OP_NOP6,
    OP_NOP7,
    OP_NOP8,
    OP_NOP9,
    OP_NOP10, // 185
    _186, _187, _188, _189, _190, _191, _192, _193, _194, _195, _196, _197, _198, _199, _200, _201, _202, _203, _204, _205, _206, _207, _208, _209, _210, _211, _212, _213, _214, _215, _216, _217, _218, _219, _220, _221, _222, _223, _224, _225, _226, _227, _228, _229, _230, _231, _232, _233, _234, _235, _236, _237, _238, _239, _240, _241, _242, _243, _244, _245, _246, _247, _248, _249, _250, _251,
    _252,
    // Opcode added by BIP 342 (Tapscript)
    // OP_CHECKSIGADD,
    // Pseudo-words
    OP_PUBKEYHASH,    // 253
    OP_PUBKEY,        // 254
    OP_INVALIDOPCODE, // 255
    // OP_UNKNOWN,
    ;

    public void opt(Interpreter executor) {
        throw new UnsupportedOperationException();
    }
}
