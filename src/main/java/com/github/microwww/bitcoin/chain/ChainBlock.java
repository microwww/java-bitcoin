package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.math.MerkleTree;
import com.github.microwww.bitcoin.math.Uint256;
import io.netty.buffer.ByteBuf;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Arrays;

public class ChainBlock implements Serializable {
    public final BlockHeader header;
    private RawTransaction[] txs = new RawTransaction[]{};

    public ChainBlock() {
        this(new BlockHeader());
    }

    public ChainBlock(BlockHeader header) {
        if (header == null) {
            throw new IllegalArgumentException("Not null");
        }
        this.header = header;
    }

    public ChainBlock readHeader(ByteBuf bf) {
        this.header.read(bf);
        return this;
    }

    public ChainBlock readBody(ByteBuf bf) {
        int len = this.header.getTxCount().intValue();
        txs = new RawTransaction[len];
        for (int i = 0; i < len; i++) {
            RawTransaction tr = new RawTransaction();
            tr.read(bf);
            txs[i] = tr;
        }
        return this;
    }

    public ChainBlock writeHeader(ByteBuf bf) {
        this.header.writer(bf);
        return this;
    }

    public ChainBlock writeTxCount(ByteBuf bf) {
        bf.writeByte(txs.length);
        return this;
    }

    public ChainBlock writeTxBody(ByteBuf bf) {
        for (RawTransaction tx : this.txs) {
            tx.write(bf);
        }
        return this;
    }

    public Uint256 hash() {
        if (this.header.getMerkleRoot() == null) {
            this.header.setMerkleRoot(this.merkleRoot());
        }
        return this.header.hash();
    }

    public Uint256 merkleRoot() {
        MerkleTree<RawTransaction, Uint256> tree = merkleTree();
        return tree.getHash();
    }

    public MerkleTree<RawTransaction, Uint256> merkleTree() {
        Assert.isTrue(this.getTxs() != null, "Not find any Transaction");
        MerkleTree<RawTransaction, Uint256> tree = MerkleTree.merkleTree(
                Arrays.asList(this.getTxs()),
                e -> e.hash(),
                (e1, e2) -> new Uint256(e1.sha256sha256(e2))
        );
        return tree;
    }

    public RawTransaction[] getTxs() {
        return txs;
    }

    public int getTxCount() {
        return txs == null ? 0 : txs.length;
    }

    public void setTxs(RawTransaction[] txs) {
        this.txs = txs;
    }

    @Override
    public String toString() {
        StringBuilder append = new StringBuilder().append("{ // _BLOCK");
        return toString(append, "\n").append("\n").append("}").toString();
    }

    public StringBuilder toString(StringBuilder sb, String prefix) {
        sb.append(prefix).append(" header  = { // HEADER");
        header.toString(sb, prefix + "      ");
        sb.append(prefix).append("   }");
        sb.append(prefix).append("   txs   = ").append(header.getTxCount()).append(" -> ").append(txs.length);

        sb.append(prefix).append("   { // RawTransaction");
        for (RawTransaction tx : txs) {
            tx.toString(sb, prefix + "        ");
            sb.append(prefix);
        }
        sb.append("   }");
        return sb;
    }
}
