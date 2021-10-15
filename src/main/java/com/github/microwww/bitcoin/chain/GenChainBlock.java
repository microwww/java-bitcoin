package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.script.PubKeyScript;
import com.github.microwww.bitcoin.script.instruction.ScriptNames;
import com.github.microwww.bitcoin.store.DiskBlock;
import com.github.microwww.bitcoin.store.TxPool;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.wallet.AccountDB;
import com.github.microwww.bitcoin.wallet.Wallet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class GenChainBlock {
    private static final Logger log = LoggerFactory.getLogger(GenChainBlock.class);

    private final Wallet wallet;
    private final DiskBlock diskBlock;
    private final TxPool txPool;

    public GenChainBlock(Wallet wallet, DiskBlock diskBlock, TxPool txPool) {
        this.wallet = wallet;
        this.diskBlock = diskBlock;
        this.txPool = txPool;
    }

    public boolean mining(ChainBlock mining) {
        return this.miningByCoinbase(mining, e -> {
            return miningByTime(e, ee -> {
                return miningByNonce(ee, eee -> {
                    return eee.header.verifyDifficulty();
                });
            });
        });
    }

    public boolean miningByCoinbase(ChainBlock mining, Predicate<ChainBlock> inner) {
        byte[] script = mining.getTxs()[0].getTxIns()[0].getScript();
        byte[] ss = new byte[script.length + 5];
        System.arraycopy(script, 0, ss, 5, script.length);
        ss[0] = ScriptNames._32.opcode();
        for (int i = 0; i < 32; i++) {
            for (int v = 0; v <= 256; v++) {
                boolean ok = inner.test(mining);
                if (ok) {
                    return true;
                }
                ss[i] = (byte) v;
                log.warn("Mining co tx-in coinbase: {},{}, pre: {}", i, v, mining.header.getPreHash().toHexReverse256());
                mining.getTxs()[0].getTxIns()[0].setScript(ss);
                mining.header.setMerkleRoot(mining.merkleRoot());
            }
        }
        return false;
    }

    public boolean miningByTime(ChainBlock mining, Predicate<ChainBlock> inner) {
        final long time = mining.header.getTime().longValue();
        for (int i = 1; i < 60 * 20; i++) { // 20 分钟内
            boolean ok = inner.test(mining);
            if (ok) {
                return true;
            }
            log.info("Mining co time: {}", time * 1000);
            mining.header.setTime(new Uint32(time + i));
        }
        return false;
    }

    public boolean miningByNonce(ChainBlock mining, Predicate<ChainBlock> inner) {
        long nonce = mining.header.getNonce().longValue();
        log.debug("Mining co nonce: {}", nonce);
        do {
            boolean ok = inner.test(mining); //mining.header.verifyDifficulty();
            if (ok) {
                return true;
            }
            nonce++;
            mining.header.setNonce(new Uint32(nonce));
            if (Thread.currentThread().isInterrupted()) {
                throw new RuntimeException("Mining thread is Interrupted !");
            }
        } while (nonce < 0x01FFFFFFFFL);

        return false;
    }

    public ChainBlock genTran(ChainBlock block) {
        List<Uint256> us = txPool.get(10);
        byte[] sr = block.serialization();
        int max = 1024 * 1024 - 100 - sr.length;
        ByteBuf buffer = Unpooled.buffer();
        List<RawTransaction> trans = new ArrayList<>(Arrays.asList(block.getTxs()));
        for (Uint256 u : us) {
            RawTransaction tx = txPool.remove(u);
            // TODO :: 这里校验交易 和 锁定
            tx.serialization(buffer);
            int len = buffer.readableBytes();
            if (max < len) {
                break;
            }
            trans.add(tx);
        }
        block.setTxs(trans.toArray(new RawTransaction[]{}));
        block.header.setMerkleRoot(block.merkleRoot());
        return block;
    }

    // 注意去修改 TX, AND MerkleRoot
    public ChainBlock genBlock(ChainBlock pre, byte[] coinbaseScript, long amount) {
        AccountDB acc = wallet.getCoinBaseAddress();
        ByteBuf bf = PubKeyScript.Type.P2PKH.scriptPubKey(acc.getPkHash());
        TxIn in = genTxIn(coinbaseScript);
        TxOut out = genTxOut(ByteUtil.readAll(bf), amount);
        RawTransaction tx = genTx(in, out);

        ChainBlock genesis = new ChainBlock();
        genesis.header.setVersion(1);
        genesis.setTxs(new RawTransaction[]{tx});
        genesis.header.setMerkleRoot(genesis.merkleRoot());
        genesis.header.setPreHash(pre.hash());
        genesis.header.setHeight(pre.getHeight() + 1);
        Uint32 uint32 = diskBlock.nextPow(pre);
        genesis.header.setBits(uint32);
        genesis.header.setTime(new Uint32(System.currentTimeMillis() / 1000));
        genesis.header.setNonce(Uint32.ZERO);
        log.debug("注意去修改 TX 和 MerkleRoot");
        return genesis;
    }

    private static RawTransaction genTx(TxIn in, TxOut out) {
        RawTransaction txNew = new RawTransaction();
        txNew.setVersion(1);
        // txNew.vin[0].scriptSig = CScript() << 486604799 << CScriptNum(4) << std::vector<unsigned char>((const unsigned char*)pszTimestamp, (const unsigned char*)pszTimestamp + strlen(pszTimestamp));
        txNew.setTxIns(new TxIn[]{in});
        txNew.setTxOuts(new TxOut[]{out});
        txNew.setLockTime(Uint32.ZERO);
        return txNew;
    }

    private static TxOut genTxOut(byte[] pkScript, long amount) {
        TxOut out = new TxOut();
        out.setValue(amount);
        out.setScriptPubKey(pkScript);
        return out;
    }

    private static TxIn genTxIn(byte[] coinbaseScript) {
        TxIn in = new TxIn();
        in.setScript(coinbaseScript);
        in.setPreTxHash(Uint256.ZERO);
        in.setPreTxOutIndex(-1);
        in.setSequence(Uint32.MAX_VALUE);
        return in;
    }
}
