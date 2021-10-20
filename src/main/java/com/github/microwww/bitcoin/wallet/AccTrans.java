package com.github.microwww.bitcoin.wallet;

import java.util.Date;

public class AccTrans {
    private int id;
    private byte[] pkHash;
    private byte[] trans;
    private long amount;
    private Date createTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getPkHash() {
        return pkHash;
    }

    public void setPkHash(byte[] pkHash) {
        this.pkHash = pkHash;
    }

    public byte[] getTrans() {
        return trans;
    }

    public void setTrans(byte[] trans) {
        this.trans = trans;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
