package com.github.microwww.bitcoin.conf;

import com.github.microwww.bitcoin.net.ServiceFlags;
import com.github.microwww.bitcoin.net.protocol.ProtocolVersion;
import com.github.microwww.bitcoin.util.FilesUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "conf.bitcoin")
public class Settings {
    private String agent = "/j-bitcoin-0.18.1:0.0.1/";
    private String dataDir = "/bitcion";
    private String[] connections;
    private String[] peers;
    private boolean txIndex = false;
    private boolean reIndex = false;
    private int bestConfirmHeight = 6;

    private CChainParams.Env env = CChainParams.Env.MAIN;

    private int protocolVersion = ProtocolVersion.PROTOCOL_VERSION;
    private long services = ServiceFlags.join(ServiceFlags.NODE_NETWORK, ServiceFlags.NODE_WITNESS, ServiceFlags.NODE_NETWORK_LIMITED);

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public long getServices() {
        return services;
    }

    public void setServices(long services) {
        this.services = services;
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public String[] getConnections() {
        return connections;
    }

    public void setConnections(String[] connections) {
        this.connections = connections;
    }

    public String[] getPeers() {
        return peers;
    }

    public List<URI> toPeers() {
        List<URI> list = new ArrayList<>(peers.length);
        for (String peer : peers) {
            String[] hp = peer.split(":");
            try {
                list.add(new URI("bitcoin", null, hp[0], Integer.parseInt(hp[1]), null, null, null));
            } catch (URISyntaxException e) {
                throw new RuntimeException("format error : " + peer + ", example : 8.8.8.8:8333", e);
            }
        }
        return list;
    }

    public void setPeers(String[] peers) {
        this.peers = peers;
    }

    public CChainParams.Env getEnv() {
        return env;
    }

    public void setEnv(CChainParams.Env env) {
        this.env = env;
    }

    public boolean isTxIndex() {
        return txIndex;
    }

    public void setTxIndex(boolean txIndex) {
        this.txIndex = txIndex;
    }

    public boolean isReIndex() {
        return reIndex;
    }

    public void setReIndex(boolean reIndex) {
        throw new UnsupportedOperationException();
    }

    public int getBestConfirmHeight() {
        return bestConfirmHeight;
    }

    public void setBestConfirmHeight(int bestConfirmHeight) {
        Assert.isTrue(bestConfirmHeight >= 0, "BestConfirmHeight >= 0");
        this.bestConfirmHeight = bestConfirmHeight;
    }

    public File lockupRootDirectory() {
        String prefix = this.getEnv().params.getDataDirPrefix();
        File root = new File(this.getDataDir(), prefix);
        try {
            FilesUtil.createCanWriteDir(root);
            return root;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
