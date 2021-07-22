package com.github.microwww.bitcoin.chain.parse;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.util.ClassPath;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析bitcoin下面的 blocks/blk00000.dat 文件
 */
public class FileParse {

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
            ChainBlock rawBlock = new ChainBlock();
            rawBlock.readHeader(buffer);
            rawBlock.readBody(buffer);
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

}