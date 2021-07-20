package com.github.microwww.bitcoin.block;

import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.net.protocol.Tx;

import java.util.List;

public class BlockHeader {
    int nVersion;
    Uint256 hashPrevBlock;
    Uint256 hashMerkleRoot;
    int nTime;
    int nBits;
    int nNonce;

    // byte txCount;
    List<Tx> txs;

}
