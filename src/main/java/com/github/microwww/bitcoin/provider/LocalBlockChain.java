package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.store.DiskBlock;
import com.github.microwww.bitcoin.store.TxPool;
import org.springframework.stereotype.Component;

@Component
public class LocalBlockChain {
    private final DiskBlock diskBlock;
    private final CChainParams chainParams;
    private final TxPool txPool;

    public LocalBlockChain(CChainParams chainParams, DiskBlock diskBlock, TxPool txPool) {
        this.chainParams = chainParams;
        this.diskBlock = diskBlock;
        this.txPool = txPool;
    }

    public DiskBlock getDiskBlock() {
        return diskBlock;
    }

    public TxPool getTxPool() {
        return txPool;
    }

    public CChainParams getChainParams() {
        return chainParams;
    }

    public Settings getSettings() {
        return this.chainParams.settings;
    }
}
