package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.store.DiskBlock;
import com.github.microwww.bitcoin.store.IndexTransaction;
import org.springframework.stereotype.Component;

@Component
public class LocalBlockChain {
    private DiskBlock diskBlock;
    private IndexTransaction indexTransaction;
    private CChainParams chainParams;

    public LocalBlockChain(CChainParams chainParams, DiskBlock diskBlock, IndexTransaction indexTransaction) {
        this.diskBlock = diskBlock;
        this.indexTransaction = indexTransaction;
        this.chainParams = chainParams;
    }

    public DiskBlock getDiskBlock() {
        return diskBlock;
    }

    public LocalBlockChain setDiskBlock(DiskBlock diskBlock) {
        this.diskBlock = diskBlock;
        return this;
    }

    public IndexTransaction getTransactionStore() {
        return indexTransaction;
    }

    public LocalBlockChain setTransactionStore(IndexTransaction indexTransaction) {
        this.indexTransaction = indexTransaction;
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
