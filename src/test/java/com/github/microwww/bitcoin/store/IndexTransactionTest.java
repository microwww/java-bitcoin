package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.AbstractEnv;
import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.util.ClassPath;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class IndexTransactionTest extends AbstractEnv {

    public IndexTransactionTest() {
        super(CChainParams.Env.MAIN);
    }

    @Test
    void serializationTransaction() throws IOException {
        ChainBlock gn = chainParams.env.G;
        IndexTransaction tx = this.indexTransaction;
        FileChainBlock block = tx.getDiskBlock().writeBlock(gn, 0, true);
        tx.indexTransaction(block);
        Uint256 hash = block.getTarget().getTxs()[0].hash();
        RawTransaction rt = tx.findTransaction(hash).get().load(true);
        assertEquals(hash, rt.hash());
    }

    @Test
    void serializationLevelDB() {
        IndexTransaction tx = this.indexTransaction;
        ByteBuf buffer = Unpooled.buffer();
        String x = ClassPath.readClassPathFile("/data/online.data.txt").get(43);
        buffer.writeBytes(ByteUtil.hex(x));
        ChainBlock gn = new ChainBlock().reset(buffer);
        FileChainBlock fc = tx.getDiskBlock().writeBlock(gn, 0, true);
        FileTransaction[] fts = tx.transactionPosition(fc, 8 + fc.getPosition());

        FileTransaction ft = fts[0];
        tx.serializationLevelDB(ft, buffer);
        byte[] bytes = ByteUtil.readAll(buffer);
        FileTransaction nft = tx.deserializationLevelDB(bytes);
        tx.serializationLevelDB(nft, buffer.clear());
        assertArrayEquals(bytes, ByteUtil.readAll(buffer));
    }
}