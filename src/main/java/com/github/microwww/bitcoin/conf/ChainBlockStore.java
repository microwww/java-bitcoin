package com.github.microwww.bitcoin.conf;

import com.github.microwww.bitcoin.store.DiskBlock;
import com.github.microwww.bitcoin.util.FilesUtil;
import com.github.microwww.bitcoin.wallet.Wallet;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

@Configuration
public class ChainBlockStore {
    private static final Logger logger = LoggerFactory.getLogger(ChainBlockStore.class);

    @Bean
    public DiskBlock diskBlock(CChainParams params, ApplicationEventPublisher publisher) throws IOException {
        return new DiskBlock.SpringDiskBlock(params, publisher);
    }

    @Bean
    public Wallet wallet(CChainParams params) {
        return Wallet.wallet(params);
    }

    public static DB leveldb(File root, boolean clear) throws IOException {
        if (clear) {
            FilesUtil.deleteRecursively(root);
        }
        return leveldb(root);
    }

    public static DB leveldb(File file) throws IOException {
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
