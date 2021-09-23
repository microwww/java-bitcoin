package com.github.microwww.bitcoin.provider;

import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.net.protocol.SendCmpct;
import com.github.microwww.bitcoin.net.protocol.Version;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public class Peer {
    public static final AttributeKey<Peer> PEER = AttributeKey.newInstance("connection-peer");

    private final LocalBlockChain localBlockChain;
    private final String host;
    private final int port;
    private Version version;
    private int blockHeight;
    private SendCmpct cmpct = new SendCmpct(this);

    private InetSocketAddress localAddress;
    private boolean meReady;
    private boolean remoteReady;

    public Peer(LocalBlockChain localBlockChain, String host, int port) {
        this.localBlockChain = localBlockChain;
        this.host = host;
        this.port = port;
    }

    public static URI uri(String host, short port) {
        return uri(host, Short.toUnsignedInt(port));
    }

    public static URI uri(String host, int port) {
        try {
            return new URI("bitcoin", null, host, port, "/", null, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String getHost() {
        return host;
    }

    public String getURI() {
        return "bitcoin://" + host + ":" + port;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Peer peer = (Peer) o;
        return port == peer.port && host.equals(peer.host);
    }

    public SendCmpct getCmpct() {
        return cmpct;
    }

    public void setCmpct(SendCmpct cmpct) {
        this.cmpct = cmpct;
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
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
