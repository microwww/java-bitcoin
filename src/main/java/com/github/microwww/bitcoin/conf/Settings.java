package com.github.microwww.bitcoin.conf;

import com.github.microwww.bitcoin.net.ServiceFlags;
import com.github.microwww.bitcoin.net.protocol.ProtocolVersion;

public class Settings {
    private int magic = 0xfabfb5da; // 0xf9beb4d9;
    private String agent = "/j-bitcoin-0.18.1:0.0.1/";
    private String dataDir = "/bitcion";
    private String[] connections;
    private String[] peers;

    private int protocolVersion = ProtocolVersion.PROTOCOL_VERSION;
    private long services = ServiceFlags.join(ServiceFlags.NODE_NETWORK, ServiceFlags.NODE_WITNESS, ServiceFlags.NODE_NETWORK_LIMITED);

    public int getMagic() {
        return magic;
    }

    public void setMagic(int magic) {
        this.magic = magic;
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

    public void setPeers(String[] peers) {
        this.peers = peers;
    }
}
