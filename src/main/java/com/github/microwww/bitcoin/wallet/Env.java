package com.github.microwww.bitcoin.wallet;

public enum Env {
    MAIN(0, 128, "bitcoincash", "bc", 5),
    TEST(111, 239, "bchtest", "tb", 196),
    REG_TEST(111, 239, "bchreg", "bcrt", 196),
    ;

    public final byte address;
    public final byte dumpedPrivateKey;
    public final String bitcash;
    public final String addressBECH32;
    public final byte addressP2SH;

    Env(int address, int dumpedPrivateKey, String bitcash, String addressBECH32, int addressP2SH) {
        this.address = (byte) address;
        this.dumpedPrivateKey = (byte) dumpedPrivateKey;
        this.bitcash = bitcash;
        this.addressBECH32 = addressBECH32;
        this.addressP2SH = (byte) addressP2SH;
    }
}
