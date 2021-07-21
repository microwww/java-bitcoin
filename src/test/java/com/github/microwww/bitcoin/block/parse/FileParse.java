package com.github.microwww.bitcoin.block.parse;

import com.github.microwww.bitcoin.util.ClassPath;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.*;

/**
 * 解析bitcoin下面的 blocks/blk00000.dat 文件
 */
public class FileParse {

    public static void main(String[] args) throws IOException {
        File file = ClassPath.findFile("/data/blk00000.dat");
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {

            // ByteBuf buffer = Unpooled.buffer(1024);
            for (int i = 0; i < 1000000; i++) {
                byte[] from = new byte[8];
                int r = in.read(from);
                if (r < from.length) {
                    throw new UnsupportedOperationException("文件已经结束, 或者剩余数据太小");
                }
                ByteBuf buffer = Unpooled.copiedBuffer(from);
                int mage = buffer.readInt();
                if (mage != 0xfabfb5da) { // 0xf9beb4d9
                    throw new UnsupportedOperationException("不支持的前缀类型: 0x" + Integer.toUnsignedString(mage) + ", 剩余数据: " + in.available());
                }
                int len = buffer.readIntLE();
                byte[] bytes = new byte[len];
                int read = in.read(bytes);
                if (read != len) {
                    throw new RuntimeException("文件已经结束");
                }
                ByteBuf bf = Unpooled.copiedBuffer(bytes);
                BlockFile.RawBlock rawBlock = new BlockFile.RawBlock();
                rawBlock.read(bf);
                System.out.println(i + "" + rawBlock);
            }
        }
    }

}