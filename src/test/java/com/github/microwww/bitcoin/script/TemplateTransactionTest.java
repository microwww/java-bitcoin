package com.github.microwww.bitcoin.script;

import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.wallet.CoinAccount;
import com.github.microwww.bitcoin.wallet.Env;
import com.github.microwww.bitcoin.wallet.util.Base58;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TemplateTransactionTest {

    @Test
    void getScriptForMultiSig() {
        byte[] pk1 = ByteUtil.hex("026477115981fe981a6918a6297d9803c4dc04f328f22041bedff886bbc2962e01");
        byte[] pk2 = ByteUtil.hex("02c96db2302d19b43d4c69368babace7854cc84eb9e061cde51cfa77ca4a22b8b9");
        byte[] pk3 = ByteUtil.hex("03c6103b3b83e4a24a0e33a4df246ef11772f9992663db0c35759a5e2ebf68d8e9");

        byte[] ss = TemplateTransaction.getScriptForMultiSig(2, pk1, pk2, pk3);
        System.out.println(ByteUtil.hex(ss));
        for (int i = 0; i < 256; i++) {
            String encode = CoinAccount.toBase58((byte) i, CoinAccount.sha256ripemd160(ss));
            System.out.println(encode);
        }
    }
}