package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.net.Peer;

/**
 * 指示节点更喜欢通过“headers”消息而不是“inv”接收新的块通知。
 */
public class SendHeaders extends AbstractProtocolAdapter<SendHeaders> {
    public SendHeaders(Peer peer) {
        super(peer);
    }
}
