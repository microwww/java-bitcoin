package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.conf.ChainBlockStore;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.iq80.leveldb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;

@Component
public class TxMemPool implements Closeable {
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
            logger.info("Transaction Index-dir: {}{}txindex", file.getCanonicalPath(), File.separator);
            levelDB = ChainBlockStore.leveldb(file, "txindex", chainParams.settings.isReIndex());
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

    public Optional<RawTransaction> read(Uint256 hash) {
        byte[] bytes = levelDB.get(hash.fill256bit());
        if (bytes != null) {
            ByteBuf bf = Unpooled.copiedBuffer(bytes);
            RawTransaction rt = new RawTransaction();
            rt.read(bf);
            return Optional.of(rt);
        }
        return Optional.empty();
    }

    @Override
    public void close() throws IOException {
        levelDB.close();
    }
}
