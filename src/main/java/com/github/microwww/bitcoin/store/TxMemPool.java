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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    private final Optional<DB> levelDB;
    @Autowired
    private DiskBlock diskBlock;

    public TxMemPool(CChainParams chainParams) {
        this.chainParams = chainParams;
        transactions = new LinkedHashSet<>();
        maxCount = chainParams.settings.getTxPoolMax();
        if (chainParams.settings.isTxIndex()) {
            try {
                File file = chainParams.settings.lockupRootDirectory();
                logger.info("Transaction Index-dir: {}{}{}", file.getCanonicalPath(), File.separator, TX_INDEX_DIR);
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
            levelDB.ifPresent(e -> {
                Uint256 hash = tr.hash();
                if (logger.isDebugEnabled())
                    logger.debug("Level-db save transaction : {}", hash.toHexReverse256());
                e.put(hash.fill256bit(), ByteUtil.readAll(buffer));
            });
        }
    }

    public void serializationLevelDB(FileTransaction ft, ByteBuf buffer) {
        long ps = ft.getPosition();
        buffer.writeIntLE((int) ps);
        buffer.writeIntLE(ft.getLength());
        byte[] bytes = ft.getFile().getName().getBytes(StandardCharsets.UTF_8);
        Assert.isTrue(bytes.length < 127, "File name too length");
        buffer.writeByte(bytes.length);
        buffer.writeBytes(bytes);
    }

    public Optional<FileTransaction> deserializationTransaction(Uint256 hash) {
        return levelDB.flatMap(e -> {
            byte[] bytes = e.get(hash.fill256bit());
            if (bytes != null) {
                return Optional.of(deserializationLevelDB(bytes));
            }
            return Optional.empty();
        });
    }

    public FileTransaction deserializationLevelDB(byte[] bytes) {
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

    public TxMemPool setDiskBlock(DiskBlock diskBlock) {
        this.diskBlock = diskBlock;
        return this;
    }

    @Override
    public void close() throws IOException {
        if (levelDB.isPresent()) {
            levelDB.get().close();
        }
    }
}
