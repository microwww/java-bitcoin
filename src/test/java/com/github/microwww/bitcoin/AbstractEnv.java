package com.github.microwww.bitcoin;

import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.provider.LocalBlockChain;
import com.github.microwww.bitcoin.store.DiskBlock;
import com.github.microwww.bitcoin.store.IndexTransaction;
import com.github.microwww.bitcoin.store.TxPool;
import com.github.microwww.bitcoin.util.FilesUtil;
import com.github.microwww.bitcoin.wallet.Wallet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public abstract class AbstractEnv {
    protected final CChainParams chainParams;
    protected DiskBlock diskBlock;
    protected IndexTransaction indexTransaction;
    protected Wallet wallet;
    protected TxPool txPool;
    protected LocalBlockChain localBlockChain;

    public AbstractEnv(CChainParams.Env env) {
        chainParams = new CChainParams(new Settings(env));
        chainParams.settings.setTxIndex(true);
        chainParams.settings.setDataDir("/tmp/" + UUID.randomUUID());
    }

    @BeforeEach
    public void initAll() {
        diskBlock = new DiskBlock(chainParams).init();
        wallet = Wallet.wallet(chainParams);
        indexTransaction = new IndexTransaction(chainParams, wallet, diskBlock);
        // down code do not need close
        txPool = new TxPool(wallet);
        localBlockChain = new LocalBlockChain(chainParams, diskBlock, indexTransaction);
    }

    @AfterEach
    public void closeAll() throws IOException {
        indexTransaction.close();
        wallet.close();
        diskBlock.close();
        FilesUtil.deleteRecursively(new File(chainParams.settings.getDataDir()));
    }

}
