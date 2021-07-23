package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.math.Uint8;
import com.github.microwww.bitcoin.net.Peer;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;

public class GetData extends AbstractProtocolAdapter<GetData> {

    private Uint8 count;
    private Message[] messages;

    public GetData(Peer peer) {
        super(peer);
    }

    @Override
    protected GetData read0(ByteBuf buf) {
        this.count = new Uint8(buf.readByte());
        for (int i = 0; i < count.intValue(); i++) {
            Message data = new Message();
            data.setTypeIn(new Uint32(buf.readIntLE()));
            data.setHashIn(new Uint256(ByteUtil.readLength(buf, Uint256.LEN)));
            messages[i] = data;
        }
        return this;
    }

    @Override
    protected void write0(ByteBuf buf) {
        int len = messages.length;
        Uint8.assertion(len);
        buf.writeByte(len);
        for (Message msg : messages) {
            buf.writeIntLE(msg.typeIn.intValue());
            buf.writeBytes(msg.hashIn.fill256bit());
        }
    }

    public static class Message {
        private Uint32 typeIn;
        private Uint256 hashIn;

        public Uint32 getTypeIn() {
            return typeIn;
        }

        public Message setTypeIn(Uint32 typeIn) {
            this.typeIn = typeIn;
            return this;
        }

        public Message setTypeIn(GetDataType typeIn) {
            this.typeIn = typeIn.getType();
            return this;
        }

        public Uint256 getHashIn() {
            return hashIn;
        }

        public Message setHashIn(Uint256 hashIn) {
            this.hashIn = hashIn;
            return this;
        }
    }

    public Uint8 getCount() {
        return count;
    }

    public GetData setCount(Uint8 count) {
        this.count = count;
        return this;
    }

    public Message[] getMessages() {
        return messages;
    }

    public GetData setMessages(Message[] messages) {
        this.messages = messages;
        return this;
    }
}
