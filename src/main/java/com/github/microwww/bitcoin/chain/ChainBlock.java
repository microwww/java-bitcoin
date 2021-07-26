package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.math.MerkleTree;
import com.github.microwww.bitcoin.math.Uint256;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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

    public ByteBuf serialization() {
        ByteBuf buffer = Unpooled.buffer();
        this.writeHeader(buffer).writeTxCount(buffer).writeTxBody(buffer);
        return buffer;
    }

    public ChainBlock deserialization(byte[] bytes) {
        ByteBuf buffer = Unpooled.copiedBuffer(bytes);
        this.readHeader(buffer).readBody(buffer);
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
        return toString(new StringBuilder()).toString();
    }

    public StringBuilder toString(StringBuilder sb) {
        sb.append("Block{")
                .append("header=").append(header.toString(sb))
                .append(", _txCount=").append(header.getTxCount()).append(" -> ").append(txs.length)
                .append(", txs="); //.append(Arrays.toString(txs))

        for (RawTransaction tx : txs) {
            sb.append("\n    ").append(tx.toString(sb));
        }
        if (txs.length > 0) {
            sb.append("\n");
        }
        sb.append('}');
        return sb;
    }
}
