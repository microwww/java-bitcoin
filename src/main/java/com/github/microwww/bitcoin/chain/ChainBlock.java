package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.math.MerkleTree;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.UintVar;
import com.github.microwww.bitcoin.store.FileTransaction;
import com.github.microwww.bitcoin.util.ByteUtil;
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

    public boolean verifyMerkleTree() {
        if (this.txs == null || this.txs.length == 0) {
            throw new IllegalArgumentException("Not have transaction");
        }
        MerkleTree<RawTransaction, byte[]> mt = MerkleTree.merkleTree(Arrays.asList(this.txs),
                e -> e.hash().fill256bit(),
                (e1, e2) -> ByteUtil.sha256sha256(ByteUtil.concat(e1, e2)));
        return this.header.getMerkleRoot().equalsByte(mt.getHash());
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
        UintVar.valueOf(txs.length).write(bf);
        return this;
    }

    public FileTransaction[] writeTxBody(ByteBuf bf) {
        FileTransaction[] fts = new FileTransaction[this.txs.length];
        for (int i = 0; i < this.txs.length; i++) {
            RawTransaction tx = this.txs[i];
            int ix = bf.writerIndex();
            FileTransaction ft = new FileTransaction(tx).setPosition(ix);
            tx.write(bf);
            ft.setLength(bf.writerIndex() - ix);
            fts[i] = ft;
        }
        return fts;
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

    public int getHeight() {
        return header.getHeight().getAsInt();
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

    public byte[] serialization() {
        ByteBuf buffer = Unpooled.buffer();
        this.writeHeader(buffer).writeTxCount(buffer).writeTxBody(buffer);
        return ByteUtil.readAll(buffer);
    }
}
