package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.conf.BlockInfo;
import com.github.microwww.bitcoin.math.Int256;
import com.github.microwww.bitcoin.net.Peer;
import io.netty.buffer.ByteBuf;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class GetHeaders extends AbstractProtocolAdapter<GetHeaders> {
    private List<Int256> list = new ArrayList<>();

    public GetHeaders(Peer peer) {
        super(peer);
    }

    @Override
    public int write(ByteBuf buf) {
        int size = buf.readableBytes();
        int ver = BlockInfo.getInstance().getSettings().getProtocolVersion();
        buf.writeInt(ver);
        Assert.isTrue(list.size() < 254, "size must < 254 and not set 000..000");
        buf.writeByte(list.size() + 1);
        for (Int256 int256 : list) {
            buf.writeBytes(int256.reverse());
        }
        buf.writeBytes(Int256.getZero());
        return buf.readableBytes() - size;
    }

    public GetHeaders setList(List<Int256> list) {
        this.list = list;
        return this;
    }
}
