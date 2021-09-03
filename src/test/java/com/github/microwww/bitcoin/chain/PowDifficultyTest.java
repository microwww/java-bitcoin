package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PowDifficultyTest {

    @Test
    void difficultyUncompress() {
        BigInteger bits = PowDifficulty.difficultyUncompress(new Uint32(0x1903a30c));
        assertEquals("3A30C00000000000000000000000000000000000000000000", bits.toString(16).toUpperCase());
    }

    @Test
    void difficultyCompress() {
        // 3A30C0000 00000000 00000000 00000000 00000000 00000000
        // 0x1903a30c
        Uint32 uint32 = PowDifficulty.difficultyCompress(new BigInteger("3A30C00000000000000000000000000000000000000000000", 16));
        assertEquals("1903a30c", uint32.toHex());
        //  98785 | 0x1b04864c, 000000000004864c000000000000000000000000000000000000000000000000
        uint32 = PowDifficulty.difficultyCompress(new BigInteger("4864c000000000000000000000000000000000000000000000000", 16));
        assertEquals("1b04864c", uint32.toHex());
        // 106849 | 0x1b028552, 0000000000028552000000000000000000000000000000000000000000000000
        uint32 = PowDifficulty.difficultyCompress(new BigInteger("28552000000000000000000000000000000000000000000000000", 16));
        assertEquals("1b028552", uint32.toHex());
        // 116929 | 0x1b00cbbd, 000000000000cbbd000000000000000000000000000000000000000000000000
        uint32 = PowDifficulty.difficultyCompress(new BigInteger("cbbd000000000000000000000000000000000000000000000000", 16));
        //assertEquals("1b00cbbd", uint32.toHex());
        assertEquals("1b00cbbd", uint32.toHex());
    }
    @Test
    void testCalculation() throws ParseException {
        {
            // 0x000000004f2886a170adb7204cb0c7a824217dd24d11a74423d564c4e0904967, 0x1d00d86a
            //  2016 | 30240 -> 32255 , 12-18-2009 17:56:01 -> 12-30-2009 13:58:59
            Uint32 u = new Uint32(0x1d00ffff);
            long t1 = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").parse("12-18-2009 17:56:01").getTime() / 1000;
            long t2 = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").parse("12-30-2009 13:58:59").getTime() / 1000;
            Uint32 c = PowDifficulty.timespan(u, t2 - t1);
            Assertions.assertEquals("0x1d00d86a", c.toString());
        }
        {
            // 0x000000000a20ed505ca567ec4611616790a1e3c49bc9bdd82ff6bd773e32db2a, 0x1c0ae493
            // 62496 06-24-2010 20:27:26
            // 64511 07-06-2010 09:53:24
            Uint32 u = new Uint32(0x1c0d3142);
            long t1 = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").parse("06-24-2010 20:27:26").getTime() / 1000;
            long t2 = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").parse("07-06-2010 09:53:24").getTime() / 1000;
            Uint32 c = PowDifficulty.timespan(u, t2 - t1);
            Assertions.assertEquals("0x1c0ae493", c.toString());
        }
        {
            // 0x000000000000a8c2cc7f45568c20d3498eba889a182dd72db10d7e7a98fb9f97, 0x1b00f339
            // 112896 03-09-2011 23:25:55
            // 114911 03-25-2011 10:34:45
            Uint32 u = new Uint32(0x1b00dc31);
            long t1 = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").parse("03-09-2011 23:25:55").getTime() / 1000;
            long t2 = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").parse("03-25-2011 10:34:45").getTime() / 1000;
            Uint32 c = PowDifficulty.timespan(u, t2 - t1);
            Assertions.assertEquals("0x1b00f339", c.toString());
        }
        {
            // 0x0000000000043c4b0ed186109c36ce09e9af29b8ed0fdebcb909fa338877743d, 0x1b04864c
            // 96768 12-10-2010 06:20:02
            // 98783 12-22-2010 02:33:13
            Uint32 u = new Uint32(0x1b055953);
            long t1 = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").parse("12-10-2010 06:20:02").getTime() / 1000;
            long t2 = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").parse("12-22-2010 02:33:13").getTime() / 1000;
            Uint32 c = PowDifficulty.timespan(u, t2 - t1);
            Assertions.assertEquals("0x1b04864c", c.toString());
        }
        {
            // 00000000f037ad09d0b05ee66b8c1da83030abaf909d2b1bf519c3c7d2cd3fdf
            // 2016 01-27-2009 21:38:51
            // 4031 02-13-2009 02:58:42
            Uint32 u = new Uint32(0x1d00ffff);
            long t1 = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").parse("01-27-2009 21:38:51").getTime() / 1000;
            long t2 = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").parse("02-13-2009 02:58:42").getTime() / 1000;
            Uint32 c = PowDifficulty.timespan(u, t2 - t1);
            Assertions.assertEquals("0x1d00ffff", c.toString());
        }
    }

    @Test
    public void testNextWorkRequired() {
        // block 112912 , 000000000000a8c2cc7f45568c20d3498eba889a182dd72db10d7e7a98fb9f97
        byte[] H112912 = ByteUtil.hex("010000002ff2886b50bfe876e5a17234218f64c09d1e187ffe5965f408800000000000004940812bd1dbe968fce79dd48a35142093256c8c66b8edfa58512df2efbc441c71008c4d39f3001b1acb68d200");
        // 0000000000008008f46559fe7f181e9dc0648f213472a1e576e8bf506b88f22f
        byte[] H112911 = ByteUtil.hex("010000005997fb4a7fe78d680beddc76dce2e4dee223f5dd167b0119e80100000000000096d047aeff0c873e2e9a51bf688d439530afafa82fb7ccbb86d7f743e4daa79845ff8b4d31dc001bf06541a000");
        // 000000000000cb6aeb7e251ed31a814d9631b5fe3d6994e00229f7ebbfae5344
        byte[] H112896 = ByteUtil.hex("01000000961f982c7914224a9b5293f810cc8e02d457179a64cff43fbb37000000000000e58aa2dedb2ecf091eb0779407be6477abca61ff3d8963a3b73cbed0e66ddf2a039c774d31dc001bef5b6d5400");

        ByteBuf bf = Unpooled.buffer().writeBytes(H112896);
        ChainBlock start = new ChainBlock().readHeader(bf).readBody(bf);
        assertArrayEquals(start.hash().reverse256bit(), ByteUtil.hex("000000000000cb6aeb7e251ed31a814d9631b5fe3d6994e00229f7ebbfae5344"));
        bf.clear().writeBytes(H112911);
        ChainBlock end = new ChainBlock().readHeader(bf).readBody(bf);
        assertArrayEquals(end.hash().reverse256bit(), ByteUtil.hex("0000000000008008f46559fe7f181e9dc0648f213472a1e576e8bf506b88f22f"));
        bf.clear().writeBytes(H112912);
        ChainBlock target = new ChainBlock().readHeader(bf).readBody(bf);
        assertArrayEquals(target.hash().reverse256bit(), ByteUtil.hex("000000000000a8c2cc7f45568c20d3498eba889a182dd72db10d7e7a98fb9f97"));

        Uint32 nextWorkRequired = PowDifficulty.nextWorkRequired(new ChainHeight(112911, target), () -> start);
        assertEquals(nextWorkRequired, target.header.getBits());
        assertEquals("0x1b00f339", nextWorkRequired.toString());
    }
}

/**
 * 第一个块文件 blk00000.dat 中的难度变化 的历史
 * 0x000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f, 0x1d00ffff
 * 0x000000004f2886a170adb7204cb0c7a824217dd24d11a74423d564c4e0904967, 0x1d00d86a
 * 0x000000002732d387256b57cabdcb17767e3d30e220ea73f844b1907c0b5919ea, 0x1d00c428
 * 0x0000000040514b192e6ca247d83388bf11cb1d5e980610ae2c6324cbb0594b32, 0x1d00be71
 * 0x0000000015bb50096055846954f7120e30d6aa2bd5ab8d4a4055ceacc853328a, 0x1d008cc3
 * 0x0000000045861e169b5a961b7034f8de9e98022e7a39100dde3ae3ea240d7245, 0x1c654657
 * 0x000000000b0078371753a9813d3f902008cec296451cf5d721e4660a2fba9612, 0x1c43b3e5
 * 0x0000000026cdac72c9b2eb5ac20d987f7cb30118203528245a34ba1dc0f0d7a3, 0x1c387f6f
 * 0x0000000027c99f9712286d6f3bd01f4d6de7b8bcce586c92f7a131b96d5d96c6, 0x1c381375
 * 0x000000000f8bad9641203f9d788e72d55ff164f1a62a3e230df6f411f7d7d8fc, 0x1c2a1115
 * 0x000000001af6e66db5628c7ba98501623bd23ea6d720571ffedf007973be07c9, 0x1c20bca7
 * 0x00000000079f22d04081de2b3961e7bb1037051df3d178a4bc3258dae9a3ab63, 0x1c16546f
 * 0x00000000021df73bddc56ff2bae4637099c096a1eba98ae631623d21b58877d6, 0x1c13ec53
 * 0x00000000051a16879d044e2f58b60ecaedc9dc92a00416f9035ccc018e72f4ed, 0x1c159c24
 * 0x0000000008c7b770615ca86782c41b97630c8fa23de40cabf3a2722224f9b799, 0x1c0f675c
 * 0x000000000632e22ce73ed38f46d5b408ff1cff2cc9e10daaf437dfd655153837, 0x1c0eba64
 * 0x00000000025825d92ecf024c9055af90f2fe92f5796aaffc61a3d95574bb3b76, 0x1c0d3142
 * 0x000000000a20ed505ca567ec4611616790a1e3c49bc9bdd82ff6bd773e32db2a, 0x1c0ae493
 * 0x0000000002216521d1a33de5bf19b3f8966395fbc81be449e2b2f1bdab2bd88f, 0x1c05a3f4
 * 0x0000000000519051eb5f3c5943cdbc176a0eff4e1fbc3e08287bdb76299b8e5c, 0x1c0168fd
 * 0x0000000000c4f919e27398ec17fe39ca0e8936596e7eeeb21f06d87e851df90e, 0x1c010c5a
 * 0x00000000002897d721b6b31ad519e3e54a90741a24a019f23e41911a8c8b0d25, 0x1c00ba18
 * 0x0000000000365da40798b3cea463f8307ff8b7bf828254721a7d4d270a35ee62, 0x1c00800e
 * 0x00000000002d3c0657d352f28051fbfffda76ce99ddf5256dddebf4c9ee6cad4, 0x1b692098
 * 0x00000000005174bca5fb1486b0cf216e2841560cd28db35839658a3583709983, 0x1b5bede6
 * 0x0000000000307c80b87edf9f6a0697e2f01db67e518c8a4d6065d1d859a3a659, 0x1b4766ed
 * 0x000000000024fc69f5415908b1960092a8e81b9d3b9a03c1133f5cb0a2d3c2af, 0x1b31b2a3
 * 0x000000000006bee26b7ce695f7324381b48361ddde16c6922867e665a0df8430, 0x1b2f8e9d
 * 0x000000000015bfe777e893c4ebd1307541792630c2932278bfe8cf3ae82668ce, 0x1b1e7eca
 * 0x000000000012384edfbd167c7778aec3e84bb1795b907cc795912e643c2cff04, 0x1b153263
 * 0x0000000000042751f341ee2dde050240cd8330401063dfdea21be270495bf360, 0x1b0e7256
 * 0x00000000000615837a43bdec942b048bd28d48aba5d73b0c79617ccc71d56e68, 0x1b098b2a
 * 0x00000000000612274477875b7ceb71488c060f9f718435122e0369a720c5f78f, 0x1b081cd2
 * 0x00000000000481ef73efe6cb2ad1e9c1e3d1c46e712f6aeb6d5079b539d12246, 0x1b055953
 * 0x0000000000043c4b0ed186109c36ce09e9af29b8ed0fdebcb909fa338877743d, 0x1b04864c
 * 0x000000000000e383d43cc471c64a9a4a46794026989ef4ff9611d5acb704e47a, 0x1b0404cb
 * 0x0000000000036e1b1c66c51814ee2f500f3e67d8e568f5606b91ad3359e68c0d, 0x1b038dee
 * 0x0000000000018cfcff9528bdb69f53cce09e9705ed2681bb68f441b27b64cfa4, 0x1b02fa29
 * 0x000000000000be2bc4445d0c929adaa79e6106528b8cc8651f1a0bba37bc0c1b, 0x1b028552
 * 0x00000000000199206616e77986c26e500819a323feb51799b57c6fa6fbbe9850, 0x1b01cc26
 * 0x0000000000004169640bb33c01f4417aefda88a1fabfa4f06f331d823df95fa2, 0x1b012dcd
 * 0x000000000000cb6aeb7e251ed31a814d9631b5fe3d6994e00229f7ebbfae5344, 0x1b00dc31
 * 0x000000000000a8c2cc7f45568c20d3498eba889a182dd72db10d7e7a98fb9f97, 0x1b00f339
 * 0x000000000000494457221111a6a99da34a993c69c2761582bec31f90ba5d560d, 0x1b00cbbd
 * 0x0000000000006995e63e3533e1cc04d0d276fca4cab6241387e6e385da004c88, 0x1b00b5ac
 */