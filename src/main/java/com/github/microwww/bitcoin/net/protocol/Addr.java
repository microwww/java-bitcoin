package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.math.UintVar;
import com.github.microwww.bitcoin.net.Peer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 请求节点发送 已知 PEER 的地址, 三小时内活动的节点会被 `ADDR` 返回
 */
public class Addr extends AbstractProtocolAdapter<Addr> {
    PeerNode[] nodes;

    public Addr(Peer peer) {
        super(peer);
    }

    @Override
    public Addr read(byte[] buf) {
        ByteBuf buffer = Unpooled.copiedBuffer(buf);
        int len = UintVar.parse(buffer).intValueExact();
        PeerNode[] ns = new PeerNode[len];
        for (int i = 0; i < len; i++) { // TODO:: 有出入, 需要真实数据验证
            long service = buffer.readLongLE();
            byte[] addr = new byte[16];
            buffer.readBytes(addr);
            short port = buffer.readShortLE();
            PeerNode peerNode = new PeerNode(service, port).setAddress(addr);
            ns[i] = peerNode;
        }
        this.nodes = ns;
        return this;
    }

    public PeerNode[] getNodes() {
        return nodes;
    }

    public void setNodes(PeerNode[] nodes) {
        this.nodes = nodes;
    }
}
