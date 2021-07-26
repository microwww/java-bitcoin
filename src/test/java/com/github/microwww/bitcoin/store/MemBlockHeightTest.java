package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.provider.DiskBlock;
import org.iq80.leveldb.DB;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MemBlockHeightTest {
    private static final CChainParams pa = new CChainParams(new Settings());

    @Test
    void tryAdd() {
        MemBlockHeight mh = new MemBlockHeight(pa);
        ChainBlock genesisBlock = pa.env.createGenesisBlock();
        mh.init(genesisBlock);
        LinkedList<ChainBlock> block = createChainBlock(genesisBlock, 100);
        block.pollFirst();
        while (true) {
            ChainBlock chainBlock = block.pollFirst();
            if (chainBlock == null) {
                break;
            }
            mh.tryAdd(chainBlock);
        }
        System.out.println(mh.toString());
        assertEquals(100, mh.getLatestHeight());
    }

    public LinkedList<ChainBlock> createChainBlock(ChainBlock genesis, int size) {
        LinkedList<ChainBlock> list = new LinkedList<>();
        list.add(genesis);
        for (int i = 0; i < size; i++) {
            ChainBlock o = list.getLast();
            ChainBlock cb = new ChainBlock();
            cb.setTxs(new RawTransaction[]{});
            cb.header
                    .setVersion(1)
                    .setTime(Uint32.ZERO)
                    .setPreHash(o.hash())
                    .setMerkleRoot(Uint256.ZERO)
                    .setBits(Uint32.ZERO)
                    .setNonce(Uint32.ZERO)
            ;
            list.add(cb);
        }
        return list;
    }


    @Test
    void add() throws IOException {
        pa.settings.setDataDir("/" + UUID.randomUUID().toString());
        DiskBlock diskBlock = new DiskBlock(pa);
        diskBlock.init();
        DB levelDB = diskBlock.getLevelDB();
        ChainBlock genesisBlock = pa.env.createGenesisBlock();
        LinkedList<ChainBlock> chains = createChainBlock(genesisBlock, 100);
        for (ChainBlock chain : chains) {
            diskBlock.writeBlock(chain, true);
        }
        levelDB.put(LevelDBPrefix.DB_LAST_BLOCK.prefixBytes, chains.getLast().serialization().array());
        diskBlock.close();
        // -----------------------
        diskBlock = new DiskBlock(pa);
        diskBlock.init();
        assertEquals(100, diskBlock.getLatestHeight());
        ChainBlock nb = createChainBlock(chains.getLast(), 1).get(0);
        diskBlock.writeBlock(nb, true);
        diskBlock.getLatestHeight();
    }
}