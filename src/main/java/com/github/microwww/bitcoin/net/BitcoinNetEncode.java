package com.github.microwww.bitcoin.net;

import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.net.protocol.AbstractProtocol;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BitcoinNetEncode extends MessageToByteEncoder<AbstractProtocol> {
    private static final Logger logger = LoggerFactory.getLogger(BitcoinNetEncode.class);
    private final CChainParams params;

    public BitcoinNetEncode(CChainParams settings) {
        this.params = settings;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, AbstractProtocol data, ByteBuf byteBuf) throws Exception {
        int magic = params.getEnvParams().getMagic();
        ByteBuf buffer = Unpooled.buffer();
        data.write(buffer);
        NetProtocol protocol = data.support();
        logger.debug("Encode a command : {}", protocol);

        new MessageHeader(magic, protocol).setPayload(ByteUtil.readAll(buffer)).writer(buffer);
        channelHandlerContext.writeAndFlush(buffer);
    }
}
