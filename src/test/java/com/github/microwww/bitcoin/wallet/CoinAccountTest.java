package com.github.microwww.bitcoin.wallet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoinAccountTest {

    @Test
    void createBitcoinAccount() {
        CoinAccount.KeyPrivate ac = CoinAccount.KeyPrivate.create();
        CoinAccount.KeyPrivate a2 = CoinAccount.KeyPrivate.importPrivateKey(ac.dumpedPrivateKey(Env.MAIN));
        assertArrayEquals(ac.getKey(), a2.getKey());
        System.out.println(a2.dumpedPrivateKey(Env.REG_TEST));
        System.out.println(a2.getAddress().toBase58Address(Env.REG_TEST));
        System.out.println(a2.getAddress().toBech32Address(Env.REG_TEST));
    }

    @Test
    public void importAddress() {
        CoinAccount.Address address = CoinAccount.KeyPrivate.create().getAddress();
        String addr = address.toBase58Address(Env.MAIN);
        CoinAccount.Address ac = CoinAccount.Address.importAddress(addr);
        assertEquals(addr, ac.toBase58Address(Env.MAIN));
    }

    @Test
    public void importPrivateKey() {
        String s = "cTpLX91cyzALTh5nxJSxYzhBGXKB8vgBDAi5LPu1dYkxGYJ55EtV";
        // bcrt1qpnz7xmzpngxsewqa7npfvph5d69444nakxd20s
        CoinAccount.KeyPrivate keyPrivate = CoinAccount.KeyPrivate.importPrivateKey(s);
        String s1 = keyPrivate.getAddress().toBase58Address(Env.REG_TEST);
        System.out.println(s1);
        s1 = keyPrivate.getAddress().toBech32Address(Env.REG_TEST);
        assertEquals("bcrt1qpnz7xmzpngxsewqa7npfvph5d69444nakxd20s", s1);
    }

    @Test
    public void dumpedPrivateKey() {
        String v = "cVTJeYm7wfNckRg1Fi72YABcyQdswddAagoaDywgCN2tmmp96a3r";
        CoinAccount.KeyPrivate cv = CoinAccount.KeyPrivate.importPrivateKey(v);
        String s = cv.dumpedPrivateKey(Env.REG_TEST);
        assertEquals(v, s);
    }

    @Test
    public void toBech32Address() {
        String s = "cVZTHk1AnJLZKkxHd7CYKTmQhBd6B3E5X8kCjoYhiTdBx5FX9LYt";
        CoinAccount.KeyPrivate keyPrivate = CoinAccount.KeyPrivate.importPrivateKey(s);
        String s1 = keyPrivate.getAddress().toBase58Address(Env.REG_TEST);
        assertEquals("mkvXJenDDLaydfN2kRCu3oKbYEniKveNHD", s1);
        s1 = keyPrivate.getAddress().toBech32Address(Env.REG_TEST);
        assertEquals("bcrt1q8dxuqm8g45ygewfyxkck8fdczz030qwvmshz8s", s1);
    }

    @Test
    public void toBase58Address() {
        String s = "cVZTHk1AnJLZKkxHd7CYKTmQhBd6B3E5X8kCjoYhiTdBx5FX9LYt";
        CoinAccount.KeyPrivate keyPrivate = CoinAccount.KeyPrivate.importPrivateKey(s);
        assertEquals("mkvXJenDDLaydfN2kRCu3oKbYEniKveNHD", keyPrivate.getAddress().toBase58Address(Env.REG_TEST));
    }

}