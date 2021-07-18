package com.github.microwww.bitcoin.net;

import com.github.microwww.bitcoin.net.protocol.Version;

public class Peer {
    private final String host;
    private final int port;
    private Version version;
    private int blockHeight;

    private boolean meReady;
    private boolean remoteReady;

    public Peer(String host, int port) {
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
