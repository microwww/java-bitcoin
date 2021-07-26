package com.github.microwww.bitcoin.conf;

import com.github.microwww.bitcoin.store.DiskBlock;
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

@Configuration
public class ChainBlockStore {
    private static final Logger logger = LoggerFactory.getLogger(ChainBlockStore.class);

    @Bean
    public DiskBlock diskBlock(CChainParams params) throws IOException {
        return new DiskBlock(params);
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

    public static File lockupRootDirectory(Settings conf) {
        String prefix = conf.getEnv().params.getDataDirPrefix();
        File root = new File(conf.getDataDir(), prefix);
        try {
            createCanWriteDir(root);
            return root;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param root
     * @return 如果是新增返回 true, 否则 false
     * @throws IOException 如果出错 或者 不可写
     */
    public static boolean createCanWriteDir(File root) throws IOException {
        boolean create = root.mkdirs();
        if (!root.canWrite()) {
            throw new IOException("Not to writer dir : " + root.getCanonicalPath());
        }
        logger.info("scan local block-link data : {}", root.getCanonicalPath());
        return create;
    }
}
