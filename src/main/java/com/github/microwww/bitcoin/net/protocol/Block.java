package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;

import java.util.List;

public class Block {
    private Uint32 time;
    private Uint32 nonce;
    private Uint32 bits;
    private int version;
    private List<Tx> txs;

    private Uint256 hashPrevBlock;
    private Uint256 hashMerkleRoot;

    public Uint32 getTime() {
        return time;
    }

    public void setTime(Uint32 time) {
        this.time = time;
    }

    public Uint32 getNonce() {
        return nonce;
    }

    public void setNonce(Uint32 nonce) {
        this.nonce = nonce;
    }

    public Uint32 getBits() {
        return bits;
    }

    public void setBits(Uint32 bits) {
        this.bits = bits;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public List<Tx> getTxs() {
        return txs;
    }

    public void setTxs(List<Tx> txs) {
        this.txs = txs;
    }

    public Uint256 getHashPrevBlock() {
        return hashPrevBlock;
    }

    public void setHashPrevBlock(Uint256 hashPrevBlock) {
        this.hashPrevBlock = hashPrevBlock;
    }

    public Uint256 getHashMerkleRoot() {
        return hashMerkleRoot;
    }

    public void setHashMerkleRoot(Uint256 hashMerkleRoot) {
        this.hashMerkleRoot = hashMerkleRoot;
    }

    public static Uint256 blockMerkleRoot(Block block) {
        throw new UnsupportedOperationException();
    }
}
