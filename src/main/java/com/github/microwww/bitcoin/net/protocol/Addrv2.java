package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.provider.Peer;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;

/**
 * 请求节点发送 已知 PEER 的地址, 三小时内活动的节点会被 `ADDR` 返回
 */
public class Addrv2 extends AbstractProtocolAdapter<CmpctBlock> {
    public enum NetworkID {
        IPV4(4),
        IPV6(16),
        TORV2(10),
        TORV3(32),
        I2P(32),
        CJDNS(16),
        ;

        private final int len;

        NetworkID(int len) {
            this.len = len;
        }

        public int getId() {
            return this.ordinal() + 1;
        }

        public byte[] read(ByteBuf bf) {
            return ByteUtil.readLength(bf, this.len);
        }
    }

    public static class Node {
        private int time;
        private long services;
        private NetworkID networkID;
        private String addr;
        private short port;

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public long getServices() {
            return services;
        }

        public void setServices(long services) {
            this.services = services;
        }

        public NetworkID getNetworkID() {
            return networkID;
        }

        public void setNetworkID(NetworkID networkID) {
            this.networkID = networkID;
        }

        public String getAddr() {
            return addr;
        }

        public void setAddr(String addr) {
            this.addr = addr;
        }

        public short getPort() {
            return port;
        }

        public void setPort(short port) {
            this.port = port;
        }
    }

    public Addrv2(Peer peer) {
        super(peer);
    }
}
