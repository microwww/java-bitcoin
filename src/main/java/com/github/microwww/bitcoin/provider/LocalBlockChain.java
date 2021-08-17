package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.store.DiskBlock;
import com.github.microwww.bitcoin.store.TransactionStore;
import org.springframework.stereotype.Component;

@Component
public class LocalBlockChain {
    private DiskBlock diskBlock;
    private TransactionStore transactionStore;
    private CChainParams chainParams;

    public LocalBlockChain(CChainParams chainParams, DiskBlock diskBlock, TransactionStore transactionStore) {
        this.diskBlock = diskBlock;
        this.transactionStore = transactionStore;
        this.chainParams = chainParams;
    }

    public DiskBlock getDiskBlock() {
        return diskBlock;
    }

    public LocalBlockChain setDiskBlock(DiskBlock diskBlock) {
        this.diskBlock = diskBlock;
        return this;
    }

    public TransactionStore getTransactionStore() {
        return transactionStore;
    }

    public LocalBlockChain setTransactionStore(TransactionStore transactionStore) {
        this.transactionStore = transactionStore;
        return this;
    }

    public CChainParams getChainParams() {
        return chainParams;
    }

    public LocalBlockChain setChainParams(CChainParams chainParams) {
        this.chainParams = chainParams;
        return this;
    }

    public Settings getSettings() {
        return this.chainParams.settings;
    }
}
