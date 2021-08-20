package com.github.microwww.bitcoin.conf;

import com.github.microwww.bitcoin.store.DiskBlock;
import com.github.microwww.bitcoin.wallet.Wallet;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

@Configuration
public class ChainBlockStore {
    private static final Logger logger = LoggerFactory.getLogger(ChainBlockStore.class);

    @Bean
    public DiskBlock diskBlock(CChainParams params) throws IOException {
        return new DiskBlock(params);
    }

    @Bean
    public Wallet wallet(CChainParams params) {
        try {
            Wallet wallet = new Wallet(params.settings.lockupRootDirectory());
            wallet.init();
            return wallet;
        } catch (SQLException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static DB leveldb(File root, String dir, boolean clear) throws IOException {
        File file = new File(root, dir);
        if (clear) {
            file.deleteOnExit();
        }
        return leveldb(root, dir);
    }

    public static DB leveldb(File root, String dir) throws IOException {
        File file = new File(root, dir);
        if (!file.exists()) {
            file.mkdirs();
        } else {
            if (file.isFile() || !file.canWrite()) {
                throw new IOException("It is a directory and can write !");
            }
        }
        DBFactory factory = new Iq80DBFactory();
        // 默认如果没有则创建
        Options options = new Options();
        return factory.open(file, options);
    }
}
