package com.github.microwww.bitcoin.wallet.cash.account;

import com.github.microwww.bitcoin.util.ByteUtil;

public class BechBitcoin {
    private static final int[] GENERATOR = {0x3b6a57b2, 0x26508e6d, 0x1ea119fa, 0x3d4233dd, 0x2a1462b3};

    public enum Enc {
        BECH32(1), BECH32M(0x2bc830a3);
        public final int val;

        Enc(int val) {
            this.val = val;
        }
    }

    public int polymod(byte[] values) {
        int chk = 1;
        for (int p = 0; p < values.length; ++p) {
            int top = chk >> 25;
            chk = (chk & 0x1ffffff) << 5 ^ values[p];
            for (int i = 0; i < 5; ++i) {
                if (((top >> i) & 1) == 1) {
                    chk ^= GENERATOR[i];
                }
            }
        }
        return chk;
    }

    public byte[] hrpExpand(String hrp) {
        char[] chars = hrp.toCharArray();
        int length = chars.length;
        byte[] ret = new byte[length + 1 + length];
        int p = 0;
        for (int i = 0; i < length; i++, p++) {
            ret[i] = (byte) (chars[i] >> 5);
        }
        p++;
        for (int i = 0; i < length; i++) {
            ret[p + i] = (byte) (chars[i] & 31);
        }
        return ret;
    }

    public byte[] createChecksum(String hrp, byte[] data, Enc enc) {
        byte[] values = ByteUtil.concat(hrpExpand(hrp), data, new byte[]{0, 0, 0, 0, 0, 0});
        int mod = polymod(values) ^ enc.val;
        byte[] ret = new byte[6];
        for (int p = 0; p < 6; ++p) {
            ret[p] = (byte) ((mod >> 5 * (5 - p)) & 31);
        }
        return ret;
    }
}
