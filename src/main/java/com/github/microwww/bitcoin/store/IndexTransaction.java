package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.*;
import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.conf.ChainBlockStore;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.iq80.leveldb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class IndexTransaction implements Closeable {
    private static final String TX_INDEX_DIR = "txindex";
    private static final Logger logger = LoggerFactory.getLogger(IndexTransaction.class);
    private final CChainParams chainParams;
    private final HashSet<RawTransaction> transactions;
    private final int maxCount;
    private final Optional<DB> levelDB;
    @Autowired
    private DiskBlock diskBlock;

    public IndexTransaction(CChainParams chainParams) {
        this.chainParams = chainParams;
        transactions = new LinkedHashSet<>();
        maxCount = chainParams.settings.getTxPoolMax();
        if (chainParams.settings.isTxIndex()) {
            try {
                File file = chainParams.settings.lockupRootDirectory();
                logger.info("Transaction Index-dir: {}{}", file.getCanonicalPath(), TX_INDEX_DIR);
                DB db = ChainBlockStore.leveldb(file, TX_INDEX_DIR, chainParams.settings.isReIndex());
                levelDB = Optional.of(db);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            levelDB = Optional.empty();
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
        // levelDB.ifPresent(e -> e.put(tx.hash().fill256bit(), ByteUtil.readAll(bf)));
    }

    public boolean isSerializable() {
        return chainParams.settings.isTxIndex();
    }

    public void serializationTransaction(FileTransaction... fts) {
        ByteBuf buffer = Unpooled.buffer();
        for (int i = fts.length - 1; i >= 0; i--) {
            FileTransaction ft = fts[i];
            RawTransaction tr = ft.getTransaction();
            buffer.clear();
            this.serializationLevelDB(ft, buffer);
            levelDB.ifPresent(db -> {
                Uint256 hash = tr.hash();
                if (logger.isDebugEnabled())
                    logger.debug("Level-db save transaction : {}", hash);
                db.put(hash.fill256bit(), ByteUtil.readAll(buffer));
            });
        }
    }

    private void serializationLevelDB(FileTransaction ft, ByteBuf buffer) {
        long ps = ft.getPosition();
        buffer.writeIntLE((int) ps);
        buffer.writeIntLE(ft.getLength());
        byte[] bytes = ft.getFile().getName().getBytes(StandardCharsets.UTF_8);
        Assert.isTrue(bytes.length < 127, "File name too length");
        buffer.writeByte(bytes.length);
        buffer.writeBytes(bytes);
    }

    public synchronized Optional<FileTransaction> findTransaction(Uint256 hash) {
        return levelDB.flatMap(e -> {
            byte[] bytes = e.get(hash.fill256bit());
            if (bytes != null) {
                return Optional.of(deserializationLevelDB(bytes));
            }
            return Optional.empty();
        });
    }

    private FileTransaction deserializationLevelDB(byte[] bytes) {
        ByteBuf buffer = Unpooled.copiedBuffer(bytes);
        int ps = buffer.readIntLE();
        int length = buffer.readIntLE();
        int len = buffer.readByte();
        String name = new String(ByteUtil.readLength(buffer, len), StandardCharsets.UTF_8);
        chainParams.getEnvParams().getDataDirPrefix();
        FileTransaction ft = new FileTransaction(new File(diskBlock.getRoot(), name));
        ft.setPosition(ps).setLength(length);
        try {
            ft.setTransaction(ft.readFileRawTransaction());
            return ft;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DiskBlock getDiskBlock() {
        return diskBlock;
    }

    public IndexTransaction setDiskBlock(DiskBlock diskBlock) {
        this.diskBlock = diskBlock;
        return this;
    }

    @Override
    public void close() throws IOException {
        if (levelDB.isPresent()) {
            levelDB.get().close();
        }
    }

    public void verifyTransactions(ChainBlock chainBlock) {
        HeightBlock hb = diskBlock.readBlock(chainBlock.header.getPreHash()).get();
        verifyTransactions(chainBlock, hb.getHeight() + 1);
    }

    public void verifyTransactions(ChainBlock chainBlock, int height) {
        Uint256 hash = chainBlock.hash();
        RawTransaction[] txs = chainBlock.getTxs();
        Assert.isTrue(txs.length > 0, "RawTransaction length > 0");
        RawTransaction first = txs[0];
        long amount = Generating.getBlockSubsidy(height, this.chainParams.env);

        Map<Uint256, RawTransaction> map = new HashMap<>();
        for (RawTransaction tx : txs) { // TODO:: 是否会递归 ?
            // main height: 546,
            // tx-1: 28204cad1d7fc1d199e8ef4fa22f182de6258a3eaafe1bbe56ebdcacd3069a5f
            // tx-2: 6b0f8a73a56c04b519f1883e8aafda643ba61a30bd1439969df21bea5f4e27e2
            map.put(tx.hash(), tx);
        }

        for (int i = 1; i < txs.length; i++) {
            RawTransaction tx = txs[i];

            TxIn[] txIns = tx.getTxIns();
            long fee = 0;
            for (TxIn in : txIns) {
                int index = in.getPreTxOutIndex();
                Optional<FileTransaction> ft = this.findTransaction(in.getPreTxHash());
                RawTransaction preTx;
                if (!ft.isPresent()) {
                    preTx = map.get(in.getPreTxHash());
                    if (preTx == null) {
                        logger.info("Tx error: {}, not find pre-tx: {}, BLOCK: {}, {}", tx.hash(), in.getPreTxHash(), height, hash);
                        throw new IllegalArgumentException("Not find pre-tx: " + in.getPreTxHash());
                    }
                } else {
                    preTx = ft.get().getTransaction();
                }
                TxOut txOut = preTx.getTxOuts()[index];
                long value = txOut.getValue();
                Assert.isTrue(value >= 0, "Amount non-negative");
                fee += value;
            }

            TxOut[] txOuts = tx.getTxOuts();
            for (TxOut out : txOuts) {
                long value = out.getValue();
                Assert.isTrue(value >= 0, "Amount non-negative");
                fee -= value;
            }
            Assert.isTrue(fee >= 0, "Fee > 0, BUT " + fee);

            amount += fee;
        }

        Assert.isTrue(first.getTxIns().length == 1, "Base-coin need one TxIN");
        TxIn in = first.getTxIns()[0];
        Assert.isTrue(in.getPreTxHash().equals(Uint256.ZERO), "Base-coin tx pre-hash : 0000...000");
        Assert.isTrue(in.getPreTxOutIndex() == -1, "Base-coin tx pre-index : -1");

        TxOut[] txOuts = first.getTxOuts();
        for (TxOut out : txOuts) {
            long value = out.getValue();
            Assert.isTrue(value >= 0, "Amount non-negative");
            amount -= value;
        }
        if (amount < 0) {
            Assert.isTrue(amount >= 0, "Base-coin fee >= 0, BUT " + amount);
        }
        if (amount > 0) {
            logger.info("Lose amount {}, {}, Base-coin", amount, hash);
        }
    }
}
