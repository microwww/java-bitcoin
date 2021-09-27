package com.github.microwww.bitcoin.script.instruction;

import com.github.microwww.bitcoin.script.Interpreter;
import io.netty.buffer.ByteBuf;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public enum ScriptNames {
    // 000, 0x00
    OP_0(Constants.PushValue.class),
    // 001, 0x01
    _1(Constants.PushValue.class),
    // 002, 0x02
    _2(Constants.PushValue.class),
    // 003, 0x03
    _3(Constants.PushValue.class),
    // 004, 0x04
    _4(Constants.PushValue.class),
    // 005, 0x05
    _5(Constants.PushValue.class),
    // 006, 0x06
    _6(Constants.PushValue.class),
    // 007, 0x07
    _7(Constants.PushValue.class),
    // 008, 0x08
    _8(Constants.PushValue.class),
    // 009, 0x09
    _9(Constants.PushValue.class),
    // 010, 0x0A
    _10(Constants.PushValue.class),
    // 011, 0x0B
    _11(Constants.PushValue.class),
    // 012, 0x0C
    _12(Constants.PushValue.class),
    // 013, 0x0D
    _13(Constants.PushValue.class),
    // 014, 0x0E
    _14(Constants.PushValue.class),
    // 015, 0x0F
    _15(Constants.PushValue.class),
    // 016, 0x10
    _16(Constants.PushValue.class),
    // 017, 0x11
    _17(Constants.PushValue.class),
    // 018, 0x12
    _18(Constants.PushValue.class),
    // 019, 0x13
    _19(Constants.PushValue.class),
    // 020, 0x14
    _20(Constants.PushValue.class),
    // 021, 0x15
    _21(Constants.PushValue.class),
    // 022, 0x16
    _22(Constants.PushValue.class),
    // 023, 0x17
    _23(Constants.PushValue.class),
    // 024, 0x18
    _24(Constants.PushValue.class),
    // 025, 0x19
    _25(Constants.PushValue.class),
    // 026, 0x1A
    _26(Constants.PushValue.class),
    // 027, 0x1B
    _27(Constants.PushValue.class),
    // 028, 0x1C
    _28(Constants.PushValue.class),
    // 029, 0x1D
    _29(Constants.PushValue.class),
    // 030, 0x1E
    _30(Constants.PushValue.class),
    // 031, 0x1F
    _31(Constants.PushValue.class),
    // 032, 0x20
    _32(Constants.PushValue.class),
    // 033, 0x21
    _33(Constants.PushValue.class),
    // 034, 0x22
    _34(Constants.PushValue.class),
    // 035, 0x23
    _35(Constants.PushValue.class),
    // 036, 0x24
    _36(Constants.PushValue.class),
    // 037, 0x25
    _37(Constants.PushValue.class),
    // 038, 0x26
    _38(Constants.PushValue.class),
    // 039, 0x27
    _39(Constants.PushValue.class),
    // 040, 0x28
    _40(Constants.PushValue.class),
    // 041, 0x29
    _41(Constants.PushValue.class),
    // 042, 0x2A
    _42(Constants.PushValue.class),
    // 043, 0x2B
    _43(Constants.PushValue.class),
    // 044, 0x2C
    _44(Constants.PushValue.class),
    // 045, 0x2D
    _45(Constants.PushValue.class),
    // 046, 0x2E
    _46(Constants.PushValue.class),
    // 047, 0x2F
    _47(Constants.PushValue.class),
    // 048, 0x30
    _48(Constants.PushValue.class),
    // 049, 0x31
    _49(Constants.PushValue.class),
    // 050, 0x32
    _50(Constants.PushValue.class),
    // 051, 0x33
    _51(Constants.PushValue.class),
    // 052, 0x34
    _52(Constants.PushValue.class),
    // 053, 0x35
    _53(Constants.PushValue.class),
    // 054, 0x36
    _54(Constants.PushValue.class),
    // 055, 0x37
    _55(Constants.PushValue.class),
    // 056, 0x38
    _56(Constants.PushValue.class),
    // 057, 0x39
    _57(Constants.PushValue.class),
    // 058, 0x3A
    _58(Constants.PushValue.class),
    // 059, 0x3B
    _59(Constants.PushValue.class),
    // 060, 0x3C
    _60(Constants.PushValue.class),
    // 061, 0x3D
    _61(Constants.PushValue.class),
    // 062, 0x3E
    _62(Constants.PushValue.class),
    // 063, 0x3F
    _63(Constants.PushValue.class),
    // 064, 0x40
    _64(Constants.PushValue.class),
    // 065, 0x41
    _65(Constants.PushValue.class),
    // 066, 0x42
    _66(Constants.PushValue.class),
    // 067, 0x43
    _67(Constants.PushValue.class),
    // 068, 0x44
    _68(Constants.PushValue.class),
    // 069, 0x45
    _69(Constants.PushValue.class),
    // 070, 0x46
    _70(Constants.PushValue.class),
    // 071, 0x47
    _71(Constants.PushValue.class),
    // 072, 0x48
    _72(Constants.PushValue.class),
    // 073, 0x49
    _73(Constants.PushValue.class),
    // 074, 0x4A
    _74(Constants.PushValue.class),
    // 075, 0x4B
    _75(Constants.PushValue.class),
    // 076, 0x4C
    OP_PUSHDATA1(Constants.OP_PUSHDATA1.class),
    // 077, 0x4D
    OP_PUSHDATA2(Constants.OP_PUSHDATA2.class),
    // 078, 0x4E
    OP_PUSHDATA4(Constants.OP_PUSHDATA4.class),
    // 079, 0x4F
    OP_1NEGATE,
    // 080, 0x50
    OP_RESERVED,
    // 081, 0x51
    OP_1(Constants.PushCode.class),// OP_TRUE 81
    // 082, 0x52
    OP_2(Constants.PushCode.class),
    // 083, 0x53
    OP_3(Constants.PushCode.class),
    // 084, 0x54
    OP_4(Constants.PushCode.class),
    // 085, 0x55
    OP_5(Constants.PushCode.class),
    // 086, 0x56
    OP_6(Constants.PushCode.class),
    // 087, 0x57
    OP_7(Constants.PushCode.class),
    // 088, 0x58
    OP_8(Constants.PushCode.class),
    // 089, 0x59
    OP_9(Constants.PushCode.class),
    // 090, 0x5A
    OP_10(Constants.PushCode.class),
    // 091, 0x5B
    OP_11(Constants.PushCode.class),
    // 092, 0x5C
    OP_12(Constants.PushCode.class),
    // 093, 0x5D
    OP_13(Constants.PushCode.class),
    // 094, 0x5E
    OP_14(Constants.PushCode.class),
    // 095, 0x5F
    OP_15(Constants.PushCode.class),
    // 096, 0x60
    OP_16(Constants.PushCode.class),

    // control
    // 097, 0x61
    OP_NOP(FlowControl.OP_NOP.class),
    // 098, 0x62
    OP_VER(),
    // 099, 0x63
    OP_IF(),
    // 100, 0x64
    OP_NOTIF(),
    // 101, 0x65
    OP_VERIF(),
    // 102, 0x66
    OP_VERNOTIF(),
    // 103, 0x67
    OP_ELSE(),
    // 104, 0x68
    OP_ENDIF(),
    // 105, 0x69
    OP_VERIFY(FlowControl.OP_VERIFY.class),
    // 106, 0x6A
    OP_RETURN(),

    // stack ops
    // 107, 0x6B
    OP_TOALTSTACK(),
    // 108, 0x6C
    OP_FROMALTSTACK(),
    // 109, 0x6D
    OP_2DROP(),
    // 110, 0x6E
    OP_2DUP(),
    // 111, 0x6F
    OP_3DUP(),
    // 112, 0x70
    OP_2OVER(),
    // 113, 0x71
    OP_2ROT(),
    // 114, 0x72
    OP_2SWAP(),
    // 115, 0x73
    OP_IFDUP(),
    // 116, 0x74
    OP_DEPTH(),
    // 117, 0x75
    OP_DROP(StackOps.OP_DROP.class),
    // 118, 0x76
    OP_DUP(StackOps.OP_DUP.class),
    // 119, 0x77
    OP_NIP(),
    // 120, 0x78
    OP_OVER(),
    // 121, 0x79
    OP_PICK(StackOps.OP_PICK.class),
    // 122, 0x7A
    OP_ROLL(),
    // 123, 0x7B
    OP_ROT(),
    // 124, 0x7C
    OP_SWAP(),
    // 125, 0x7D
    OP_TUCK(),

    // splice ops
    // 126, 0x7E
    OP_CAT(),
    // 127, 0x7F
    OP_SUBSTR(),
    // 128, 0x80
    OP_LEFT(),
    // 129, 0x81
    OP_RIGHT(),
    // 130, 0x82
    OP_SIZE(),

    // bit logic
    // 131, 0x83
    OP_INVERT(),
    // 132, 0x84
    OP_AND(),
    // 133, 0x85
    OP_OR(),
    // 134, 0x86
    OP_XOR(),
    // 135, 0x87
    OP_EQUAL(BitLogic.OP_EQUAL.class),
    // 136, 0x88
    OP_EQUALVERIFY(BitLogic.OP_EQUALVERIFY.class),
    // 137, 0x89
    OP_RESERVED1(),
    // 138, 0x8A
    OP_RESERVED2(),
    // 139, 0x8B
    OP_1ADD(Numeric.OP_1ADD.class),
    // 140, 0x8C
    OP_1SUB(Numeric.OP_1SUB.class),
    // 141, 0x8D
    OP_2MUL(Numeric.OP_2MUL.class),
    // 142, 0x8E
    OP_2DIV(Numeric.OP_2DIV.class),
    // 143, 0x8F
    OP_NEGATE(Numeric.OP_NEGATE.class),
    // 144, 0x90
    OP_ABS(Numeric.OP_ABS.class),
    // 145, 0x91
    OP_NOT(Numeric.OP_NOT.class),
    // 146, 0x92
    OP_0NOTEQUAL(Numeric.OP_0NOTEQUAL.class),
    // 147, 0x93
    OP_ADD(Numeric.OP_ADD.class),
    // 148, 0x94
    OP_SUB(Numeric.OP_SUB.class),
    // 149, 0x95
    OP_MUL(Numeric.OP_MUL.class),
    // 150, 0x96
    OP_DIV(Numeric.OP_DIV.class),
    // 151, 0x97
    OP_MOD(Numeric.OP_MOD.class),
    // 152, 0x98
    OP_LSHIFT(Numeric.OP_LSHIFT.class),
    // 153, 0x99
    OP_RSHIFT(Numeric.OP_RSHIFT.class),
    // 154, 0x9A
    OP_BOOLAND(Numeric.OP_BOOLAND.class),
    // 155, 0x9B
    OP_BOOLOR(Numeric.OP_BOOLOR.class),
    // 156, 0x9C
    OP_NUMEQUAL(Numeric.OP_NUMEQUAL.class),
    // 157, 0x9D
    OP_NUMEQUALVERIFY(),
    // 158, 0x9E
    OP_NUMNOTEQUAL(),
    // 159, 0x9F
    OP_LESSTHAN(),
    // 160, 0xA0
    OP_GREATERTHAN(),
    // 161, 0xA1
    OP_LESSTHANOREQUAL(),
    // 162, 0xA2
    OP_GREATERTHANOREQUAL(),
    // 163, 0xA3
    OP_MIN(Numeric.OP_MIN.class),
    // 164, 0xA4
    OP_MAX(Numeric.OP_MAX.class),
    // 165, 0xA5
    OP_WITHIN(Numeric.OP_WITHIN.class),
    // 166, 0xA6
    OP_RIPEMD160(Crypto.OP_RIPEMD160.class),
    // 167, 0xA7
    OP_SHA1(),
    // 168, 0xA8
    OP_SHA256(Crypto.OP_SHA256.class),
    // 169, 0xA9
    OP_HASH160(Crypto.OP_HASH160.class),
    // 170, 0xAA
    OP_HASH256(Crypto.OP_HASH256.class),
    // 171, 0xAB
    OP_CODESEPARATOR(Crypto.OP_CODESEPARATOR.class),
    // 172, 0xAC
    OP_CHECKSIG(Crypto.OP_CHECKSIG.class),
    // 173, 0xAD
    OP_CHECKSIGVERIFY(Crypto.OP_CHECKSIGVERIFY.class),
    // 174, 0xAE
    OP_CHECKMULTISIG(Crypto.OP_CHECKMULTISIG.class),
    // 175, 0xAF
    OP_CHECKMULTISIGVERIFY(),

    // expansion
    // 176, 0xB0
    OP_NOP1(),
    // 177, 0xB1
    OP_CHECKLOCKTIMEVERIFY(Expansion.OP_CHECKLOCKTIMEVERIFY.class),
    // 178, 0xB2
    OP_CHECKSEQUENCEVERIFY(),
    // 179, 0xB3
    OP_NOP4(),
    // 180, 0xB4
    OP_NOP5(),
    // 181, 0xB5
    OP_NOP6(),
    // 182, 0xB6
    OP_NOP7(),
    // 183, 0xB7
    OP_NOP8(),
    // 184, 0xB8
    OP_NOP9(),
    // 185, 0xB9
    OP_NOP10(),
    // 186, 0xBA
    _186, _187, _188, _189, _190, _191, _192, _193, _194, _195, _196, _197, _198, _199, _200, _201, _202, _203, _204, _205, _206, _207, _208, _209, _210, _211, _212, _213, _214, _215, _216, _217, _218, _219, _220, _221, _222, _223, _224, _225, _226, _227, _228, _229, _230, _231, _232, _233, _234, _235, _236, _237, _238, _239, _240, _241, _242, _243, _244, _245, _246, _247, _248, _249, _250, _251,
    _252,
    // Opcode added by BIP 342 (Tapscript)
    // OP_CHECKSIGADD(),
    // Pseudo-words
    // 253, 0xFD
    OP_PUBKEYHASH,
    // 254, 0xFE
    OP_PUBKEY,
    // 255, 0xFF
    OP_INVALIDOPCODE(),
    // OP_UNKNOWN(),
    ;
    private final Constructor<? extends Script> script;

    ScriptNames() {
        this(UnsupportedScript.class);
    }

    ScriptNames(Class<? extends Script> clazz) {
        try {
            this.script = clazz.getConstructor(int.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Script> compile(ByteBuf bf) {
        List<Script> ss = new ArrayList<>();
        if (bf.isReadable()) {
            byte code = bf.readByte();
            Script scr = ScriptNames.values()[code].operand(bf);
            ss.add(scr);
        }
        return ss;
    }

    public Script operand(ByteBuf bf) {
        try {
            Script proxy = script.newInstance(this.ordinal());
            proxy.operand(bf);
            return proxy;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public byte opcode() {
        return (byte) this.ordinal();
    }

    static class UnsupportedScript extends AbstractScriptOperand {

        public UnsupportedScript(int code) {
            super(code);
        }

        @Override
        public void operand(ByteBuf bf) {
            throw new UnsupportedOperationException(ScriptNames.values()[code].name());
        }

        @Override
        public void exec(Interpreter interpreter) {
            throw new UnsupportedOperationException(ScriptNames.values()[code].name());
        }

        @Override
        public int opcode() {
            throw new UnsupportedOperationException(ScriptNames.values()[code].name());
        }
    }
}
