package com.github.microwww.bitcoin.wallet;

import com.github.microwww.bitcoin.wallet.util.Base58;
import org.h2.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Bitcoin-core 使用种子地址, 这里使用随机地址
 */
public class Wallet implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(Wallet.class);

    public static final String TABLE_NAME = "account";
    public static final String SELECT_ACCOUNT = "select id, pk_hash, key_private, type, tag, create_time from " + TABLE_NAME;
    public static final int DEFAULT_TYPE = 1;

    public static String name = "h2wallet";
    private final File wallet;
    private final Connection conn;
    private final Env env;

    static {
        try {
            Class.forName(Driver.class.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Wallet(File root, Env env) {
        try {
            wallet = new File(new File(root, "wallet"), name).getCanonicalFile();
            conn = DriverManager.getConnection("jdbc:h2:file:" + wallet.getCanonicalPath());
            this.env = env;
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public AccountDB getCoinBaseAddress() {
        try {
            PreparedStatement ps = conn.prepareStatement(SELECT_ACCOUNT + " t order by id limit 1");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapper(rs);
            }
        } catch (SQLException throwables) {
            log.error("SELECT coinbase error !", throwables);
        }
        throw new RuntimeException("Not find coinbase-address");
    }

    public void init() throws SQLException, IOException {
        log.info("Wallet dir : {}", wallet.getParentFile().getCanonicalPath());
        ResultSet main = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"});
        if (main.next()) {
            log.info("TABLE {} EXIST, {}", TABLE_NAME, wallet.getCanonicalPath());
            if (log.isDebugEnabled()) {
                ResultSetMetaData metaData = main.getMetaData();
                int c = metaData.getColumnCount();
                String[] mc = new String[c];
                int max = 0;
                for (int i = 0; i < c; i++) {
                    // TABLE_CATALOG, TABLE_SCHEMA, TABLE_NAME, TABLE_TYPE, REMARKS, TYPE_NAME, TYPE_NAME, TYPE_NAME,TYPE_NAME,TYPE_NAME,SQL
                    mc[i] = metaData.getColumnName(i + 1);
                    max = Math.max(max, mc[i].length());
                }
                StringBuilder sb = new StringBuilder();
                do {
                    for (int i = 0; i < c; i++) {
                        String string = main.getString(i + 1);
                        if (string != null) {
                            string = string.replaceAll("\\n", "\\\\n");
                        }
                        sb.append(String.format("%" + max + "s : ", mc[i])).append(string).append("\n");
                    }
                } while (main.next());
                log.debug("TABLE : \n" + sb);
            }
        } else {
            conn.createStatement().execute("CREATE TABLE " + TABLE_NAME + " (" +
                    "id int(11) NOT NULL auto_increment," +
                    "pk_hash VARCHAR(64) NOT NULL," +
                    "key_private VARCHAR(255) NOT NULL," +
                    "type int(11) NOT NULL DEFAULT '0'," +
                    "tag VARCHAR(100) NOT NULL," +
                    "create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "PRIMARY KEY (id)," +
                    "UNIQUE INDEX address (pk_hash)" +
                    ")");
            //conn.createStatement().execute("CREATE SEQUENCE IF NOT EXISTS RANDOM_USER.RANDOMTABLE_SEQ START WITH 1 INCREMENT BY 1");
            byte[] bytes = Secp256k1.generatePrivateKey();
            CoinAccount.KeyPrivate kp = new CoinAccount.KeyPrivate(bytes);
            this.insert("", kp, 0);
            log.info("CREATE TABLE {}, address : {}, in {}", TABLE_NAME, kp.getAddress().toBase58Address(env), wallet.getCanonicalPath());
        }
    }

    public AccountDB gen() {
        return gen("");
    }

    public AccountDB gen(String tag) {
        CoinAccount.KeyPrivate prv = new CoinAccount.KeyPrivate(Secp256k1.generatePrivateKey());
        return insert(tag, prv);
    }

    public AccountDB insert(String tag, CoinAccount.Address kp, int type) {
        return insert(tag, new byte[]{}, kp.getKeyPublicHash(), type);
    }

    public AccountDB insert(String tag, CoinAccount.Address kp) {
        return insert(tag, new byte[]{}, kp.getKeyPublicHash(), DEFAULT_TYPE);
    }

    public AccountDB insert(String tag, CoinAccount.KeyPrivate kp, int type) {
        return insert(tag, kp.getKey(), kp.getAddress().getKeyPublicHash(), type);
    }

    public AccountDB insert(String tag, CoinAccount.KeyPrivate kp) {
        return insert(tag, kp.getKey(), kp.getAddress().getKeyPublicHash(), DEFAULT_TYPE);
    }

    public List<AccountDB> listAddress() {
        List<AccountDB> res = new ArrayList<>();
        try {
            ResultSet ps = conn.createStatement().executeQuery(SELECT_ACCOUNT);
            while (ps.next()) {
                res.add(mapper(ps));
            }
            return res;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private AccountDB insert(String tag, byte[] keyPrivate, byte[] address, int type) {
        try {
            String addr = Base58.encode(address);
            PreparedStatement ps = conn.prepareStatement(SELECT_ACCOUNT + " t where t.pk_hash = ?");
            ps.setString(1, addr);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                log.warn("pk_hash (address) exist, skip : {}", addr);
            } else {
                PreparedStatement ips = conn.prepareStatement("insert into " + TABLE_NAME + "(pk_hash, key_private, type, tag) values(?,?,?,?)");
                ips.setString(1, addr);
                ips.setString(2, Base58.encode(keyPrivate));
                ips.setInt(3, type);
                ips.setString(4, tag);
                ips.execute();
                rs = ps.executeQuery();
                rs.next();
            }
            return mapper(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            conn.close();
        } catch (SQLException e) {
            log.warn("Close H2 error", e);
        }
    }

    private AccountDB mapper(ResultSet ps) throws SQLException {
        AccountDB ad = new AccountDB(env);
        int i = 1;
        ad.setId(ps.getInt(i++));
        ad.setPkHash(Base58.decode(ps.getString(i++)));
        ad.setKeyPrivate(Base58.decode(ps.getString(i++)));
        ad.setType(ps.getInt(i++));
        ad.setTag(ps.getString(i++));
        ad.setCreateTime(ps.getTimestamp(i++));
        Assert.isTrue(i == 7, "Read 6+");
        return ad;
    }

    public Env getEnv() {
        return env;
    }
}
