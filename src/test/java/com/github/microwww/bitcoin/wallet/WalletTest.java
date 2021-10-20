package com.github.microwww.bitcoin.wallet;

import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.util.ClassPath;
import com.github.microwww.bitcoin.util.FilesUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WalletTest {

    static File file = new File("/tmp/" + UUID.randomUUID());
    static Wallet wallet;

    @BeforeAll
    public static void init() throws SQLException, IOException {
        wallet = new Wallet(file, Env.MAIN);
        wallet.init();
    }

    @Test
    public void testAccount() {
        int size = wallet.listAddress().size();
        CoinAccount.KeyPrivate keyPrivate = CoinAccount.KeyPrivate.create();
        wallet.insert("", keyPrivate);
        keyPrivate = CoinAccount.KeyPrivate.create();
        wallet.insert("", keyPrivate.getAddress());
        List<AccountDB> accountDBS = wallet.listAddress();
        assertEquals(size + 2, accountDBS.size());
    }

    @Test
    public void localTransaction() {
        CoinAccount.KeyPrivate keyPrivate = CoinAccount.KeyPrivate.importPrivateKey("cUENsdriHKq2jnTtBzv71CDPwa5ZW5TASPpCB3tRvfENX1E4kYx1");
        wallet.insert("", keyPrivate);
        String lines = ClassPath.readClassPathFile("/data/line-data.txt").get(119);
        RawTransaction trans = new RawTransaction();
        ByteBuf bf = Unpooled.buffer().writeBytes(ByteUtil.hex(lines));
        trans.deserialization(bf);
        wallet.localTransaction(trans);
        List<AccTrans> ts = wallet.selectTransaction(keyPrivate.getAddress().getKeyPublicHash());
        assertEquals(1, ts.size());
    }

    @AfterAll
    public static void after() {
        FilesUtil.deleteRecursively(file);
    }
}