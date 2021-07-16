package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.conf.BlockInfo;
import com.github.microwww.bitcoin.conf.Settings;
import com.github.microwww.bitcoin.net.MessageHeader;
import com.github.microwww.bitcoin.net.NetProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class VerACK extends ProtocolAdapter {

    @Override
    public void service(ChannelHandlerContext ctx) {
        Settings settings = BlockInfo.getPeer(ctx).getMeConfig();
        ByteBuf bf = Unpooled.buffer();
        new VerACK().write(bf);
        ByteBuf buff = Unpooled.copiedBuffer(bf);
        // TODO :: WRITER HEAD MESSAGE
        new MessageHeader(settings.getMagic(), NetProtocol.VERACK).setPayload(bf.array()).writer(buff);
        ctx.writeAndFlush(bf);

        ctx.executor().execute(() -> {
            new MessageHeader(settings.getMagic(), NetProtocol.VERACK).setPayload(bf.array()).writer(buff);
        });
    }
}
