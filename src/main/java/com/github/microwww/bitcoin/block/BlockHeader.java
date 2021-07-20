package com.github.microwww.bitcoin.block;

import com.github.microwww.bitcoin.math.MerkleTree;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;

import java.util.Collections;
import java.util.List;

public class BlockHeader {
    private int version;
    private Uint256 hashPrevBlock;
    private Uint256 hashMerkleRoot;
    private Uint32 time;
    private Uint32 bits; // 难度
    private Uint32 nonce;

    // byte txCount;
    private List<Transaction> txs = Collections.emptyList();

    public BlockHeader() {
    }

    public Uint256 hash() {
        throw new UnsupportedOperationException();
    }

    /**
     * 静态方法二不是普通方法是为了避免和属性产生冲突, 后续可能为修改该方法的位置
     *
     * @param block
     * @return
     */
    public static Uint256 blockMerkleRoot(BlockHeader block) {
        MerkleTree<Transaction, Uint256> tree = MerkleTree.merkleTree(block.getTxs(), e -> e.hash(), (e1, e2) -> new Uint256(e1.sha256sha256(e2)));
        return tree.getHash();
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
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

    public Uint32 getTime() {
        return time;
    }

    public void setTime(Uint32 time) {
        this.time = time;
    }

    public Uint32 getBits() {
        return bits;
    }

    public void setBits(Uint32 bits) {
        this.bits = bits;
    }

    public Uint32 getNonce() {
        return nonce;
    }

    public void setNonce(Uint32 nonce) {
        this.nonce = nonce;
    }

    public List<Transaction> getTxs() {
        return txs;
    }

    public void setTxs(List<Transaction> txs) {
        this.txs = txs;
    }
}
