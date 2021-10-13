package com.github.microwww.bitcoin.chain;

import com.github.microwww.bitcoin.math.UintVar;
import com.github.microwww.bitcoin.script.TemplateTransaction;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.wallet.CoinAccount;
import io.netty.buffer.ByteBuf;

import java.math.BigDecimal;
import java.util.Optional;

public class TxOut {
    private long value;
    // private UintVar scriptLength;
    private byte[] scriptPubKey;

    public TxOut() {
    }

    public TxOut(long value) {
        this.value = value;
        //this.scriptLength = UintVar.ZERO;
        this.scriptPubKey = new byte[]{};
    }

    public void read(ByteBuf bf) {
        value = bf.readLongLE();
        UintVar scriptLength = UintVar.parse(bf);
        scriptPubKey = ByteUtil.readLength(bf, scriptLength.intValueExact());
    }

    public void write(ByteBuf bf) {
        bf.writeLongLE(value);
        //scriptLength = new Uint8(bf.readByte());
        UintVar.valueOf(scriptPubKey.length).write(bf);
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
        return UintVar.valueOf(scriptPubKey.length);
    }

    public byte[] getScriptPubKey() {
        return scriptPubKey;
    }

    public TxOut setScriptPubKey(byte[] scriptPubKey) {
        this.scriptPubKey = scriptPubKey;
        return this;
    }

    public Optional<TemplateTransaction> loadType() {
        return TemplateTransaction.select(scriptPubKey);
    }

    public Optional<CoinAccount.Address> loadAddress() {
        return TemplateTransaction.selectAddress(scriptPubKey);
    }

    @Override
    public String toString() {
        return toString(new StringBuilder("TxOut:"), "\n").toString();
    }

    public StringBuilder toString(StringBuilder append, String s) {
        return append
                .append(s).append(" value  = ").append(value)
                .append(s).append(" script = 0x").append(Integer.toUnsignedString(scriptPubKey.length, 16)).append(" ").append(ByteUtil.hex(scriptPubKey));
    }
}