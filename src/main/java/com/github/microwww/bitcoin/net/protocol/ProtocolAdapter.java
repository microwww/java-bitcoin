package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.conf.BlockInfo;
import com.github.microwww.bitcoin.net.Peer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;

public class ProtocolAdapter implements AbstractProtocol {
    private static final Logger logger = LoggerFactory.getLogger(ProtocolAdapter.class);

    @Override
    public void write(ByteBuf buf) {
        logger.debug("{} set nothing", this.getClass().getSimpleName());
    }

    @Override
    public void writeWithHeader(ByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void service(ChannelHandlerContext ctx) {
        logger.info("DO nothing");
    }

    public static <T> T readNothing(byte[] payload, Class<T> clazz, Object... args) {
        Assert.isTrue(payload.length == 0, "Not need message");
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
