package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.script.instruction.ScriptNames;
import com.github.microwww.bitcoin.store.DiskBlock;
import com.github.microwww.bitcoin.store.TxPool;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.util.ClassPath;
import com.github.microwww.bitcoin.wallet.Env;
import com.github.microwww.bitcoin.wallet.Wallet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class GenChainBlockTest {
    static CChainParams params;
    static Wallet w;

    @BeforeAll
    public static void init() throws SQLException, IOException {
        params = new CChainParams(new Settings(CChainParams.Env.REG_TEST));
        params.settings.setDataDir("/tmp/" + UUID.randomUUID()).setTxIndex(true);
        String file = GenChainBlockTest.class.getResource("/").getFile();
        w = new Wallet(new File(file), Env.MAIN);
        w.init();
    }

    @Test
    public void mining() throws InterruptedException {
        ByteBuf buffer = Unpooled.buffer();
        DiskBlock d = new DiskBlock(params);
        TxPool pool = new TxPool();
        GenChainBlock gen = new GenChainBlock(w, d, pool);
        ChainBlock pre = params.env.createGenesisBlock();
        long coin = Generating.getBlockSubsidy(pre.getHeight(), params.env);
        byte[] s = "JAVA-agent".getBytes(StandardCharsets.UTF_8);
        Uint256 mk = null;
        {
            buffer.clear().writeByte(ScriptNames.OP_RETURN.opcode()).writeByte(s.length).writeBytes(s);
            ChainBlock block = gen.genBlock(pre, ByteUtil.readAll(buffer), coin);
            Thread thread = new Thread(() -> {
                gen.mining(block);
            });
            thread.start();

            TimeUnit.SECONDS.sleep(1);
            thread.interrupt();
            TimeUnit.MILLISECONDS.sleep(100);
            assertFalse(thread.isAlive());
            mk = block.header.getMerkleRoot();
        }
        {
            buffer.clear().writeByte(ScriptNames.OP_RETURN.opcode()).writeByte(s.length).writeBytes(s);
            ChainBlock block = gen.genBlock(pre, ByteUtil.readAll(buffer), coin);
            List<String> lns = ClassPath.readClassPathFile("/data/line-data.txt");
            RawTransaction tr = new RawTransaction();
            tr.deserialization(buffer.writeBytes(ByteUtil.hex(lns.get(44))));
            pool.put(tr.hash(), tr);
            tr.deserialization(buffer.writeBytes(ByteUtil.hex(lns.get(46))));
            pool.put(tr.hash(), tr);
            gen.genTran(block);
            gen.mining(block);
            block.header.assertDifficulty();
            assertNotEquals(mk, block.merkleRoot());
        }
    }

}