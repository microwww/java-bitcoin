package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import org.iq80.leveldb.DB;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class MemBlockHeightTest {
    private static final Logger logger = LoggerFactory.getLogger(MemBlockHeightTest.class);
    private static final CChainParams pa = new CChainParams(new Settings());

    @Test
    void tryAdd() {
        pa.settings.setDataDir("/" + UUID.randomUUID().toString());
        MemBlockHeight mh = new MemBlockHeight(pa.env.createGenesisBlock());
        ChainBlock genesisBlock = pa.env.createGenesisBlock();
        List<ChainBlock> block = createChainBlock(genesisBlock, 100);
        for (int i = 1; i < block.size(); i++) {
            ChainBlock chainBlock = block.get(i);
            mh.tryAdd(chainBlock);
        }
        System.out.println(mh.toString());
        assertEquals(100, mh.getLatestHeight());
        assertEquals(mh.get(0).get(), genesisBlock.hash());
        assertEquals(mh.get(mh.getLatestHeight()).get(), block.get(block.size() - 1).hash());
    }

    @Test
    void writeAndRead() throws IOException {
        pa.settings.setDataDir("/" + UUID.randomUUID().toString());
        DiskBlock diskBlock = new DiskBlock(pa);
        diskBlock.init();
        DB levelDB = diskBlock.getLevelDB();
        ChainBlock genesisBlock = pa.env.createGenesisBlock();
        List<ChainBlock> chains = createChainBlock(genesisBlock, 100);
        ChainBlock last = chains.get(chains.size() - 1);
        for (ChainBlock chain : chains) {
            diskBlock.writeBlock(chain, true);
        }
        // levelDB.put(LevelDBPrefix.DB_LAST_BLOCK.prefixBytes, new HeightChainBlock(last, chains.size() - 1).serialization());
        diskBlock.close();
        logger.info(" --------------------------  新的开始 ---------------------------------------------");
        diskBlock = new DiskBlock(pa);
        diskBlock.init();
        assertEquals(100, diskBlock.getLatestHeight());
        ChainBlock nb = createChainBlock(last, 1).get(0);
        diskBlock.writeBlock(nb, true);
        diskBlock.getLatestHeight();
    }

    @Test
    void conflictHeight() {
        pa.settings.setDataDir("/" + UUID.randomUUID());
        DiskBlock diskBlock = new DiskBlock(pa);
        diskBlock.init();
        ChainBlock genesisBlock = pa.env.createGenesisBlock();
        List<ChainBlock> chains = createChainBlock(genesisBlock, 10);
        for (ChainBlock chain : chains) {
            diskBlock.writeBlock(chain, true);
        }
        int latestHeight = diskBlock.getLatestHeight();
        assertEquals(10, latestHeight);
        Uint256 h10 = diskBlock.getHash(10).get();

        List<ChainBlock> inv = createChainBlock(chains.get(8), 5);
        for (ChainBlock chain : inv) {
            diskBlock.writeBlock(chain, true);
        }
        latestHeight = diskBlock.getLatestHeight();
        Uint256 h10_2 = diskBlock.getHash(10).get();
        assertNotEquals(h10, h10_2);
        assertEquals(13, latestHeight);

        List<ChainBlock> list = new ArrayList<>(chains.subList(0, 8));
        list.addAll(inv);
        for (int i = 0; i < latestHeight; i++) {
            assertEquals(list.get(i).hash(), diskBlock.getHash(i).get());
        }
    }

    public List<ChainBlock> createChainBlock(ChainBlock genesis, int size) {
        List<ChainBlock> list = new ArrayList<>();
        list.add(genesis);
        for (int i = 0; i < size; i++) {
            ChainBlock cb = new ChainBlock();
            cb.setTxs(new RawTransaction[]{});
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ThreadLocalRandom current = ThreadLocalRandom.current();
            current.ints(256, Byte.MIN_VALUE, Byte.MAX_VALUE).forEach(e -> {
                out.write(e);
            });
            byte[] b256 = out.toByteArray();
            assertEquals(256, b256.length);
            cb.header
                    .setVersion(1)
                    .setTime(new Uint32(current.nextInt(1, 10_000_000)))
                    .setPreHash(genesis.hash())
                    .setMerkleRoot(new Uint256(b256))
                    .setBits(new Uint32(current.nextInt(1, 10_000_000)))
                    .setNonce(new Uint32(current.nextInt(1, 10_000_000)))
            ;
            list.add(cb);
            genesis = cb;
        }
        return list;
    }

}