package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.util.ClassPath;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BitcoinServerTest {

    @Test
    public void testLevelDB() throws IOException {
        DBFactory factory = new Iq80DBFactory();
        // 默认如果没有则创建
        Options options = new Options();
        File file = ClassPath.lookupFile("/index");
        if (!file.exists()) {
            file.mkdirs();
        } else {
            if (file.isFile() || !file.canWrite()) {
                throw new IOException("It is a directory and can write !");
            }
        }
        System.out.println(file.getCanonicalPath());
        byte[] key = "Hello".getBytes(StandardCharsets.UTF_8);
        try (DB db = factory.open(file, options);) {
            db.put(key, "world".getBytes(StandardCharsets.UTF_8));
            assertEquals("world", new String(db.get(key)));

            DBIterator iterator = db.iterator();
            for (int i = 0; i < 1000 && iterator.hasNext(); i++) {
                Map.Entry<byte[], byte[]> entry = iterator.next();
                System.out.println(i + ",  " + new String(entry.getKey()) + "  :  " + new String(entry.getValue()));
            }
        }
    }

    @Test
    @Disabled
    public void testFileLevelDB() throws IOException {
        DBFactory factory = new Iq80DBFactory();
        Options options = new Options();
        File file = new File(".......\\txindex");
        try (DB db = factory.open(file, options);) {
            DBIterator iterator = db.iterator();
            for (int i = 0; i < 1000 && iterator.hasNext(); i++) {
                Map.Entry<byte[], byte[]> entry = iterator.next();
                System.out.println(String.format("%03d, %s : %s", i,  ByteUtil.hex(ByteUtil.reverse(entry.getKey())), ByteUtil.hex(entry.getValue())));
            }
        }
    }
}