package com.github.microwww.bitcoin.store;

import com.github.microwww.bitcoin.chain.ChainBlock;
import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.math.Uint256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.*;

public class MemBlockHeight {
    private static final Logger logger = LoggerFactory.getLogger(MemBlockHeight.class);
    private final int bestConfirmHeight;
    Map<Uint256, HeightER> chainHashIndex = new HashMap<>();
    List<StandbyList> chains = new ArrayList<>();
    int best = -1; // 0开始, 已经确定的最好的块

    public MemBlockHeight(CChainParams chainParams) {
        bestConfirmHeight = chainParams.settings.getBestConfirmHeight();
    }

    public MemBlockHeight init(ChainBlock block) {
        best = 0;
        chains.add(new StandbyList(best, block));
        return this;
    }

    /**
     * @param block
     * @return 返回添加高度, -1 添加失败
     */
    public synchronized int tryAdd(ChainBlock block) {
        Uint256 pre = block.header.getPreHash();
        HeightER h = chainHashIndex.get(pre);
        if (h.height < best) {
            return -1;
        }
        if (h == null) {
            return -1;
        }
        int next = h.height + 1;
        Assert.isTrue(chains.size() >= next, "List size > next");
        if (next >= chains.size()) {
            Assert.isTrue(chains.size() == next, "list size == next , ready to add");
            StandbyList standby = new StandbyList(next, block);
            chains.add(standby);
            confirmHeight(block);
        } else {
            chains.get(next).add(block);
        }
        return next;
    }

    public synchronized MemBlockHeight loadFrom(Uint256 hash, int height) {
        Assert.isTrue(chains.size() == height, "set is equal");
        chains.add(new StandbyList(height, hash));
        return this;
    }

    public synchronized MemBlockHeight loadFrom(ChainBlock hash, int height) {
        Assert.isTrue(chains.size() == height, "set is equal");
        chains.add(new StandbyList(height, hash));
        return this;
    }

    private void confirmHeight(ChainBlock highest) {
        if (chains.size() > best + bestConfirmHeight) {
            Uint256 hash = highest.header.getPreHash();
            for (int i = 1; i < bestConfirmHeight; i++) {
                hash = chainHashIndex.get(hash).preHash;
            }
            chains.get(best).confirm(hash);
            logger.info("New best height : {}, {}", best, hash);
            best++;
        }
    }

    public synchronized Optional<Uint256> get(int index) {
        if (chains.size() > index) {
            StandbyList st = chains.get(index);
            if (st.hash != null) {
                return Optional.of(st.hash);
            }
            return Optional.of(st.set.iterator().next().hash());
        }
        return Optional.empty();
    }

    /**
     * @param hash
     * @return 从 0 开始, 如果找不到 返回 -1
     */
    public synchronized int get(Uint256 hash) {
        for (int i = 0; i < chains.size(); i++) {
            if (hash.equals(hash)) {
                return i;
            }
        }
        return -1;
    }

    public synchronized int size() {
        return chains.size();
    }

    public synchronized boolean isEmpty() {
        return chains.isEmpty();
    }

    public synchronized int getHeight() {
        return chains.size();
    }

    static class HeightER {
        int height;
        Uint256 preHash;

        public HeightER(int height, Uint256 preHash) {
            this.height = height;
            this.preHash = preHash;
        }
    }

    class StandbyList {
        final int height;
        Uint256 hash; // 最后获取一个最好的
        Set<ChainBlock> set = new HashSet<>();

        /**
         * @param height
         * @param val    至少要有一个元素
         */
        public StandbyList(int height, ChainBlock val) {
            this.height = height;
            this.add(val);
        }

        public StandbyList(int height, Uint256 hash) {
            this.height = height;
            this.hash = hash;
            this.set = null;
        }

        public boolean add(ChainBlock block) {
            chainHashIndex.put(block.hash(), new HeightER(height, block.header.getPreHash()));
            return set.add(block);
        }

        public void confirm(Uint256 hash) {
            Assert.isTrue(this.contains(hash), "standby-list must contain hash");
            this.hash = hash;
            this.set = null;
        }

        private boolean contains(Uint256 hash) {
            if (set != null) {
                for (ChainBlock chainBlock : set) {
                    if (chainBlock.hash().equals(hash)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("MemBlockHeight { ").append("chains=\n");
        for (StandbyList chain : chains) {
            sb.append("    height = ").append(chain.height);
            if (chain.set != null) {
                for (ChainBlock cb : chain.set) {
                    sb.append(", hash = ").append(cb.hash())
                            .append(", prehash = ").append(cb.header.getPreHash());
                }
            }
            sb.append(", hash = ").append(chain.hash).append("\n");
        }
        sb.append('}');
        return sb.toString();
    }
}
