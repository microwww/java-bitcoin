package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.store.DiskBlock;
import com.github.microwww.bitcoin.store.IndexTransaction;
import org.springframework.stereotype.Component;

@Component
public class LocalBlockChain {
    private final DiskBlock diskBlock;
    private final IndexTransaction indexTransaction;
    private final CChainParams chainParams;

    public LocalBlockChain(CChainParams chainParams, DiskBlock diskBlock, IndexTransaction indexTransaction) {
        this.diskBlock = diskBlock;
        this.indexTransaction = indexTransaction;
        this.chainParams = chainParams;
    }

    public DiskBlock getDiskBlock() {
        return diskBlock;
    }

    public IndexTransaction getTransactionStore() {
        return indexTransaction;
    }

    public CChainParams getChainParams() {
        return chainParams;
    }

    public Settings getSettings() {
        return this.chainParams.settings;
    }
}
