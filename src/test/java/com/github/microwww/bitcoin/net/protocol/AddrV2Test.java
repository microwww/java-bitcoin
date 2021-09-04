package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.provider.Peer;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.util.ClassPath;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AddrV2Test {
    //0161eb32610d010405093b9a208d
    //0274e93261fd090401045241fd3c208d58eb3261fd0904010403881dc5208d
    // 01dde93261fd0904010455d6a6e4208d
    @Test
    void write0read0() {
        // private static final String addrv2_1000
        this.write0read0("0171f33261fd4d040104b9ee8030208d", 1);
        this.write0read0("0161eb32610d010405093b9a208d", 1);
        this.write0read0("0274e93261fd090401045241fd3c208d58eb3261fd0904010403881dc5208d", 2);
        this.write0read0("01dde93261fd0904010455d6a6e4208d", 1);
        this.write0read0("01b3f132610d01045e82542c208d", 1);
        String line3 = ClassPath.readClassPathFile("/data/line-data.txt").get(110);
        AddrV2 addrV2 = this.write0read0(line3, 1000);
    }

    AddrV2 write0read0(String hexBytes, int count) {
        AddrV2 test = new AddrV2(new Peer(null, "test", 8333));
        byte[] hex = ByteUtil.hex(hexBytes);
        test.read(hex);
        assertEquals(count, test.getNodes().length);
        ByteBuf bf = Unpooled.buffer();
        test.write(bf);
        byte[] bytes = ByteUtil.readAll(bf);
        assertArrayEquals(bytes, hex);
        return test;
    }

    /**
     * src/test/netbase_tests.cpp
     */
    public static final String stream_addrv1_hex =
            "03" // number of entries

                    + "61bc6649"                         // time, Fri Jan  9 02:54:25 UTC 2009
                    + "0000000000000000"                 // service flags, NODE_NONE
                    + "00000000000000000000000000000001" // address, fixed 16 bytes (IPv4 embedded in IPv6)
                    + "0000"                             // port

                    + "79627683"                         // time, Tue Nov 22 11:22:33 UTC 2039
                    + "0100000000000000"                 // service flags, NODE_NETWORK
                    + "00000000000000000000000000000001" // address, fixed 16 bytes (IPv6)
                    + "00f1"                             // port

                    + "ffffffff"                         // time, Sun Feb  7 06:28:15 UTC 2106
                    + "4804000000000000"                 // service flags, NODE_WITNESS | NODE_COMPACT_FILTERS | NODE_NETWORK_LIMITED
                    + "00000000000000000000000000000001" // address, fixed 16 bytes (IPv6)
                    + "f1f2";                            // port

    // fixture_addresses should equal to this when serialized in V2 format.
// When this is unserialized from V2 format it should equal to fixture_addresses.
    public static final String stream_addrv2_hex =
            "03" // number of entries

                    + "61bc6649"                         // time, Fri Jan  9 02:54:25 UTC 2009
                    + "00"                               // service flags, COMPACTSIZE(NODE_NONE)
                    + "02"                               // network id, IPv6
                    + "10"                               // address length, COMPACTSIZE(16)
                    + "00000000000000000000000000000001" // address
                    + "0000"                             // port

                    + "79627683"                         // time, Tue Nov 22 11:22:33 UTC 2039
                    + "01"                               // service flags, COMPACTSIZE(NODE_NETWORK)
                    + "02"                               // network id, IPv6
                    + "10"                               // address length, COMPACTSIZE(16)
                    + "00000000000000000000000000000001" // address
                    + "00f1"                             // port

                    + "ffffffff"                         // time, Sun Feb  7 06:28:15 UTC 2106
                    + "fd4804"                           // service flags, COMPACTSIZE(NODE_WITNESS | NODE_COMPACT_FILTERS | NODE_NETWORK_LIMITED)
                    + "02"                               // network id, IPv6
                    + "10"                               // address length, COMPACTSIZE(16)
                    + "00000000000000000000000000000001" // address
                    + "f1f2";                            // port

}