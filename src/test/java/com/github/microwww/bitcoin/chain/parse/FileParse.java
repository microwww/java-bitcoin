package com.github.microwww.bitcoin.chain.parse;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.util.ClassPath;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * 解析bitcoin下面的 blocks/blk00000.dat 文件
 */
class FileParse {

    @Test
    public void testBlock() {
        List<String> file = ClassPath.readClassPathFile("/data/line-data.txt");
        List<ChainBlock> list = new ArrayList<>();
        for (int i = 33; i < 39; i++) {
            ByteBuf buffer = Unpooled.copiedBuffer(ByteUtil.hex(file.get(i)));
            int mage = buffer.readInt();
            Assertions.assertEquals(0xf9beb4d9, mage);
            int len = buffer.readIntLE();
            // Assertions.assertTrue(len <= buffer.readableBytes());
            ChainBlock rawBlock = new ChainBlock().reset(buffer);
            list.add(rawBlock);
        }
        Assertions.assertEquals(list.get(0).hash(), list.get(1).header.getPreHash());
        Assertions.assertEquals(list.get(1).hash(), list.get(2).header.getPreHash());
        Assertions.assertEquals(list.get(2).hash(), list.get(3).header.getPreHash());
        // 有一行数据被跳过
        // Assertions.assertEquals(list.get(3).hash(), list.get(4).getPreHash());
        Assertions.assertEquals(list.get(4).hash(), list.get(5).header.getPreHash());
        // Assertions.assertEquals(list.get(5).hash(), list.get(5).getPreHash());
        Uint256 hash = list.get(3).getTxs()[0].hash();
        Assertions.assertEquals("999e1c837c76a1b7fbb7e57baf87b309960f5ffefbf2a9b95dd890602272f644", hash.toHexReverse256());
    }

    @Test
    @Disabled
    void testParseBLK00000() throws IOException, InterruptedException {
        File file = new File("D:\\Program\\bitcoin-0.21.1\\online\\blocks\\blk00000.dat");
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {

            ByteBuf bf = Unpooled.buffer(10 * 1024 * 1024);
            ByteBuf buffer = Unpooled.buffer(10);
            for (int i = 0; i < 100; i++) {
                byte[] from = new byte[8];
                int r = in.read(from);
                if (r < from.length) {
                    throw new UnsupportedOperationException("文件已经结束, 或者剩余数据太小");
                }
                buffer.clear().writeBytes(from);
                int marge = buffer.readInt();
                int len = buffer.readIntLE();
                byte[] bytes = new byte[len];
                int read = in.read(bytes);
                if (read != len) {
                    throw new RuntimeException("文件已经结束");
                }
                bf.clear().writeBytes(bytes);
                ChainBlock rawBlock = new ChainBlock().reset(bf);
                System.out.println("SKIP CHECK MARGE " + new Uint32(marge).toHex() + ", " + i + ", " + rawBlock.toString());
                Thread.sleep(100);
            }
        }
    }

    @Test
    @Disabled
    void testParseBLK() throws Exception {
        String f = "G:\\bitoin\\data\\blocks\\blk00000.dat";
        RandomAccessFile r = new RandomAccessFile(f, "r");
        FileChannel ch = r.getChannel();
        ByteBuffer bf = ByteBuffer.allocate(1024);
        ByteBuf ub = Unpooled.buffer();
        int position = 0;
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            bf.clear();
            int read = ch.read(bf);
            if (read < 0) {
                break;
            }
            bf.rewind();
            ub.clear();
            while (bf.hasRemaining()) {
                ub.writeBytes(bf);
            }
            byte[] magic = ByteUtil.readLength(ub, 4);
            Assert.assertArrayEquals(new byte[]{(byte) 0xf9, (byte) 0xbe, (byte) 0xb4, (byte) 0xd9}, magic);
            int len = ub.readIntLE();
            while (ub.readableBytes() < len) {
                bf.clear();
                read = ch.read(bf);
                if (read < 0) {
                    return;
                }
                bf.rewind();
                while (bf.hasRemaining()) {
                    ub.writeBytes(bf);
                }
            }
            Assert.assertTrue(ub.readableBytes() >= len);
            Assert.assertTrue(len > 80 && len < 4_000_000);
            position += len + 8;
            ch.position(position);

            // System.out.println(i + ", magic : " + ByteUtil.hex(magic) + ", position " + position + ", len " + len);
            try {
                ChainBlock chainBlock = new ChainBlock().reset(ub);
                System.out.println(chainBlock.hash() + " , " + chainBlock.header.getPreHash());
            } catch (RuntimeException e) {
                ub.readerIndex(0);
                System.out.println(ByteUtil.hex(ByteUtil.readLength(ub, len + 8)));
            }
        }
    }
}
