package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.math.Uint256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.*;

public class MemBlockHeight {
    private static final Logger logger = LoggerFactory.getLogger(MemBlockHeight.class);
    private final ChainBlock generate;
    Map<Uint256, Node> chainHashIndex = new HashMap<>();
    List<Node> chains = new ArrayList<>();

    public MemBlockHeight(ChainBlock generate) {
        this.generate = generate;
        new Node(0, generate).add();
    }

    /**
     * @param block
     * @return 返回添加高度, -1 添加失败
     */
    public synchronized int tryAdd(ChainBlock block) {
        Uint256 preHash = block.header.getPreHash();
        Node val = chainHashIndex.get(preHash);
        if (val != null) {
            return new Node(val.height + 1, block).add();
        }
        return -1;
    }

    public synchronized int hashAdd(Uint256 hash, int height) {
        Assert.isTrue(chains.size() == height, "Only to add, NOT SET");
        return new Node(height, hash).add();
    }

    public synchronized Optional<Uint256> get(int index) {
        if (chains.size() > index) {
            Node st = chains.get(index);
            return Optional.of(st.hash);
        }
        return Optional.empty();
    }

    /**
     * @param hash
     * @return 从 0 开始, 如果找不到 返回 -1
     */
    public synchronized int get(Uint256 hash) {
        Node node = chainHashIndex.get(hash);
        if (node != null) {
            return node.height;
        }
        return -1;
    }

    /**
     * from 0
     *
     * @return
     */
    public synchronized int getLatestHeight() {
        return chains.size() - 1;
    }

    public synchronized Uint256 getLatestHash() {
        if (chains.isEmpty()) {
            return null;
        }
        return chains.get(chains.size() - 1).hash;
    }

    public synchronized Uint256 removeTail(int count) {
        Node last = null;
        for (int i = 0; i < count; i++) {
            last = chains.get(chains.size() - 1).remove();
        }
        return last == null ? null : last.hash;
    }

    class Node {
        final int height;
        Uint256 hash;

        public Node(int height, ChainBlock block) {
            this.height = height;
            this.hash = block.hash();
        }

        public Node(int height, Uint256 hash) {
            this.height = height;
            this.hash = hash;
        }

        public int add() {
            if (chains.size() != height) {
                return -1;
            }
            chains.add(this);
            chainHashIndex.put(hash, this);
            logger.debug("Add new Height : {},  {}", height, hash);
            return height;
        }

        public Node remove() {
            Node remove = chains.remove(height);
            Assert.isTrue(remove == this, "remove himself");
            chainHashIndex.remove(hash);
            return this;
        }
    }

    public ChainBlock getGenerate() {
        return generate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("MemBlockHeight { ").append("chains=\n");
        for (Node chain : chains) {
            sb.append("    height = ").append(chain.height)
                    .append(", hash = ").append(chain.hash).append("\n");
        }
        sb.append('}');
        return sb.toString();
    }
}
