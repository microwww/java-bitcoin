package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.provider.Peer;
import io.netty.buffer.ByteBuf;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class GetHeaders extends AbstractProtocolAdapter<GetHeaders> {
    public static final int MAX_UN_CONNECTING_HEADERS = 10;
    public static final int MAX_GET_BLOCK_SZ = 10;
    public static final int MAX_LOCATOR_SZ = 101;
    public static final int MAX_HEADERS_RESULTS = 2000;

    private int version;
    private Uint32 count;
    private Uint256 stopping;
    private List<Uint256> starting = new ArrayList<>();

    public GetHeaders(Peer peer) {
        super(peer);
    }

    @Override
    public int write(ByteBuf buf) {
        int size = buf.readableBytes();
        int ver = peer.getMeSettings().getProtocolVersion();
        buf.writeInt(ver);
        Assert.isTrue(!starting.isEmpty(), "size must > 0 and not set 000..000");
        Assert.isTrue(starting.size() < 254, "size must < 254 and not set 000..000");
        buf.writeByte(starting.size());
        for (Uint256 int256 : starting) {
            buf.writeBytes(int256.fill256bit());
        }
        buf.writeBytes(Uint256.zero256());
        return buf.readableBytes() - size;
    }

    @Override
    protected GetHeaders read0(ByteBuf buf) {
        int size = buf.readableBytes();
        this.version = buf.readInt();
        this.count = new Uint32(buf.readByte());
        for (Uint256 int256 : starting) {
            buf.writeBytes(int256.fill256bit());
        }
        this.stopping = Uint256.read(buf);
        return this;
    }

    public GetHeaders setStarting(List<Uint256> starting) {
        this.starting = starting;
        return this;
    }

    public List<Uint256> getStarting() {
        return starting;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Uint32 getCount() {
        return count;
    }

    public void setCount(Uint32 count) {
        this.count = count;
    }

    public Uint256 getStopping() {
        return stopping;
    }

    public GetHeaders setStopping(Uint256 stopping) {
        this.stopping = stopping;
        return this;
    }
}
