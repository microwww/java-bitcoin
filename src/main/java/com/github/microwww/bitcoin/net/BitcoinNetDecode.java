package com.github.microwww.bitcoin.net;

import com.github.microwww.bitcoin.conf.Settings;
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

    private final Settings settings;

    public BitcoinNetDecode(Settings settings) {
        this.settings = settings;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int magic = settings.getMagic();
        MessageHeader read = MessageHeader.read(byteBuf);
        logger.debug("Decode a command : {}", read.getCommand());
        Assert.isTrue(read.getMagic() == magic, "Magic not match: NEED " + magic + " BUT " + read.getMagic());
        list.add(read);
    }
}
