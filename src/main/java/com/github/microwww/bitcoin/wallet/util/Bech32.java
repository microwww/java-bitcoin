package com.github.microwww.bitcoin.wallet.util;

public class Bech32 {
    public static final String CHARSET = "qpzry9x8gf2tvdw0s3jn54khce6mua7l";

    /**
     * The cashaddr character set for decoding.
     */
    public static final byte[] CHARSET_REV = { // 127
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 16+
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 2
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 3
            15, -1, 10, 17, 21, 20, 26, 30, +7, +5, -1, -1, -1, -1, -1, -1, // 4
            -1, 29, -1, 24, 13, 25, +9, +8, 23, -1, 18, 22, 31, 27, 19, -1, // 5
            +1, +0, +3, 16, 11, 28, 12, 14, +6, +4, +2, -1, -1, -1, -1, -1, // 6
            -1, 29, -1, 24, 13, 25, +9, +8, 23, -1, 18, 22, 31, 27, 19, -1, // 7
            +1, +0, +3, 16, 11, 28, 12, 14, 6, 4, 2, -1, -1, -1, -1, -1 ////// 8
    };

    private Bech32() {
    }

    public static final Bech32 BECH = new Bech32();

    /**
     * not 8 -> 5
     *
     * @param str
     * @return
     */
    public byte[] bechDecode(String str) {
        byte[] values = new byte[str.length()];
        for (int i = 0; i < str.length(); ++i) {
            byte c = (byte) str.charAt(i);
            // We have an invalid char in there.
            if (c > 127 || CHARSET_REV[c] == -1) {
                throw new IllegalArgumentException("Char at 0-127 ! But " + (int) c);
            }

            values[i] = CHARSET_REV[c];
        }
        return values;
    }

    /**
     * not do 8 -> 5 format
     *
     * @param payload
     * @return
     */
    public String bechEncode(byte[]... payload) {
        StringBuffer ret = new StringBuffer();
        for (byte[] cs : payload) {
            for (byte c : cs) {
                ret.append(CHARSET.charAt(c));
            }
        }
        return ret.toString();
    }

    public byte[] payloadDecode(byte[] payload) {
        return this.payloadDecode(payload, 0, payload.length);
    }

    /**
     * 5 -> 8
     *
     * @param payload
     * @param off
     * @param len
     * @return
     */
    public byte[] payloadDecode(byte[] payload, int off, int len) {
        BitArray from = new BitArray(payload, off, len);
        int nlen = len * 5 / 8;// int
        BitArray to = new BitArray(new byte[nlen]);
        for (int i = 0, j = i; i < to.bitLength(); i++, j++) {
            if (i % 5 == 0) {
                j += 3;
            }
            if (from.get(j)) {
                to.set(i);
            }
        }
        return to.toArray();
    }

    public byte[] payloadEncode(byte[] data) {
        return this.payloadEncode(data, 0, data.length);
    }

    /**
     * 8 -> 5
     *
     * @param data
     * @param off
     * @param len
     * @return
     */
    public byte[] payloadEncode(byte[] data, int off, int len) {
        BitArray from = new BitArray(data, off, len);
        int nlen = (int) Math.ceil(len * 8 / 5.0);
        BitArray to = new BitArray(new byte[nlen]);
        for (int i = 0, j = i; i < from.bitLength(); i++, j++) {
            if (i % 5 == 0) {// skip (8 - 5)
                j += 3;
            }
            if (from.get(i)) {
                to.set(j);
            }
        }
        return to.toArray();
    }
}
