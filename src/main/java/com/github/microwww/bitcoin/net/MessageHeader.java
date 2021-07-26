package com.github.microwww.bitcoin.net;

import com.github.microwww.bitcoin.net.protocol.UnsupportedNetProtocolException;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import org.springframework.util.Assert;

import java.util.Arrays;

/**
 * Message header.
 * (4) message start.
 * (12) command.
 * (4) size.
 * (4) checksum.
 */
public class MessageHeader {
    public static final int MESSAGE_START_SIZE = 4;
    public static final int COMMAND_SIZE = 12;
    public static final int MESSAGE_SIZE_SIZE = 4;
    public static final int CHECKSUM_SIZE = 4;
    public static final int MESSAGE_SIZE_OFFSET = MESSAGE_START_SIZE + COMMAND_SIZE;
    public static final int CHECKSUM_OFFSET = MESSAGE_SIZE_OFFSET + MESSAGE_SIZE_SIZE;
    public static final int HEADER_SIZE = MESSAGE_START_SIZE + COMMAND_SIZE + MESSAGE_SIZE_SIZE + CHECKSUM_SIZE;

    private int magic = 0xf9beb4d9;// 0xfabfb5da
    private String command;
    private int length; // read 时候使用
    private byte[] checksum; // 4
    private byte[] payload;

    public MessageHeader() {
    }

    public MessageHeader(int magic, NetProtocol command) {
        this.magic = magic;
        this.command = command.cmd();
    }

    public int getMagic() {
        return magic;
    }

    public MessageHeader setMagic(int magic) {
        this.magic = magic;
        return this;
    }

    public NetProtocol getNetProtocol() throws UnsupportedNetProtocolException {
        return NetProtocol.select(command);
    }

    public String getCommand() {
        return this.command;
    }

    public MessageHeader setCommand(NetProtocol command) {
        this.command = command.name();
        return this;
    }

    public MessageHeader setCommand(String command) {
        this.command = command;
        return this;
    }

    public byte[] getPayload() {
        return payload;
    }

    public MessageHeader setPayload(byte[] payload) {
        this.payload = payload;
        return this;
    }

    public MessageHeader writer(ByteBuf out) {
        checksum = checksum(payload);
        out.writeInt(magic).writeBytes(this.getNetProtocol().toByte(COMMAND_SIZE)).writeIntLE(payload.length).writeBytes(checksum).writeBytes(payload);
        return this;
    }

    public static byte[] checksum(byte[] payload) {
        return Arrays.copyOf(ByteUtil.sha256sha256(payload), CHECKSUM_SIZE);
    }

    public boolean verifyChecksum() {
        if (checksum == null || checksum.length != CHECKSUM_SIZE) {
            return false;
        }
        byte[] sha256 = ByteUtil.sha256sha256(payload);
        for (int i = 0; i < CHECKSUM_SIZE; i++) {
            if (checksum[i] != sha256[i]) {
                return false;
            }
        }
        return true;
    }

    public static MessageHeader read(ByteBuf out) {
        MessageHeader messageHeader = readHeader(out);
        return readBody(messageHeader, out);
    }

    public static MessageHeader readHeader(ByteBuf out) {
        int length = out.readableBytes();
        Assert.isTrue(length >= HEADER_SIZE, "Head length error !");
        MessageHeader header = new MessageHeader();
        // byte[] checksum = checksum(payload);
        header.setMagic(out.readInt());
        byte[] cmd = new byte[COMMAND_SIZE];
        out.readBytes(cmd);
        header.setCommand(NetProtocol.toType(cmd));
        header.length = out.readIntLE();
        byte[] ck = new byte[CHECKSUM_SIZE];
        out.readBytes(ck);
        header.checksum = ck;
        return header;
    }

    public static MessageHeader readBody(MessageHeader header, ByteBuf out) {
        int len = header.length;
        Assert.isTrue(len >= 0, "PAYLOAD length >= 0");
        byte[] payload = new byte[len];
        if (len > 0) {
            out.readBytes(payload);
        }
        header.setPayload(payload);
        return header;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "MessageHeader{" +
                "magic=" + magic +
                ", command='" + command + '\'' +
                ", payload=" + Arrays.toString(payload) +
                ", checksum=" + Arrays.toString(checksum) +
                '}';
    }
}
