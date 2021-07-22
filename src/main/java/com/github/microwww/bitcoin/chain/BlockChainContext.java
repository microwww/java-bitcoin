package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.net.Peer;
import io.netty.channel.ChannelHandlerContext;

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

    private final AtomicInteger height = new AtomicInteger(0);
    private final Map<String, Peer> peers = new ConcurrentSkipListMap<>();
    private Path dataDir;
    private int hashCount; // number of block locator hash entries
    private Settings settings = new Settings();
    // TODO :: 有线程安全问题  暂时不处理
    private LinkedList<ChainBlock> blocks = new LinkedList<>();

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
            if (blocks.isEmpty()) {
                blocks.add(this.settings.getEnv().createGenesisBlock());
                height.addAndGet(1);
            }
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
}
