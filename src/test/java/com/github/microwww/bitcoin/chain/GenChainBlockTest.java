package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.AbstractEnv;
import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.script.instruction.ScriptNames;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.util.ClassPath;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class GenChainBlockTest extends AbstractEnv {

    public GenChainBlockTest() {
        super(CChainParams.Env.REG_TEST);
    }

    @Test
    public void mining() throws InterruptedException {
        ByteBuf buffer = Unpooled.buffer();
        GenChainBlock gen = new GenChainBlock(wallet, this.diskBlock, this.txPool);
        ChainBlock pre = chainParams.env.G;
        long coin = Generating.getBlockSubsidy(pre.getHeight(), chainParams.env);
        byte[] s = "JAVA-agent".getBytes(StandardCharsets.ISO_8859_1);
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
            txPool.put(tr.hash(), tr);
            tr.deserialization(buffer.writeBytes(ByteUtil.hex(lns.get(46))));
            txPool.put(tr.hash(), tr);
            gen.genTran(block);
            gen.mining(block);
            block.header.assertDifficulty();
            assertNotEquals(mk, block.merkleRoot());
        }
    }

}