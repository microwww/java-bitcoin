package com.github.microwww.bitcoin.net;

import com.github.microwww.bitcoin.conf.Config;
import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.net.protocol.Version;

import java.util.Set;

public class Peer {
    private final Settings meConfig;
    private final String host;
    private final int port;
    private Version version;
    private int blockHeight;

    private boolean meReady;
    private boolean remoteReady;

    public Peer(Settings me, String host, int port) {
        this.meConfig = me;
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

    public Settings getMeConfig() {
        return meConfig;
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
}
