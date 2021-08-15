package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxIn;
import com.github.microwww.bitcoin.chain.TxOut;
import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.conf.ChainBlockStore;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.script.Interpreter;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.iq80.leveldb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;

@Component
public class TxMemPool implements Closeable {
    private static final String TX_INDEX_DIR = "txindex";
    private static final Logger logger = LoggerFactory.getLogger(TxMemPool.class);
    private final CChainParams chainParams;
    private final HashSet<RawTransaction> transactions;
    private final int maxCount;
    private final DB levelDB;

    public TxMemPool(CChainParams chainParams) {
        this.chainParams = chainParams;
        transactions = new LinkedHashSet<>();
        maxCount = chainParams.settings.getTxPoolMax();
        try {
            File file = chainParams.settings.lockupRootDirectory();
            logger.info("Transaction Index-dir: {}{}{}", file.getCanonicalPath(), File.separator, TX_INDEX_DIR);
            levelDB = ChainBlockStore.leveldb(file, TX_INDEX_DIR, chainParams.settings.isReIndex());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void add(RawTransaction request) {
        // TODO:: 需要验证交易
        if (transactions.size() > maxCount) {
            RawTransaction next = transactions.iterator().next();
            transactions.remove(next);
        }
        transactions.add(request);
    }

    public void index(RawTransaction tx) {
        ByteBuf bf = Unpooled.buffer();
        tx.write(bf);
        levelDB.put(tx.hash().fill256bit(), ByteUtil.readAll(bf));
    }

    public Optional<RawTransaction> readTransaction(Uint256 hash) {
        byte[] bytes = levelDB.get(hash.fill256bit());
        if (bytes != null) {
            ByteBuf bf = Unpooled.copiedBuffer(bytes);
            RawTransaction rt = new RawTransaction();
            rt.read(bf);
            return Optional.of(rt);
        }
        return Optional.empty();
    }

    public void writeTransaction(ChainBlock cb) {
        RawTransaction[] txs = cb.getTxs();
        ByteBuf bf = Unpooled.buffer();
        for (RawTransaction tx : txs) {
            Uint256 hash = tx.hash();
            TxIn[] ins = tx.getTxIns();
            for (int i = 0; i < ins.length; i++) {
                TxIn in = ins[i];
                Uint256 preHash = in.getPreTxHash();
                byte[] preTx = levelDB.get(preHash.fill256bit());
                try {
                    RawTransaction rt = new RawTransaction();
                    rt.read(Unpooled.copiedBuffer(preTx));
                    TxOut preout = rt.getTxOuts()[in.getPreTxOutIndex()];
                    Interpreter ip = new Interpreter(tx).indexTxIn(i, preout);
                    // TODO :: script
                    ip.executor(in.getScript()).witnessPushStack().executor(preout.getScriptPubKey());
                    Assert.isTrue(ip.isSuccess(), "SCRIPT run error !");
                } catch (Exception ex) {// TODO :: ERROR
                    logger.warn("exchange RawTransaction HASH: {}, PRE-HASH : {}, error", hash, preHash, ex);
                    break;
                }
            }
            byte[] bytes = levelDB.get(hash.fill256bit());
            if (bytes == null) {
                tx.write(bf.clear());
                logger.info("RawTransaction add to level db : {}", hash.toHexReverse256());
                levelDB.put(hash.fill256bit(), ByteUtil.readAll(bf));
            }
        }
    }

    @Override
    public void close() throws IOException {
        levelDB.close();
    }
}
