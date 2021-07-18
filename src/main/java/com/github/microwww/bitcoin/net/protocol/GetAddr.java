package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.net.Peer;

/**
 * 请求节点发送 已知 PEER 的地址, 三小时内活动的节点会被 `ADDR` 返回
 */
public class GetAddr extends AbstractProtocolAdapter<GetAddr> {
    public GetAddr(Peer peer) {
        super(peer);
    }
}
