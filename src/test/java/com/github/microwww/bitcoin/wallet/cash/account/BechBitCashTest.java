package com.github.microwww.bitcoin.wallet.cash.account;

import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.wallet.CoinAccount;
import com.github.microwww.bitcoin.wallet.Env;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BechBitCashTest {

    @Test
    public void testConversion(){
        CoinAccount.Address address = new CoinAccount.Address(ByteUtil.hex("0279BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798"));
        System.out.println( address.toBase58Address(Env.MAIN) );
        System.out.println( address.toBech32Address(Env.TEST) );
    }
}