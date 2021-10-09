package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.math.Uint256;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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
        FileChainBlock block = tx.getDiskBlock().writeBlock(gn, 0, true).getFileChainBlock();
        tx.indexTransaction(block);
        Uint256 hash = block.getTarget().get().getTxs()[0].hash();
        RawTransaction rt = tx.findTransaction(hash).get().load();
        assertEquals(hash, rt.hash());
    }

}