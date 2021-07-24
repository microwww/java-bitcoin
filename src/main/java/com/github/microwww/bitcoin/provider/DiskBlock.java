package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.math.Uint32;
import org.springframework.stereotype.Component;

/**
 * struct CDiskTxPos : public FlatFilePos
 * init.cpp | CleanupBlockRevFiles
 * validation.h  Open a block file (blk?????.dat) FILE* OpenBlockFile(const FlatFilePos &pos, bool fReadOnly = false);
 **/
@Component
public class DiskBlock {

    public ChainBlock getHash(int height) {// TODO : block file !
        throw new UnsupportedOperationException();
    }

    public ChainBlock getLatestBlock() {
        throw new UnsupportedOperationException();
    }

    public DiskBlock addBlock(ChainBlock header) {
        throw new UnsupportedOperationException();
    }

    public DiskBlock setChainBlock(ChainBlock block) {
        throw new UnsupportedOperationException();
    }

    public Uint32 getHeight() {
        throw new UnsupportedOperationException();
    }
}
