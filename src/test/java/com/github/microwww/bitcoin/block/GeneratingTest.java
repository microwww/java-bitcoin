package com.github.microwww.bitcoin.block;

import cn.hutool.crypto.digest.DigestUtil;
import com.github.microwww.bitcoin.util.ByteUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeneratingTest {

    @Test
    @Disabled
    void createGenesisBlock() {
        new Generating().createGenesisBlock();
    }

    @Test
    void txHash() {
        String tx = "020000000001019fe5681c625e3bb1d4e29150a5cc92aaa191aa93bdee0307e900c6db18fb209a0000000000feffffff0233da81230100000016001421185ecb0abecc80845974e08e8931fb5e152a6f40178406000000001600149ceb4b5e32db0c790ca1e01369144cdeb740fb900247304402200c879ebdd4d6f50ebf5a7a7f714d2236f17516e34a2f7d470a512f64a57346d502207c2402bad51c161b03e5a86e0870db4f6790880f0cbc1c033e0e681f16acdc6c0121038e0e1ba1962b2a6a549e306872df52a518dc160cf47ca4c73444c864f9efbc6e00000000";
        byte[] hex = ByteUtil.hex(tx);
        System.out.println(ByteUtil.hex(DigestUtil.sha256(hex)));
        // f8e141bf0a9e7c323c3c6a45ba4d7899ba7f109e38ca12d0bc395aa9003e81b8
    }
}