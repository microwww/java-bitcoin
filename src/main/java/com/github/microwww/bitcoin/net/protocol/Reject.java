package com.github.microwww.bitcoin.net.protocol;

import com.github.microwww.bitcoin.math.UintVar;
import com.github.microwww.bitcoin.provider.Peer;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class Reject extends AbstractProtocolAdapter<Reject> {
    private String message;
    private byte code;
    private String reason;
    private byte[] data = new byte[]{};

    public Reject(Peer peer) {
        super(peer);
    }

    @Override
    protected Reject read0(ByteBuf buf) {
        byte[] bytes = UintVar.parseAndRead(buf);
        message = new String(bytes);
        code = buf.readByte();
        byte[] byts = UintVar.parseAndRead(buf);
        reason = new String(byts);
        data = ByteUtil.readAll(buf);
        return this;
    }

    @Override
    protected void write0(ByteBuf buf) {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        UintVar.writeData(buf, bytes);
        buf.writeBytes(bytes);
        buf.writeByte(code);
        bytes = reason.getBytes(StandardCharsets.UTF_8);
        UintVar.writeData(buf, bytes);
        buf.writeBytes(data);
    }


    public enum Code {
        REJECT_MALFORMED(0x01),
        REJECT_INVALID(0x10),
        REJECT_OBSOLETE(0x11),
        REJECT_DUPLICATE(0x12),
        REJECT_NONSTANDARD(0x40),
        REJECT_DUST(0x41),
        REJECT_INSUFFICIENTFEE(0x42),
        REJECT_CHECKPOINT(0x43),
        ;
        public final byte code;

        Code(int code) {
            this.code = (byte) code;
        }

        public static Code select(int code) {
            for (Code value : Code.values()) {
                if (value.code == code) {
                    return value;
                }
            }
            return null;
        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
