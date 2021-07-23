package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.net.Peer;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 该类中的属性不建议手动修改, key 使用本地地址, 这样可以方便发现服务是否断开.
 */
public class BlockChainContext {
    private static final Logger logger = LoggerFactory.getLogger(BlockChainContext.class);

    private boolean init = false;
    private final AtomicInteger height = new AtomicInteger(0);
    private final Map<String, Peer> peers = new ConcurrentSkipListMap<>();
    private Path dataDir;
    private int hashCount; // number of block locator hash entries
    private Settings settings = new Settings();
    // TODO :: 有线程安全问题  暂时不处理
    private final LinkedList<ChainBlock> blocks = new LinkedList<>();

    private static BlockChainContext block = new BlockChainContext();

    public static BlockChainContext get() {
        return block;
    }

    private BlockChainContext() {
    }

    public AtomicInteger getHeight() {
        return height;
    }

    public Map<String, Peer> getPeers() {
        return Collections.unmodifiableMap(peers);
    }

    public BlockChainContext addPeers(InetSocketAddress address, Peer peer) {
        this.peers.put(key(address), peer);
        return this;
    }

    public BlockChainContext remove(InetSocketAddress address) {
        this.peers.remove(key(address));
        return this;
    }

    public Path getDataDir() {
        return dataDir;
    }

    public void setDataDir(Path dataDir) {
        this.dataDir = dataDir;
    }

    public Peer getPeer(InetSocketAddress address) {
        return this.peers.get(key(address));
    }

    public static Peer getPeer(ChannelHandlerContext ctx) {
        return BlockChainContext.get().getPeer((InetSocketAddress) ctx.channel().localAddress());
    }

    private String key(InetSocketAddress dr) {
        return dr.getHostString() + ":" + dr.getPort();
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public int getHashCount() {
        return hashCount;
    }

    public void setHashCount(int hashCount) {
        this.hashCount = hashCount;
    }

    public ChainBlock getHash(int height) {// TODO : block file !
        return blocks.get(height);
    }

    public ChainBlock getLatestBlock() {
        synchronized (blocks) {
            return blocks.getLast();
        }
    }

    // TODO :: 需要处理顺序
    public BlockChainContext addBlock(ChainBlock header) {
        synchronized (blocks) {
            blocks.add(header);
        }
        return this;
    }

    public void init() {
        Assert.isTrue(!init, "do not to re-init");
        synchronized (blocks) {
            if (!init) {
                init = true;
                if (blocks.isEmpty()) {
                    ChainBlock genesis = this.settings.getEnv().createGenesisBlock();
                    blocks.add(genesis);
                    logger.info("create Genesis Block : {}", genesis.hash().toHexReverse256());
                    height.set(0);//创世块
                }
            }
        }
    }

    public BlockChainContext setChainBlock(ChainBlock block) {
        Uint256 hs = block.hash();
        synchronized (blocks) {
            boolean add = false;
            for (int i = 0; i < blocks.size(); i++) {
                if (blocks.get(i).hash().equals(hs)) {
                    blocks.set(i, block);
                    add = true;
                    break;
                }
            }
            if (!add) {
                logger.warn("Skip a block which is not find : {}", hs.toHexReverse256());
            }
        }
        return this;
    }
}
