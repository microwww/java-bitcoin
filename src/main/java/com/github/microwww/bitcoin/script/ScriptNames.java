package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.script.ex.ScriptDisableException;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.wallet.cash.account.BitcoinCashAccount;

public enum ScriptNames {
    // push value
    OP_0,
    _1() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(1);
        }
    },
    _2() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(2);
        }
    },
    _3() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(3);
        }
    },
    _4() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(4);
        }
    },
    _5() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(5);
        }
    },
    _6() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(6);
        }
    },
    _7() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(7);
        }
    },
    _8() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(8);
        }
    },
    _9() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(9);
        }
    },
    _10() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(10);
        }
    },
    _11() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(11);
        }
    },
    _12() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(12);
        }
    },
    _13() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(13);
        }
    },
    _14() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(14);
        }
    },
    _15() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(15);
        }
    },
    _16() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(16);
        }
    },
    _17() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(17);
        }
    },
    _18() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(18);
        }
    },
    _19() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(19);
        }
    },
    _20() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(20);
        }
    },
    _21() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(21);
        }
    },
    _22() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(22);
        }
    },
    _23() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(23);
        }
    },
    _24() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(24);
        }
    },
    _25() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(25);
        }
    },
    _26() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(26);
        }
    },
    _27() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(27);
        }
    },
    _28() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(28);
        }
    },
    _29() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(29);
        }
    },
    _30() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(30);
        }
    },
    _31() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(31);
        }
    },
    _32() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(32);
        }
    },
    _33() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(33);
        }
    },
    _34() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(34);
        }
    },
    _35() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(35);
        }
    },
    _36() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(36);
        }
    },
    _37() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(37);
        }
    },
    _38() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(38);
        }
    },
    _39() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(39);
        }
    },
    _40() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(40);
        }
    },
    _41() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(41);
        }
    },
    _42() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(42);
        }
    },
    _43() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(43);
        }
    },
    _44() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(44);
        }
    },
    _45() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(45);
        }
    },
    _46() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(46);
        }
    },
    _47() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(47);
        }
    },
    _48() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(48);
        }
    },
    _49() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(49);
        }
    },
    _50() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(50);
        }
    },
    _51() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(51);
        }
    },
    _52() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(52);
        }
    },
    _53() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(53);
        }
    },
    _54() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(54);
        }
    },
    _55() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(55);
        }
    },
    _56() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(56);
        }
    },
    _57() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(57);
        }
    },
    _58() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(58);
        }
    },
    _59() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(59);
        }
    },
    _60() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(60);
        }
    },
    _61() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(61);
        }
    },
    _62() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(62);
        }
    },
    _63() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(63);
        }
    },
    _64() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(64);
        }
    },
    _65() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(65);
        }
    },
    _66() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(66);
        }
    },
    _67() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(67);
        }
    },
    _68() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(68);
        }
    },
    _69() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(69);
        }
    },
    _70() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(70);
        }
    },
    _71() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(71);
        }
    },
    _72() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(72);
        }
    },
    _73() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(73);
        }
    },
    _74() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(74);
        }
    },
    _75() {
        @Override
        public void opt(BytesStack stack) {
            stack.push(75);
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
    OP_10,
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
    OP_NOTIF,
    OP_VERIF,
    OP_VERNOTIF,
    OP_ELSE,
    OP_ENDIF,
    OP_VERIFY,
    OP_RETURN, // 106

    // stack ops
    OP_TOALTSTACK, // 107
    OP_FROMALTSTACK,
    OP_2DROP, // 109
    OP_2DUP,
    OP_3DUP,
    OP_2OVER,
    OP_2ROT,
    OP_2SWAP,
    OP_IFDUP, // 115
    OP_DEPTH,
    OP_DROP,
    OP_DUP {
        @Override
        public void opt(BytesStack stack) {
            byte[] peek = stack.assertNotEmpty().peek();
            stack.push(peek);
        }
    },
    OP_NIP,
    OP_OVER,
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
    OP_EQUALVERIFY, // 136
    OP_RESERVED1,
    OP_RESERVED2,

    // numeric
    OP_1ADD { // 139

        @Override
        public void opt(BytesStack stack) {
            int v = stack.assertNotEmpty().popInt() + 1;
            stack.push(v);
        }
    },
    OP_1SUB {
        @Override
        public void opt(BytesStack stack) {
            int v = stack.assertNotEmpty().popInt() - 1;
            stack.push(v);
        }
    },
    OP_2MUL {
        @Override
        public void opt(BytesStack stack) {
            throw new ScriptDisableException(this.name());
        }
    },
    OP_2DIV {
        @Override
        public void opt(BytesStack stack) {
            throw new ScriptDisableException(this.name());
        }
    },
    OP_NEGATE {
        @Override
        public void opt(BytesStack stack) {
            int i = stack.assertNotEmpty().popInt();
            if (i > 0) {
                stack.push(-i);
            } else {
                stack.push(i);
            }
        }
    },
    OP_ABS {
        @Override
        public void opt(BytesStack stack) {
            int i = stack.assertNotEmpty().popInt();
            if (i >= 0) {
                stack.push(i);
            } else {
                stack.push(-i);
            }
        }
    },
    OP_NOT {
        @Override
        public void opt(BytesStack stack) {
            int i = stack.assertNotEmpty().popInt();
            if (i == 0) {
                stack.push(1);
            } else {
                stack.push(0);
            }
        }
    },
    OP_0NOTEQUAL {
        @Override
        public void opt(BytesStack stack) {
            int i = stack.assertNotEmpty().popInt();
            if (i != 0) {
                stack.push(1);
            } else {
                stack.push(0);
            }
        }
    },
    OP_ADD {
        @Override
        public void opt(BytesStack stack) {
            int a = stack.assertSizeGE(2).popInt();
            int b = stack.popInt();
            stack.push(a + b);
        }
    },
    OP_SUB {
        @Override
        public void opt(BytesStack stack) {
            int a = stack.assertSizeGE(2).popInt();
            int b = stack.popInt();
            stack.push(a - b);
        }
    },
    OP_MUL {
        @Override
        public void opt(BytesStack stack) {
            throw new ScriptDisableException(this.name());
        }
    },
    OP_DIV {
        @Override
        public void opt(BytesStack stack) {
            throw new ScriptDisableException(this.name());
        }
    },
    OP_MOD {
        @Override
        public void opt(BytesStack stack) {
            throw new ScriptDisableException(this.name());
        }
    },
    OP_LSHIFT {
        @Override
        public void opt(BytesStack stack) {
            throw new ScriptDisableException(this.name());
        }
    },
    OP_RSHIFT {
        @Override
        public void opt(BytesStack stack) {
            throw new ScriptDisableException(this.name());
        }
    },
    OP_BOOLAND {
        @Override
        public void opt(BytesStack stack) {
            int a = stack.assertSizeGE(2).popInt();
            int b = stack.popInt();
            if (a != 0 && b != 0) {
                stack.push(1);
            } else {
                stack.push(0);
            }
        }
    },
    OP_BOOLOR { // 155

        @Override
        public void opt(BytesStack stack) {
            int a = stack.assertSizeGE(2).popInt();
            int b = stack.popInt();
            if (a != 0 || b != 0) {
                stack.push(1);
            } else {
                stack.push(0);
            }
        }
    },
    OP_NUMEQUAL {
        @Override
        public void opt(BytesStack stack) {
            int a = stack.assertSizeGE(2).popInt();
            int b = stack.popInt();
            if (a == b) {
                stack.push(1);
            } else {
                stack.push(0);
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
        public void opt(BytesStack stack) {
            byte[] pop = stack.assertNotEmpty().pop();
            byte[] bytes = BitcoinCashAccount.ripemd160hash(pop);
            stack.push(bytes);
        }
    },
    OP_SHA1,
    OP_SHA256 {
        @Override
        public void opt(BytesStack stack) {
            byte[] pop = stack.assertNotEmpty().pop();
            byte[] bytes = BitcoinCashAccount.hash(pop, 0, pop.length);
            stack.push(bytes);
        }
    },
    OP_HASH160 {
        @Override
        public void opt(BytesStack stack) {
            byte[] pop = stack.assertNotEmpty().pop();
            byte[] bytes = BitcoinCashAccount.sha256hash160(pop);
            stack.push(bytes);
        }
    },
    OP_HASH256 {
        @Override
        public void opt(BytesStack stack) {
            byte[] pop = stack.assertNotEmpty().pop();
            byte[] bytes = ByteUtil.sha256sha256(pop);
            stack.push(bytes);
        }
    },
    OP_CODESEPARATOR,
    OP_CHECKSIG,
    OP_CHECKSIGVERIFY,
    OP_CHECKMULTISIG,
    OP_CHECKMULTISIGVERIFY, // 175

    // expansion
    OP_NOP1,
    OP_CHECKLOCKTIMEVERIFY,
    OP_CHECKSEQUENCEVERIFY,
    OP_NOP4,
    OP_NOP5,
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

    public void opt(BytesStack stack) {
        throw new UnsupportedOperationException();
    }
}
