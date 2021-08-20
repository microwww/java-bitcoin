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
    private String[] peers = {};
    private boolean txIndex = false;
    private boolean reIndex = false;
    private int bestConfirmHeight = 6;
    private String bind; // host:port
    private int txPoolMax = 500;

    private CChainParams.Env env = CChainParams.Env.MAIN;

    private int protocolVersion = ProtocolVersion.PROTOCOL_VERSION;
    private long services = ServiceFlags.join(ServiceFlags.NODE_NETWORK, ServiceFlags.NODE_WITNESS, ServiceFlags.NODE_NETWORK_LIMITED);

    public Settings() {
    }

    public Settings(CChainParams.Env env) {
        this.env = env;
    }

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

    public Settings setDataDir(String dataDir) {
        this.dataDir = dataDir;
        return this;
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

    public List<URI> toPeers(int defPort) {
        return toPeers(peers, defPort);
    }

    public static List<URI> toPeers(String[] peers, int defPort) {
        List<URI> list = new ArrayList<>(peers.length);
        for (String peer : peers) {
            String[] hp = peer.split(":");
            try {
                String host = hp[0];
                int port = defPort;
                if (hp.length > 1) {
                    port = Integer.parseInt(hp[1]);
                }
                list.add(new URI("bitcoin", null, host, port, null, null, null));
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

    public Settings setTxIndex(boolean txIndex) {
        this.txIndex = txIndex;
        return this;
    }

    public boolean isReIndex() {
        return reIndex;
    }

    public void setReIndex(boolean reIndex) {
        this.reIndex = reIndex;
    }

    public int getBestConfirmHeight() {
        return bestConfirmHeight;
    }

    public String getBind() {
        return bind;
    }

    public void setBind(String bind) {
        this.bind = bind;
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

    public int getTxPoolMax() {
        return txPoolMax;
    }

    public void setTxPoolMax(int txPoolMax) {
        this.txPoolMax = txPoolMax;
    }
}
