package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.store.DiskBlock;
import com.github.microwww.bitcoin.store.TxMemPool;
import org.springframework.stereotype.Component;

@Component
public class LocalBlockChain {
    private DiskBlock diskBlock;
    private TxMemPool txMemPool;
    private CChainParams chainParams;

    public LocalBlockChain(CChainParams chainParams, DiskBlock diskBlock, TxMemPool txMemPool) {
        this.diskBlock = diskBlock;
        this.txMemPool = txMemPool;
        this.chainParams = chainParams;
    }

    public DiskBlock getDiskBlock() {
        return diskBlock;
    }

    public LocalBlockChain setDiskBlock(DiskBlock diskBlock) {
        this.diskBlock = diskBlock;
        return this;
    }

    public TxMemPool getTxMemPool() {
        return txMemPool;
    }

    public LocalBlockChain setTxMemPool(TxMemPool txMemPool) {
        this.txMemPool = txMemPool;
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
