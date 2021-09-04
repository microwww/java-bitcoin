package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.math.UintVar;
import com.github.microwww.bitcoin.provider.Peer;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 请求节点发送 已知 PEER 的地址, 三小时内活动的节点会被 `ADDR` 返回
 */
public class AddrV2 extends AbstractProtocolAdapter<AddrV2> {
    private static final Logger logger = LoggerFactory.getLogger(AddrV2.class);
    public static final int NODE_ADDRESS_MAX_LENGTH = 512;
    private Node[] nodes = {};

    public AddrV2(Peer peer) {
        super(peer);
    }

    @Override
    protected AddrV2 read0(ByteBuf buf) {
        int len = UintVar.parse(buf).intValueExact();
        List<Node> list = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            Node node = new Node();
            node.time = buf.readIntLE();
            node.services = UintVar.parse(buf);
            byte v = buf.readByte();
            if (v > 0 && v < NetworkID.values().length) {
                node.networkID = NetworkID.values()[v];
            } else {
                logger.warn("Addrv2 payload do not parse NetworkID [{}] , ignore : {}", v, peer.getURI());
                break; // 顺序解析, 部分无法解析导致后面的也无法解析
            }
            int chars = UintVar.parse(buf).intValueExact();
            node.addr = ByteUtil.readLength(buf, chars);
            node.port = buf.readShortLE();
            // 放到最后, 否则后面的也无法解析
            if (chars > NODE_ADDRESS_MAX_LENGTH) {
                logger.warn("Addrv2 payload over MAX-bytes, ignore : {}", peer.getURI());
                continue;
            }
            list.add(node);
        }
        this.nodes = list.toArray(new Node[]{});
        return this;
    }

    @Override
    protected void write0(ByteBuf buf) {
        UintVar.valueOf(nodes.length).write(buf);
        for (Node node : nodes) {
            buf.writeIntLE(node.time);
            node.services.write(buf);
            buf.writeByte(node.networkID.ordinal());
            UintVar.writeData(buf, node.addr);
            buf.writeShortLE(node.port);
        }
    }

    public Node[] getNodes() {
        return nodes;
    }

    public void setNodes(Node[] nodes) {
        this.nodes = nodes;
    }

    @Override
    public String toString() {
        return "AddrV2, " +
                "nodes=" + Arrays.toString(nodes);
    }

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
        private UintVar services;
        private NetworkID networkID;
        private byte[] addr;
        private short port;

        public int getTime() {
            return time;
        }

        public Date getDateTime() {
            return new Date(this.time * 1_000);
        }

        public void setTime(int time) {
            this.time = time;
        }

        public UintVar getServices() {
            return services;
        }

        public void setServices(UintVar services) {
            this.services = services;
        }

        public NetworkID getNetworkID() {
            return networkID;
        }

        public void setNetworkID(NetworkID networkID) {
            this.networkID = networkID;
        }

        public byte[] getAddr() {
            return addr;
        }

        public void setAddr(byte[] addr) {
            this.addr = addr;
        }

        public short getPort() {
            return port;
        }

        public void setPort(short port) {
            this.port = port;
        }

        @Override
        public String toString() {
            return "Node {" +
                    "time=" + getDateTime() +
                    ", services=" + services +
                    ", networkID=" + networkID +
                    ", addr='" + addr + '\'' +
                    ", port=" + port +
                    '}';
        }
    }

}
