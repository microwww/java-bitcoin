package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.math.MerkleTree;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.UintVar;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.springframework.util.Assert;

import java.util.Arrays;

public class ChainBlock implements ByteSerializable {
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

    private ChainBlock readBody(ByteBuf bf) {
        int len = this.header.getTxCount().intValue();
        txs = new RawTransaction[len];
        for (int i = 0; i < len; i++) {
            RawTransaction tr = new RawTransaction();
            tr.deserialization(bf);
            txs[i] = tr;
        }
        return this;
    }

    private ChainBlock writeHeader(ByteBuf bf) {
        this.header.writer(bf);
        return this;
    }

    private ChainBlock writeTxCount(ByteBuf bf) {
        Assert.isTrue(txs.length == header.getTxCount().intValueExact(), "tx and head-tx equals");
        UintVar.valueOf(txs.length).write(bf);
        return this;
    }

    private void writeTxBody(ByteBuf bf) {
        Uint256 hash = this.hash();
        for (RawTransaction tx : this.txs) {
            tx.serialization(bf);
            tx.setBlockHash(hash);
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
        return merkleTree(this.txs);
    }

    public static MerkleTree<RawTransaction, Uint256> merkleTree(RawTransaction[] txs) {
        Assert.isTrue(txs != null, "Not find any Transaction");
        MerkleTree<RawTransaction, Uint256> tree = MerkleTree.merkleTree(
                Arrays.asList(txs),
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
        header.setTxCount(txs.length);
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

    public ChainBlock reset(ByteBuf buffer) {
        this.deserialization(buffer);
        return this;
    }

    public byte[] serialization() {
        return ByteUtil.readAll(serialization(Unpooled.buffer()));
    }

    public ByteBuf serialization(ByteBuf buffer, boolean tx) {
        this.writeHeader(buffer);
        if (tx) {
            this.writeTxCount(buffer).writeTxBody(buffer);
        } else {
            UintVar.valueOf(0).write(buffer);
        }
        return buffer;
    }

    @Override
    public ByteBuf serialization(ByteBuf buffer) {
        return this.serialization(buffer, true);
    }

    @Override
    public ByteBuf deserialization(ByteBuf buffer) {
        this.readHeader(buffer).readBody(buffer);
        return buffer;
    }
}
