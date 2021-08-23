package com.github.microwww.bitcoin.wallet;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

class WalletTest {

    @Test
    public void testCreate() throws SQLException, IOException {
        Wallet w = new Wallet(new File("/tmp/" + UUID.randomUUID()));
        w.init();

        CoinAccount.KeyPrivate keyPrivate = CoinAccount.KeyPrivate.create();
        w.insert("", keyPrivate);
        keyPrivate = CoinAccount.KeyPrivate.create();
        w.insert("", keyPrivate.getAddress());
        List<AccountDB> accountDBS = w.listAddress();
        for (AccountDB a : accountDBS) {
            System.out.println(a);
        }
    }

}