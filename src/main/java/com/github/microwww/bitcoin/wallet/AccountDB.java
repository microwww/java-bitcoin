package com.github.microwww.bitcoin.wallet;

import com.github.microwww.bitcoin.util.ByteUtil;

import java.util.Date;

public class AccountDB {
    private final Env env;
    private int id;
    private byte[] pkHash;
    private byte[] keyPrivate;
    private int type;// 0: p2pk, 1: witness, 2: p2sh
    private String tag;
    private Date createTime;

    public AccountDB(Env env) {
        this.env = env;
    }

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
        return keyPrivate;
    }

    public void setKeyPrivate(byte[] keyPrivate) {
        this.keyPrivate = keyPrivate;
    }

    public byte[] getPkHash() {
        return pkHash;
    }

    public String toAddress() {
        if (type == 1) {
            return new CoinAccount.Address(pkHash).toBech32Address(env);
        } else if (type == 2) {
            return new CoinAccount.Address(pkHash).toP2SHAddress(env);
        }
        return new CoinAccount.Address(pkHash).toBase58Address(env);
    }

    public void setPkHash(byte[] pkHash) {
        this.pkHash = pkHash;
    }

    public boolean isWitness() {
        return this.type == 1;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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
                ", address = 0x" + ByteUtil.hex(pkHash) +
                ", KeyPrivate = 0x" + ByteUtil.hex(keyPrivate) +
                ", tag = " + tag +
                ", createTime = " + createTime +
                '}';
    }
}
