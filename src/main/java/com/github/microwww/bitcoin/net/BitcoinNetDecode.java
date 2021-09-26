package com.github.microwww.bitcoin.net;

import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BitcoinNetDecode extends ReplayingDecoder<Void> {
    private static final Logger logger = LoggerFactory.getLogger(BitcoinNetDecode.class);

    private final CChainParams settings;

    public BitcoinNetDecode(CChainParams settings) {
        this.settings = settings;
    }

    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int magic = settings.getEnvParams().getMagic();
        MessageHeader read;
        ByteBuf temp = byteBuf.readBytes(MessageHeader.HEADER_SIZE);
        try {
            read = MessageHeader.readHeader(temp);
        } finally {
            temp.release();
        }
        temp = byteBuf.readBytes(read.getLength());
        try {
            MessageHeader.readBody(read, temp);
        } finally {
            temp.release();
        }
        if (logger.isDebugEnabled())
            logger.debug("Decode command {}, 0x{}, next bytes {} ", read.getCommand(), ByteUtil.hex(read.getPayload()), byteBuf.readableBytes());

        if (read.getMagic() != magic) {
            logger.error("Magic not match: NEED 0x{} BUT 0x{}", Integer.toUnsignedString(magic, 16), Integer.toUnsignedString(read.getMagic(), 16));
            channelHandlerContext.channel().close();
        } else {
            list.add(read);
        }
    }
}
