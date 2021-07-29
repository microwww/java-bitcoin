package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.math.Uint8;
import com.github.microwww.bitcoin.math.UintVar;
import com.github.microwww.bitcoin.util.ByteUtil;
import io.netty.buffer.ByteBuf;

public class TxOut {
    public long value;
    public UintVar scriptLength;
    public byte[] scriptPubKey;

    public void read(ByteBuf bf) {
        value = bf.readLongLE();
        scriptLength = UintVar.reader(bf);
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

    public void setValue(long value) {
        this.value = value;
    }

    public UintVar getScriptLength() {
        return scriptLength;
    }

    public byte[] getScriptPubKey() {
        return scriptPubKey;
    }

    public void setScriptPubKey(byte[] scriptPubKey) {
        this.scriptPubKey = scriptPubKey;
    }

    @Override
    public String toString() {
        return "TxOut{" +
                "value=" + value +
                ", scriptLength=" + scriptLength +
                ", script=" + ByteUtil.hex(scriptPubKey) +
                '}';
    }
}