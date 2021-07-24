package com.github.microwww.bitcoin.conf;

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
import java.io.RandomAccessFile;

@Configuration
public class ChainBlockStore {
    private static final Logger logger = LoggerFactory.getLogger(ChainBlockStore.class);

    @Bean(destroyMethod = "close")
    public DB leveldb(Settings config) throws IOException {
        File ro = ChainBlockStore.loadLocalFile(config);
        File file = new File(ro, "index");
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

    public static File loadLocalFile(Settings conf) {
        String prefix = conf.getEnv().getDataDirPrefix();
        File root = new File(conf.getDataDir(), prefix);
        try {
            root.mkdirs();
            if (!root.canWrite()) {
                throw new RuntimeException("Not to writer dir : " + root.getCanonicalPath());
            }
            logger.info("scan local block-link data : {}", root.getCanonicalPath());
            File lock = new File(root, "lock");
            lock.createNewFile();
            new RandomAccessFile(lock, "rw").getChannel().lock();
            return root;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
