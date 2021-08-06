package com.github.microwww.bitcoin.script;

public enum TemplateTransaction {
    P2PK,
    P2PKH,
    MN,
    P2SH,
    P2WPKH() {
        @Override
        public boolean isSupport(byte[] data) {// [0x00][0x14] [Hash160(PK)]
            return TemplateTransaction.p2wSupport(data, 0x14);
        }

        @Override
        public void executor(Interpreter interpreter) { // TODO:: 重写
            byte[] sc = new byte[10];
            int i = 0;
            sc[i++] = ScriptNames.OP_DROP.opcode();
            sc[i++] = ScriptNames._4.opcode();
            sc[i++] = 0x01; // 大端的整数 1
            sc[i++] = 0x00;
            sc[i++] = 0x00;
            sc[i++] = 0x00;
            sc[i++] = ScriptNames.OP_PICK.opcode();
            sc[i++] = ScriptNames.OP_HASH160.opcode();
            sc[i++] = ScriptNames.OP_EQUALVERIFY.opcode();
            sc[i++] = ScriptNames.OP_CHECKSIG.opcode();
            interpreter.subScript().executor(sc);
        }
    },
    P2WSH() {
        @Override
        public boolean isSupport(byte[] data) {// [0x00][0x20] [SHA256(witnessScript)]
            return TemplateTransaction.p2wSupport(data, 0x20);
        }
    },

    P2SH_P2WPKH,
    P2SH_P2WSH,
    OP_RETURN,
    ;

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
        throw new UnsupportedOperationException();
    }
}
