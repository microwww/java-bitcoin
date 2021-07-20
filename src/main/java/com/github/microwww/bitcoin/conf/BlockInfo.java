package com.github.microwww.bitcoin.conf;

import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.net.Peer;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 该类中的属性不建议手动修改, key 使用本地地址, 这样可以方便发现服务是否断开.
 */
public class BlockInfo {

    private final AtomicInteger height = new AtomicInteger(0);
    private final Map<String, Peer> peers = new ConcurrentSkipListMap<>();
    private Path dataDir;
    private int hashCount; // number of block locator hash entries
    Settings settings = new Settings();

    private static BlockInfo block = new BlockInfo();

    public static BlockInfo getInstance() {
        return block;
    }

    private BlockInfo() {
    }

    public AtomicInteger getHeight() {
        return height;
    }

    public Map<String, Peer> getPeers() {
        return Collections.unmodifiableMap(peers);
    }

    public BlockInfo addPeers(InetSocketAddress address, Peer peer) {
        this.peers.put(key(address), peer);
        return this;
    }

    public BlockInfo remove(InetSocketAddress address) {
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
        return BlockInfo.getInstance().getPeer((InetSocketAddress) ctx.channel().localAddress());
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

    public Uint256 getHash(int height) {// TODO : block file !
        throw new UnsupportedOperationException();
    }
}
