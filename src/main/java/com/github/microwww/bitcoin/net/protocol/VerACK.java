package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.conf.BlockInfo;
import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.net.MessageHeader;
import com.github.microwww.bitcoin.net.NetProtocol;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class VerACK extends ProtocolAdapter {

    @Override
    public void service(ChannelHandlerContext ctx) {
        BlockInfo.getPeer(ctx).setRemoteReady(true);
        ctx.executor().execute(() -> {
            ctx.write(new GetAddr()); // 请求 节点发送 已知 PEER 的地址
        });
    }
}
