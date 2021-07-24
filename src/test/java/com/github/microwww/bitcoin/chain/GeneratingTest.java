package com.github.microwww.bitcoin.chain;

import cn.hutool.crypto.digest.DigestUtil;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.util.ByteUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GeneratingTest {

    @Test
    void createGenesisBlock() {
        // REG-TEST-NET
        ChainBlock genesisBlock;
        genesisBlock = Generating.createGenesisBlock(new Uint32(1296688602), new Uint32(2), new Uint32(0x207fffff), 1, 50 * Generating.COIN);
        Assertions.assertEquals("0f9188f13cb7b2c71f2a335e3a4fc328bf5beb436012afca590b1a11466e2206", genesisBlock.hash().toHexReverse256());
        Assertions.assertEquals("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b", genesisBlock.getTxs()[0].hash().toHexReverse256());
        Assertions.assertEquals("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b", genesisBlock.header.getMerkleRoot().toHexReverse256());

        // MAIN-NET
        genesisBlock = Generating.createGenesisBlock(new Uint32(1231006505), new Uint32(2083236893), new Uint32(0x1d00ffff), 1, 50 * Generating.COIN);
        Assertions.assertEquals("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f", genesisBlock.hash().toHexReverse256());
        Assertions.assertEquals("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b", genesisBlock.getTxs()[0].hash().toHexReverse256());
        Assertions.assertEquals("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b", genesisBlock.header.getMerkleRoot().toHexReverse256());

        // TEST-NET
        genesisBlock = Generating.createGenesisBlock(new Uint32(1296688602), new Uint32(414098458), new Uint32(0x1d00ffff), 1, 50 * Generating.COIN);
        Assertions.assertEquals("000000000933ea01ad0ee984209779baaec3ced90fa3f408719526f8d77f4943", genesisBlock.hash().toHexReverse256());
        Assertions.assertEquals("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b", genesisBlock.getTxs()[0].hash().toHexReverse256());
        Assertions.assertEquals("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b", genesisBlock.header.getMerkleRoot().toHexReverse256());

        // ByteBuf buffer = Unpooled.buffer();
        // genesisBlock.writeHeader(buffer).writeBody(buffer);
        // String hex = ByteUtil.hex(ByteUtil.readAll(buffer));
        // System.out.println(hex);
        // genesis = CreateGenesisBlock(1231006505, 2083236893, 0x1d00ffff, 1, 50 * COIN);
    }

    @Test
    void txHash() {
        String tx = "020000000001019fe5681c625e3bb1d4e29150a5cc92aaa191aa93bdee0307e900c6db18fb209a0000000000feffffff0233da81230100000016001421185ecb0abecc80845974e08e8931fb5e152a6f40178406000000001600149ceb4b5e32db0c790ca1e01369144cdeb740fb900247304402200c879ebdd4d6f50ebf5a7a7f714d2236f17516e34a2f7d470a512f64a57346d502207c2402bad51c161b03e5a86e0870db4f6790880f0cbc1c033e0e681f16acdc6c0121038e0e1ba1962b2a6a549e306872df52a518dc160cf47ca4c73444c864f9efbc6e00000000";
        byte[] hex = ByteUtil.hex(tx);
        System.out.println(ByteUtil.hex(DigestUtil.sha256(hex)));
        // f8e141bf0a9e7c323c3c6a45ba4d7899ba7f109e38ca12d0bc395aa9003e81b8
    }
}