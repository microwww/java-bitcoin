package com.github.microwww.bitcoin.wallet;

import com.github.microwww.bitcoin.util.ByteUtil;

import java.util.Date;

public class AccountDB {
    public enum Version {
        BASE58, BECH32
    }
    private int id;
    private String address;
    private byte[] KeyPrivate;
    private String tag;
    private Version version;
    private Date createTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public byte[] getKeyPrivate() {
        return KeyPrivate;
    }

    public void setKeyPrivate(byte[] keyPrivate) {
        KeyPrivate = keyPrivate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "AccountDB {" +
                "  id =" + id +
                ", address = 0x" + address +
                ", KeyPrivate = 0x" + ByteUtil.hex(KeyPrivate) +
                ", tag = " + tag +
                ", createTime = " + createTime +
                '}';
    }
}
