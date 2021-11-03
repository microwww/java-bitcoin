package com.github.microwww.bitcoin.wallet;

import com.github.microwww.bitcoin.AbstractEnv;
import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.util.ClassPath;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WalletTest extends AbstractEnv {

    public WalletTest() {
        super(CChainParams.Env.REG_TEST);
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
}