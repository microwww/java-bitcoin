package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.script.ins.InstructionAdaptor;
import io.netty.buffer.ByteBuf;

public enum ScriptNames implements Instruction {
    // 000, 0x00
    OP_0(),
    // 001, 0x01
    _1(),
    // 002, 0x02
    _2(),
    // 003, 0x03
    _3(),
    // 004, 0x04
    _4(),
    // 005, 0x05
    _5(),
    // 006, 0x06
    _6(),
    // 007, 0x07
    _7(),
    // 008, 0x08
    _8(),
    // 009, 0x09
    _9(),
    // 010, 0x0A
    _10(),
    // 011, 0x0B
    _11(),
    // 012, 0x0C
    _12(),
    // 013, 0x0D
    _13(),
    // 014, 0x0E
    _14(),
    // 015, 0x0F
    _15(),
    // 016, 0x10
    _16(),
    // 017, 0x11
    _17(),
    // 018, 0x12
    _18(),
    // 019, 0x13
    _19(),
    // 020, 0x14
    _20(),
    // 021, 0x15
    _21(),
    // 022, 0x16
    _22(),
    // 023, 0x17
    _23(),
    // 024, 0x18
    _24(),
    // 025, 0x19
    _25(),
    // 026, 0x1A
    _26(),
    // 027, 0x1B
    _27(),
    // 028, 0x1C
    _28(),
    // 029, 0x1D
    _29(),
    // 030, 0x1E
    _30(),
    // 031, 0x1F
    _31(),
    // 032, 0x20
    _32(),
    // 033, 0x21
    _33(),
    // 034, 0x22
    _34(),
    // 035, 0x23
    _35(),
    // 036, 0x24
    _36(),
    // 037, 0x25
    _37(),
    // 038, 0x26
    _38(),
    // 039, 0x27
    _39(),
    // 040, 0x28
    _40(),
    // 041, 0x29
    _41(),
    // 042, 0x2A
    _42(),
    // 043, 0x2B
    _43(),
    // 044, 0x2C
    _44(),
    // 045, 0x2D
    _45(),
    // 046, 0x2E
    _46(),
    // 047, 0x2F
    _47(),
    // 048, 0x30
    _48(),
    // 049, 0x31
    _49(),
    // 050, 0x32
    _50(),
    // 051, 0x33
    _51(),
    // 052, 0x34
    _52(),
    // 053, 0x35
    _53(),
    // 054, 0x36
    _54(),
    // 055, 0x37
    _55(),
    // 056, 0x38
    _56(),
    // 057, 0x39
    _57(),
    // 058, 0x3A
    _58(),
    // 059, 0x3B
    _59(),
    // 060, 0x3C
    _60(),
    // 061, 0x3D
    _61(),
    // 062, 0x3E
    _62(),
    // 063, 0x3F
    _63(),
    // 064, 0x40
    _64(),
    // 065, 0x41
    _65(),
    // 066, 0x42
    _66(),
    // 067, 0x43
    _67(),
    // 068, 0x44
    _68(),
    // 069, 0x45
    _69(),
    // 070, 0x46
    _70(),
    // 071, 0x47
    _71(),
    // 072, 0x48
    _72(),
    // 073, 0x49
    _73(),
    // 074, 0x4A
    _74(),
    // 075, 0x4B
    _75(),
    // 076, 0x4C
    OP_PUSHDATA1(),
    // 077, 0x4D
    OP_PUSHDATA2(),
    // 078, 0x4E
    OP_PUSHDATA4(),
    // 079, 0x4F
    OP_1NEGATE(),
    // 080, 0x50
    OP_RESERVED(),
    // 081, 0x51
    OP_1,// OP_TRUE 81
    // 082, 0x52
    OP_2(),
    // 083, 0x53
    OP_3(),
    // 084, 0x54
    OP_4(),
    // 085, 0x55
    OP_5(),
    // 086, 0x56
    OP_6(),
    // 087, 0x57
    OP_7(),
    // 088, 0x58
    OP_8(),
    // 089, 0x59
    OP_9(),
    // 090, 0x5A
    OP_10(),
    // 091, 0x5B
    OP_11(),
    // 092, 0x5C
    OP_12(),
    // 093, 0x5D
    OP_13(),
    // 094, 0x5E
    OP_14(),
    // 095, 0x5F
    OP_15(),
    // 096, 0x60
    OP_16(),

    // control
    // 097, 0x61
    OP_NOP(),
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
    OP_VERIFY(),
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
    OP_DROP(),
    // 118, 0x76
    OP_DUP(),
    // 119, 0x77
    OP_NIP(),
    // 120, 0x78
    OP_OVER(),
    // 121, 0x79
    OP_PICK(),
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
    OP_EQUAL(),
    // 136, 0x88
    OP_EQUALVERIFY(),
    // 137, 0x89
    OP_RESERVED1(),
    // 138, 0x8A
    OP_RESERVED2(),
    // 139, 0x8B
    OP_1ADD(),
    // 140, 0x8C
    OP_1SUB(),
    // 141, 0x8D
    OP_2MUL(),
    // 142, 0x8E
    OP_2DIV(),
    // 143, 0x8F
    OP_NEGATE(),
    // 144, 0x90
    OP_ABS(),
    // 145, 0x91
    OP_NOT(),
    // 146, 0x92
    OP_0NOTEQUAL(),
    // 147, 0x93
    OP_ADD(),
    // 148, 0x94
    OP_SUB(),
    // 149, 0x95
    OP_MUL(),
    // 150, 0x96
    OP_DIV(),
    // 151, 0x97
    OP_MOD(),
    // 152, 0x98
    OP_LSHIFT(),
    // 153, 0x99
    OP_RSHIFT(),
    // 154, 0x9A
    OP_BOOLAND(),
    // 155, 0x9B
    OP_BOOLOR(),
    // 156, 0x9C
    OP_NUMEQUAL(),
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
    OP_MIN(),
    // 164, 0xA4
    OP_MAX(),
    // 165, 0xA5
    OP_WITHIN(),
    // 166, 0xA6
    OP_RIPEMD160(),
    // 167, 0xA7
    OP_SHA1(),
    // 168, 0xA8
    OP_SHA256(),
    // 169, 0xA9
    OP_HASH160(),
    // 170, 0xAA
    OP_HASH256(),
    // 171, 0xAB
    OP_CODESEPARATOR(),
    // 172, 0xAC
    OP_CHECKSIG(),
    // 173, 0xAD
    OP_CHECKSIGVERIFY(),
    // 174, 0xAE
    OP_CHECKMULTISIG(),
    // 175, 0xAF
    OP_CHECKMULTISIGVERIFY(),

    // expansion
    // 176, 0xB0
    OP_NOP1(),
    // 177, 0xB1
    OP_CHECKLOCKTIMEVERIFY(),
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
    private final Instruction proxy;

    ScriptNames() {
        proxy = new InstructionAdaptor();
    }

    ScriptNames(InstructionAdaptor ins) {
        proxy = ins;
    }

    @Override
    public ScriptOperation compile(ByteBuf bf) {
        return proxy.compile(bf);
    }

    @Override
    public void exec(Interpreter interpreter, Object data, int pc) {
        proxy.exec(interpreter, data, pc);
    }

    @Override
    public void exec(Interpreter interpreter, Object data) {
        proxy.exec(interpreter, data);
    }

    @Override
    public byte opcode() {
        return (byte) this.ordinal();
    }
}
