package com.github.microwww.bitcoin.wallet;

import com.github.microwww.bitcoin.util.ByteUtil;
import org.h2.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// @Component in @Bean
public class Wallet implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(Wallet.class);
    private static final String TABLE_NAME = "account";

    public static String name = "h2wallet";
    private final File wallet;
    private final Connection conn;

    static {
        try {
            Class.forName(Driver.class.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Wallet(File root) throws SQLException, IOException {
        wallet = new File(new File(root, "wallet"), name).getCanonicalFile();
        conn = DriverManager.getConnection("jdbc:h2:file:" + wallet.getCanonicalPath());
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
                    "address VARCHAR(64) NOT NULL," +
                    "key_private BLOB(1024) NOT NULL," +
                    "tag VARCHAR(100) NOT NULL," +
                    "create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "PRIMARY KEY (id)," +
                    "UNIQUE INDEX address (address)" +
                    ")");
            //conn.createStatement().execute("CREATE SEQUENCE IF NOT EXISTS RANDOM_USER.RANDOMTABLE_SEQ START WITH 1 INCREMENT BY 1");
            log.info("CREATE TABLE account : {}", wallet.getCanonicalPath());
        }
    }

    public void insert(String tag, CoinAccount.Address kp) {
        insert(tag, new byte[]{}, kp.getKeyPublicHash());
    }

    public void insert(String tag, CoinAccount.KeyPrivate kp) {
        insert(tag, kp.getKey(), kp.getAddress().getKeyPublicHash());
    }

    public List<AccountDB> listAddress() {
        List<AccountDB> res = new ArrayList<>();
        try {
            ResultSet ps = conn.createStatement().executeQuery("select id, address, key_private, tag, create_time from " + TABLE_NAME);
            while (ps.next()) {
                AccountDB ad = new AccountDB();
                ad.setId(ps.getInt(1));
                ad.setAddress(ps.getString(2));
                ad.setKeyPrivate(ps.getBytes(3));
                ad.setTag(ps.getString(4));
                ad.setCreateTime(ps.getTimestamp(5));
                res.add(ad);
            }
            return res;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void insert(String tag, byte[] keyPrivate, byte[] address) {
        try {
            String hex = ByteUtil.hex(address);
            PreparedStatement ps = conn.prepareStatement("select address from " + TABLE_NAME + " t where t.address = ?");
            ps.setString(1, hex);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                log.warn("Address exist, skip : {}", hex);
                return;
            }
            ps = conn.prepareStatement("insert into " + TABLE_NAME + "(address, key_private, tag) values(?,?,?)");
            ps.setString(1, hex);
            ps.setBytes(2, keyPrivate);
            ps.setString(3, tag);
            ps.execute();
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
}
