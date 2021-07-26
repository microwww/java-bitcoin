package com.github.microwww.bitcoin.net;

import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.net.protocol.AbstractProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.List;

public class BitcoinNetDecode extends ReplayingDecoder<AbstractProtocol> {
    private static final Logger logger = LoggerFactory.getLogger(BitcoinNetDecode.class);

    private final CChainParams settings;

    public BitcoinNetDecode(CChainParams settings) {
        this.settings = settings;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int magic = settings.getEnvParams().getMagic();
        int start = byteBuf.readerIndex();
        byteBuf.markReaderIndex();
        if (byteBuf.readableBytes() >= MessageHeader.HEADER_SIZE) {
            MessageHeader read = MessageHeader.readHeader(byteBuf);
            int end = byteBuf.readerIndex();
            Assert.isTrue(end - start == MessageHeader.HEADER_SIZE, "protocol is modify, to fix it");
            if (byteBuf.readableBytes() >= read.getLength()) {
                MessageHeader.readBody(read, byteBuf);
                logger.debug("Decode a command : {}", read.getCommand());
                Assert.isTrue(read.getMagic() == magic, "Magic not match: NEED " + magic + " BUT " + read.getMagic());
                list.add(read);
            } else { // 半包
                logger.debug("Decode a half package command : {}", read.getCommand());
                byteBuf.resetReaderIndex();
            }
        }
    }
}
