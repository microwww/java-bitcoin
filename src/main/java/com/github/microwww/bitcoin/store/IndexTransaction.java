package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.*;
import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.conf.ChainBlockStore;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.script.Interpreter;
import com.github.microwww.bitcoin.script.ex.TransactionInvalidException;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.wallet.Wallet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.iq80.leveldb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Component
public class IndexTransaction implements ApplicationListener<FileChainBlock.BlockWrite2fileEvent>, Closeable {
    private static final Logger logger = LoggerFactory.getLogger(IndexTransaction.class);
    private final Map<Uint256, RawTransaction> transactions;
    private final int maxCount;
    private final Optional<DB> levelDB;
    private final DiskBlock diskBlock;
    private final Wallet wallet;
    private final CChainParams chainParams;

    public IndexTransaction(CChainParams chainParams) {
        this(null, null, chainParams);
    }

    @Autowired
    public IndexTransaction(Wallet wallet, DiskBlock diskBlock, CChainParams chainParams) {
        this.diskBlock = diskBlock;
        this.wallet = wallet;
        this.chainParams = chainParams;
        transactions = Collections.synchronizedMap(new LinkedHashMap<>());
        maxCount = chainParams.settings.getTxPoolMax();
        if (chainParams.settings.isTxIndex()) {
            try {
                File file = chainParams.settings.getTxIndexDirectory();
                logger.info("Transaction Index-dir: {}", file.getCanonicalPath());
                DB db = ChainBlockStore.leveldb(file, chainParams.settings.isReIndex());
                levelDB = Optional.of(db);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            levelDB = Optional.empty();
        }
    }

    @Override
    public void onApplicationEvent(FileChainBlock.BlockWrite2fileEvent event) {
        indexTransaction(event.getBitcoinSource());
    }

    public void add(RawTransaction request) {
        // TODO:: 需要验证交易
        if (transactions.size() > maxCount) {
            Uint256 next = transactions.keySet().iterator().next();
            transactions.remove(next);
        }
        transactions.put(request.hash(), request);
        if (request.isWitness()) {
            transactions.put(request.whash(), request);
        }
    }

    public Optional<RawTransaction> findCacheTransaction(Uint256 uint256) {
        return Optional.ofNullable(transactions.get(uint256));
    }

    void indexTransaction(FileChainBlock fc) {
        int magicAndLengthBytes = 8;
        FileTransaction[] fts = this.transactionPosition(fc, magicAndLengthBytes + fc.getPosition());
        this.serializationTransaction(fts);
    }

    public FileTransaction[] transactionPosition(FileChainBlock fc, long offset) {
        ChainBlock chainBlock = fc.getTarget();
        ByteBuf bf = Unpooled.buffer();
        offset += chainBlock.header.bytesLength();
        RawTransaction[] txs = chainBlock.getTxs();
        FileTransaction[] fts = new FileTransaction[txs.length];
        for (int i = 0; i < txs.length; i++) {
            RawTransaction tx = txs[i];
            int ix = bf.writerIndex();
            tx.serialization(bf);

            fts[i] = new FileTransaction(fc.getFile(), ix + offset, bf.writerIndex() - ix, chainBlock.hash(), tx);
        }
        return fts;
    }

    private void serializationTransaction(FileTransaction... fts) {
        ByteBuf buffer = Unpooled.buffer();
        for (int i = fts.length - 1; i >= 0; i--) {
            FileTransaction ft = fts[i];
            RawTransaction tr = ft.getTarget();
            this.serializationLevelDB(ft, buffer);
            Uint256 hash = tr.hash();
            levelDB.ifPresent(db -> {
                if (logger.isDebugEnabled())
                    logger.debug("Level-db save transaction : {}", hash);
                db.put(hash.fill256bit(), ByteUtil.readAll(buffer));
                if (tr.isWitness()) {
                    db.put(tr.whash().fill256bit(), ByteUtil.readAll(buffer));
                }
            });
            buffer.clear();
            wallet.localTransaction(tr);
        }
    }

    public synchronized Optional<RawTransaction> getTransaction(Uint256 hash) {
        Optional<RawTransaction> raw = this.findCacheTransaction(hash);
        if (raw.isPresent()) {
            return raw;
        } else {
            Optional<FileTransaction> ft = findTransaction(hash);
            if (ft.isPresent()) {
                return Optional.of(ft.get().getTarget());
            }
        }
        return Optional.empty();
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

    public void serializationLevelDB(FileTransaction ft, ByteBuf buffer) {
        long ps = ft.getPosition();
        int file = AccessBlockFile.parseIndex(ft.getFile().getName());
        buffer.writeIntLE(file);
        buffer.writeIntLE((int) ps);
        buffer.writeIntLE(ft.getLength());
        buffer.writeBytes(ft.getBlock().fill256bit());
    }

    public FileTransaction deserializationLevelDB(byte[] bytes) {
        ByteBuf buffer = Unpooled.copiedBuffer(bytes);
        int file = buffer.readIntLE();
        int ps = buffer.readIntLE();
        int length = buffer.readIntLE();
        byte[] hash = new byte[32];
        buffer.readBytes(hash);
        File root = chainParams.settings.getBlocksDirectory();
        FileTransaction ft = new FileTransaction(new File(root, String.format(AccessBlockFile.sequenceFile, file)), ps, length, new Uint256(hash));
        ft.target = ft.load(false);
        return ft;
    }

    public DiskBlock getDiskBlock() {
        return diskBlock;
    }

    @Override
    public void close() throws IOException {
        if (levelDB.isPresent()) {
            levelDB.get().close();
        }
    }

    public void verifyTransactions(ChainBlock chainBlock) {
        if (chainBlock.header.getHeight().isPresent()) {
            ChainBlock hb = diskBlock.readBlock(chainBlock.header.getPreHash()).get();
            chainBlock.header.setHeight(hb.getHeight() + 1);
        }
        verifyTransactionsWithHeight(chainBlock);
    }

    private void verifyTransactionsWithHeight(ChainBlock chainBlock) {
        int height = chainBlock.getHeight();
        Uint256 hash = chainBlock.hash();
        RawTransaction[] txs = chainBlock.getTxs();
        Assert.isTrue(txs.length > 0, "RawTransaction length > 0");
        RawTransaction first = txs[0];
        long amount = Generating.getBlockSubsidy(height, this.chainParams.env);

        // 一个块 中有两个关联的交易
        // main height: 546,
        // tx-1: 28204cad1d7fc1d199e8ef4fa22f182de6258a3eaafe1bbe56ebdcacd3069a5f
        // tx-2: 6b0f8a73a56c04b519f1883e8aafda643ba61a30bd1439969df21bea5f4e27e2
        Map<Uint256, RawTransaction> map = new HashMap<>();
        for (RawTransaction tx : txs) {
            map.put(tx.hash(), tx);
        }

        for (int i = 1; i < txs.length; i++) {
            RawTransaction tx = txs[i];

            TxIn[] txIns = tx.getTxIns();
            long fee = 0;
            for (int inIndex = 0; inIndex < txIns.length; inIndex++) {
                TxIn in = txIns[inIndex];
                Uint256 txh = tx.hash();
                int outIndex = in.getPreTxOutIndex();
                Optional<FileTransaction> ft = this.findTransaction(in.getPreTxHash());
                RawTransaction preTx;
                if (!ft.isPresent()) {
                    preTx = map.get(in.getPreTxHash()); // TODO:: 是否会递归 ?
                    if (preTx == null) {
                        logger.info("Tx error: {}, not find pre-tx: {}, BLOCK: {}, {}", txh, in.getPreTxHash(), height, hash);
                        throw new IllegalArgumentException("Not find pre-tx: " + in.getPreTxHash());
                    }
                } else {
                    preTx = ft.get().getTarget();
                }

                TxOut txOut = preTx.getTxOuts()[outIndex];
                if (logger.isDebugEnabled()) {
                    logger.debug("Run tx script, {}, script in: {}, out: {}", hash, ByteUtil.hex(in.getScript()), ByteUtil.hex(txOut.getScriptPubKey()));
                }
                verifyScript(chainBlock, tx, inIndex, txOut);

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

    public void verifyScript(ChainBlock block, RawTransaction tx, int inIndex, TxOut txOut) {
        try {
            this.tryVerifyScript(block, tx, inIndex, txOut);
        } catch (RuntimeException e) {
            Uint256 txh = tx.hash();
            TxIn in = tx.getTxIns()[inIndex];
            logger.error("Tx script run error, {}, in: {} \n {} \n {}", txh, inIndex, ByteUtil.hex(in.getScript()), ByteUtil.hex(txOut.getScriptPubKey()), e);
            throw e;
        }
    }

    public void tryVerifyScript(ChainBlock block, RawTransaction tx, int inIndex, TxOut txOut) {
        Uint256 txh = tx.hash();
        Interpreter interpreter = new Interpreter(tx, block).indexTxIn(inIndex, txOut).witnessPushStack()
                .executor(tx.getTxIns()[inIndex].getScript())
                .executor(txOut.getScriptPubKey());

        if (!interpreter.isSuccess()) {
            throw new TransactionInvalidException("Tx in: " + inIndex + ", TX: " + txh);
        }
    }
}
