package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.math.MerkleTree;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint8;
import io.netty.buffer.ByteBuf;
import org.springframework.util.Assert;

import java.util.Arrays;

public class ChainBlock {
    public final BlockHeader header;

    private Uint8 _txCount; // 这只是个标记位, 协议传输和解析的时候需要, 真实使用 txs.length 即可
    private RawTransaction[] txs;

    public ChainBlock() {
        this(new BlockHeader());
    }

    public ChainBlock(BlockHeader header) {
        if (header == null) {
            throw new IllegalArgumentException("Not null");
        }
        this.header = header;
    }

    public void readHeader(ByteBuf bf) {
        this.header.read(bf);
    }

    public void readBody(ByteBuf bf) {
        _txCount = new Uint8(bf.readByte());
        int len = _txCount.intValue();
        txs = new RawTransaction[len];
        for (int i = 0; i < len; i++) {
            RawTransaction tr = new RawTransaction();
            tr.read(bf);
            txs[i] = tr;
        }
    }

    public void writeHeader(ByteBuf bf) {
        this.header.writer(bf);
    }

    public void writeBody(ByteBuf bf) {
        bf.writeByte(txs.length);
        for (RawTransaction tx : this.txs) {
            tx.write(bf);
        }
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
        Assert.isTrue(this.getTxs() != null, "Not find transaction, init it");
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
                .append(", _txCount=").append(_txCount).append(" | ").append(txs.length)
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
