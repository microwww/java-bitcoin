package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.math.Uint256;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IndexTransactionTest {
    private final CChainParams params = new CChainParams(new Settings());
    private IndexTransaction tx;

    {
        params.settings.setDataDir("/tmp/" + UUID.randomUUID())
                .setTxIndex(true);
        tx = new IndexTransaction(params);
        tx.setDiskBlock(new DiskBlock(params));
    }

    @Test
    void serializationTransaction() throws IOException {
        ChainBlock gn = params.env.createGenesisBlock();
        Optional<HeightBlock> heightBlock = tx.getDiskBlock().writeBlock(gn, 0, true);
        FileTransaction[] fs = heightBlock.get().getFileChainBlock().getFileTransactions();
        tx.serializationTransaction(fs);
        Uint256 hash = fs[0].getTransaction().hash();
        RawTransaction rt = tx.deserializationTransaction(hash).get().readFileRawTransaction();
        assertEquals(hash, rt.hash());
    }

}