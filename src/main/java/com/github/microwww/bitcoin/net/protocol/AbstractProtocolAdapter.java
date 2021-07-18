package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.conf.BlockInfo;
import com.github.microwww.bitcoin.net.MessageHeader;
import com.github.microwww.bitcoin.net.Peer;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProtocolAdapter<T extends AbstractProtocol> implements AbstractProtocol<T> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractProtocolAdapter.class);

    protected final Peer peer;

    public AbstractProtocolAdapter(Peer peer) {
        this.peer = peer;
    }

    @Override
    public MessageHeader writeWithHeader(ByteBuf buf) {
        int magic = BlockInfo.getInstance().getSettings().getMagic();
        ByteBuf buffer = Unpooled.buffer();
        this.write(buffer);
        byte[] bytes = ByteUtil.readAll(buffer);
        return new MessageHeader(magic, this.support()).setPayload(bytes).writer(buf);
    }

    @Override
    public int write(ByteBuf buf) {
        logger.debug("{} set nothing", this.getClass().getSimpleName());
        return 0;
    }

    @Override
    public T read(byte[] buf) {
        logger.info("do not Reading nothing");
        return (T) this;
    }

    public Peer getPeer() {
        return peer;
    }
}
