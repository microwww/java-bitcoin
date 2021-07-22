package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.net.Peer;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;

public class GetData extends AbstractTypeHash<GetData> {
    private Uint32 typeIn;
    private Uint256 hashIn;

    public GetData(Peer peer) {
        super(peer);
    }

    @Override
    protected GetData read0(ByteBuf buf) {
        typeIn = new Uint32(buf.readIntLE());
        hashIn = new Uint256(ByteUtil.readLength(buf, Uint256.LEN));
        return this;
    }

    @Override
    protected void write0(ByteBuf buf) {
        buf.writeIntLE(typeIn.intValue());
        buf.writeBytes(hashIn.reverse256bit());
    }

    public Uint32 getTypeIn() {
        return typeIn;
    }

    public void setTypeIn(Uint32 typeIn) {
        this.typeIn = typeIn;
    }

    public Uint256 getHashIn() {
        return hashIn;
    }

    public void setHashIn(Uint256 hashIn) {
        this.hashIn = hashIn;
    }
}
