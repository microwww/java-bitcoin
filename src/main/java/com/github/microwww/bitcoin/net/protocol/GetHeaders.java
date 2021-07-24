package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.net.Peer;
import io.netty.buffer.ByteBuf;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class GetHeaders extends AbstractProtocolAdapter<GetHeaders> {
    private List<Uint256> list = new ArrayList<>();

    public GetHeaders(Peer peer) {
        super(peer);
    }

    @Override
    public int write(ByteBuf buf) {
        int size = buf.readableBytes();
        int ver = peer.getMeSettings().getProtocolVersion();
        buf.writeInt(ver);
        Assert.isTrue(!list.isEmpty(), "size must > 0 and not set 000..000");
        Assert.isTrue(list.size() < 254, "size must < 254 and not set 000..000");
        buf.writeByte(list.size());
        for (Uint256 int256 : list) {
            buf.writeBytes(int256.reverse256bit());
        }
        buf.writeBytes(Uint256.zero256());
        return buf.readableBytes() - size;
    }

    public GetHeaders setList(List<Uint256> list) {
        this.list = list;
        return this;
    }
}
