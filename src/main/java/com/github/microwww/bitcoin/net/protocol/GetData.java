package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.math.Uint32;
import com.github.microwww.bitcoin.math.UintVar;
import com.github.microwww.bitcoin.provider.Peer;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;

import java.util.Optional;

public class GetData extends AbstractProtocolAdapter<GetData> {

    private UintVar count;
    private Message[] messages;

    public GetData(Peer peer) {
        super(peer);
    }

    @Override
    protected GetData read0(ByteBuf buf) {
        this.count = UintVar.parse(buf);
        if (this.count.intValue() > 50_000) {
            throw new IllegalStateException("Payload (maximum 50,000 entries, which is just over 1.8 megabytes)");
        }
        this.messages = new Message[count.intValue()];

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
        // Uint8.assertion(len);
        // buf.writeByte(len);
        UintVar.valueOf(len).write(buf);
        for (Message msg : messages) {
            buf.writeIntLE(msg.typeIn.intValue());
            buf.writeBytes(msg.hashIn.fill256bit());
        }
    }

    public static class Message {
        private Uint32 typeIn;
        private Uint256 hashIn;

        public Message() {
        }

        public Message(Type type, Uint256 hash) {
            this.typeIn = new Uint32(type.inventory);
            this.hashIn = hash;
        }

        public Uint32 getTypeIn() {
            return typeIn;
        }

        public Optional<GetDataType> select() {
            return GetDataType.select(this.typeIn);
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

        public boolean isBlock() {
            return this.select().map(e -> e.name().contains("BLOCK")).orElse(false);
        }

        public boolean isTx() {
            return this.select().map(e -> e.name().contains("TX")).orElse(false);
        }

        @Override
        public String toString() {
            return "typeIn=" + typeIn + ", hashIn=" + hashIn;
        }
    }

    public UintVar getCount() {
        return count;
    }

    public Message[] getMessages() {
        return messages;
    }

    public GetData setMessages(Message[] messages) {
        this.messages = messages;
        return this;
    }

    public void validity() {
        validity(this);
    }

    public void validity(Object support) {
        for (GetData.Message message : this.getMessages()) {
            message.select().ifPresent(e -> {
                e.validity(support);
            });
        }
    }

    private static final int MSG_WITNESS_FLAG = 1 << 30;
    private static final int MSG_TYPE_MASK = 0xffffffff >>> 2;

    /**
     * sendcmpct
     * 1,1  cmpctblock
     * 0 !cmpctblock , invs or headers
     */
    public enum Type {
        UNDEFINED(0),
        MSG_TX(1),
        MSG_BLOCK(2),
        MSG_WTX(5),                                      //!< Defined in BIP 339
        // The following can only occur in getdata. Invs always use TX/WTX or BLOCK.
        MSG_FILTERED_BLOCK(3),                           //!< Defined in BIP37
        MSG_CMPCT_BLOCK(4),                              //!< Defined in BIP152
        MSG_WITNESS_BLOCK(MSG_BLOCK.inventory | MSG_WITNESS_FLAG), //!< Defined in BIP144
        MSG_WITNESS_TX(MSG_TX.inventory | MSG_WITNESS_FLAG),       //!< Defined in BIP144
        // MSG_FILTERED_WITNESS_BLOCK is defined in BIP144 as reserved for future
        // use and remains unused.
        // MSG_FILTERED_WITNESS_BLOCK = MSG_FILTERED_BLOCK | MSG_WITNESS_FLAG,
        ;
        public final int inventory;

        Type(int inventory) {
            this.inventory = inventory;
        }
    }
}
