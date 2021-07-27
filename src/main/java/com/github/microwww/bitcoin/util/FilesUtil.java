package com.github.microwww.bitcoin.util;

import java.io.File;
import java.io.IOException;

public class FilesUtil {
    private FilesUtil() {
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
        return create;
    }

    public static boolean createCanWriteFile(File file) throws IOException {
        boolean create = file.createNewFile();
        if (!file.canWrite()) {
            throw new IOException("Not to writer dir : " + file.getCanonicalPath());
        } else if (file.isDirectory()) {
            throw new IOException("File do not allow a directory : " + file.getCanonicalPath());
        }
        return create;
    }
}
