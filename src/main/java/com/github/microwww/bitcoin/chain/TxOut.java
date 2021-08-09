package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.math.UintVar;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;

public class TxOut {
    private long value;
    private UintVar scriptLength;
    private byte[] scriptPubKey;

    public TxOut() {
    }

    public TxOut(long value) {
        this.value = value;
        this.scriptLength = UintVar.ZERO;
        this.scriptPubKey = new byte[]{};
    }

    public void read(ByteBuf bf) {
        value = bf.readLongLE();
        scriptLength = UintVar.parse(bf);
        scriptPubKey = ByteUtil.readLength(bf, scriptLength.intValueExact());
    }

    public void write(ByteBuf bf) {
        bf.writeLongLE(value);
        //scriptLength = new Uint8(bf.readByte());
        bf.writeByte(scriptPubKey.length);
        bf.writeBytes(scriptPubKey);
    }

    public long getValue() {
        return value;
    }

    public TxOut setValue(long value) {
        this.value = value;
        return this;
    }

    public UintVar getScriptLength() {
        return scriptLength;
    }

    public byte[] getScriptPubKey() {
        return scriptPubKey;
    }

    public TxOut setScriptPubKey(byte[] scriptPubKey) {
        this.scriptPubKey = scriptPubKey;
        return this;
    }

    @Override
    public String toString() {
        return toString(new StringBuilder("TxOut:"), "\n").toString();
    }

    public StringBuilder toString(StringBuilder append, String s) {
        return append
                .append(s).append(" value  = ").append(value)
                .append(s).append(" script = 0x").append(Integer.toUnsignedString(scriptLength.intValue(), 16)).append(" ").append(ByteUtil.hex(scriptPubKey));
    }
}