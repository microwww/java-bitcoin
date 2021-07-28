package com.github.microwww.bitcoin.net;

import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.net.protocol.Version;
import com.github.microwww.bitcoin.provider.LocalBlockChain;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;

public class Peer {
    public static final AttributeKey<Peer> PEER = AttributeKey.newInstance("connection-peer");

    private final LocalBlockChain localBlockChain;
    private final String host;
    private final int port;
    private Version version;
    private int blockHeight;

    private InetSocketAddress localAddress;
    private boolean meReady;
    private boolean remoteReady;

    public Peer(LocalBlockChain localBlockChain, String host, int port) {
        this.localBlockChain = localBlockChain;
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public int getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(int blockHeight) {
        this.blockHeight = blockHeight;
    }

    public boolean isMeReady() {
        return meReady;
    }

    public void setMeReady(boolean meReady) {
        this.meReady = meReady;
    }

    public boolean isRemoteReady() {
        return remoteReady;
    }

    public void setRemoteReady(boolean remoteReady) {
        this.remoteReady = remoteReady;
    }

    public boolean isReady() {
        return remoteReady && meReady;
    }

    public Settings getMeSettings() {
        return localBlockChain.getSettings();
    }

    public CChainParams.Params getMeParams() {
        return localBlockChain.getChainParams().getEnvParams();
    }

    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(InetSocketAddress localAddress) {
        this.localAddress = localAddress;
    }

    public LocalBlockChain getLocalBlockChain() {
        return localBlockChain;
    }

    @Override
    public String toString() {
        return "Peer{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", blockHeight=" + blockHeight +
                ", meReady=" + meReady +
                ", remoteReady=" + remoteReady +
                '}';
    }
}
