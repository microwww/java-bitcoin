package com.github.microwww.bitcoin.wallet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Account4bitcoinTest {

    @Test
    void createBitcoinAccount() {
        Account4bitcoin ac = Account4bitcoin.createBitcoinAccount(BitAccountConfig.REG_TEST);
        Account4bitcoin a2 = Account4bitcoin.fromBase58(ac.getPrivateKeyBase58());
        assertEquals(ac.getPrivateKeyBase58(), a2.getPrivateKeyBase58());
    }

}