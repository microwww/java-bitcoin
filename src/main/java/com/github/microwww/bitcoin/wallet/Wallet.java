package com.github.microwww.bitcoin.wallet;

import com.github.microwww.bitcoin.chain.RawTransaction;
import com.github.microwww.bitcoin.chain.TxOut;
import com.github.microwww.bitcoin.conf.CChainParams;
import com.github.microwww.bitcoin.math.Uint256;
import com.github.microwww.bitcoin.script.PubKeyScript;
import com.github.microwww.bitcoin.util.ByteUtil;
import com.github.microwww.bitcoin.wallet.util.Base58;
import org.h2.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bitcoin-core 使用种子地址, 这里使用随机地址
 */
public class Wallet implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(Wallet.class);

    public static final String ACCOUNT_TABLE_NAME = "account";
    public static final String SELECT_ACCOUNT = "select id, pk_hash, key_private, type, tag, create_time from " + ACCOUNT_TABLE_NAME;
    public static final int DEFAULT_TYPE = 1;
    public static final String TRANSACTION_TABLE_NAME = "transaction";
    public static final String SELECT_TRANSACTION = "select id, pk_hash, trans, amount, create_time from " + TRANSACTION_TABLE_NAME;

    public static String name = "h2wallet";
    private final File walletRoot;
    private final Connection conn;
    private final Env env;
    private Map<Uint256, AccountDB> accounts = new ConcurrentHashMap<>();

    private static Wallet wallet;

    static {
        try {
            Class.forName(Driver.class.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Wallet wallet(CChainParams params) {
        try {
            if (wallet == null || wallet.conn.isClosed()) {
                return create(params);
            }
            return wallet;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized Wallet create(CChainParams params) throws SQLException, IOException {
        if (wallet == null || wallet.conn.isClosed()) {
            Wallet w = new Wallet(new File(params.settings.getDataDir()), params.env.addressType());
            w.init();
            wallet = w;
        }
        return wallet;
    }

    private Wallet(File root, Env env) {
        try {
            this.walletRoot = new File(new File(root, "wallet"), name).getCanonicalFile();
            conn = DriverManager.getConnection("jdbc:h2:file:" + this.walletRoot.getCanonicalPath());
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
        log.info("Wallet dir : {}", walletRoot.getParentFile().getCanonicalPath());
        ResultSet main = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"});
        if (main.next()) {
            log.info("TABLE {} EXIST, {}", ACCOUNT_TABLE_NAME, walletRoot.getCanonicalPath());
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
            conn.createStatement().execute("CREATE TABLE " + ACCOUNT_TABLE_NAME + " (" +
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
            log.info("CREATE TABLE {}, address : {}, in {}", ACCOUNT_TABLE_NAME, kp.getAddress().toBase58Address(env), walletRoot.getCanonicalPath());

            conn.createStatement().execute("CREATE TABLE " + TRANSACTION_TABLE_NAME + " (" +
                    " id INT NOT NULL AUTO_INCREMENT," +
                    " pk_hash VARCHAR(64) NOT NULL," +
                    " trans VARCHAR(64) NOT NULL," +
                    " amount BIGINT NOT NULL," +
                    " create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    " PRIMARY KEY (id)" +
                    ")");
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

    public void localTransaction(RawTransaction trans) {
        Uint256 hash = trans.hash();
        for (TxOut out : trans.getTxOuts()) {
            PubKeyScript scr = out.getScriptTemplate();
            // scr.getAddress(trans, env).ifPresent(System.out::println);
            scr.getAddress().ifPresent(e -> {
                byte[] kph = e.getKeyPublicHash();
                if (accounts.containsKey(new Uint256(kph))) {
                    AccTrans at = new AccTrans();
                    at.setPkHash(kph);
                    at.setTrans(hash.reverse256bit());
                    at.setAmount(out.getValue());
                    this.insertTransaction(at);
                }
            });
        }
    }

    public AccTrans insertTransaction(AccTrans trans) {
        try {
            String nt = Base58.encode(trans.getTrans());
            PreparedStatement ps = conn.prepareStatement(SELECT_TRANSACTION + " t where t.trans = ?");
            ps.setString(1, nt);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                log.warn("trans exist, skip : {}", ByteUtil.hex(trans.getTrans()));
            } else {
                PreparedStatement ips = conn.prepareStatement("insert into " + TRANSACTION_TABLE_NAME + "(pk_hash, trans, amount) values(?,?,?)");
                ips.setString(1, Base58.encode(trans.getPkHash()));
                ips.setString(2, nt);
                ips.setLong(3, trans.getAmount());
                ips.execute();
                rs = ps.executeQuery();
                rs.next();
            }
            return mapperTrans(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<AccTrans> selectTransaction(byte[] address) {
        try {
            List<AccTrans> list = new ArrayList<>();
            String nt = Base58.encode(address);
            PreparedStatement ps = conn.prepareStatement(SELECT_TRANSACTION + " t where t.pk_hash = ?");
            ps.setString(1, nt);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapperTrans(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

    private synchronized AccountDB insert(String tag, byte[] keyPrivate, byte[] address, int type) {
        try {
            String addr = Base58.encode(address);
            PreparedStatement ps = conn.prepareStatement(SELECT_ACCOUNT + " t where t.pk_hash = ?");
            ps.setString(1, addr);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                log.warn("pk_hash (address) exist, skip : {}", addr);
            } else {
                PreparedStatement ips = conn.prepareStatement("insert into " + ACCOUNT_TABLE_NAME + "(pk_hash, key_private, type, tag) values(?,?,?,?)");
                ips.setString(1, addr);
                ips.setString(2, Base58.encode(keyPrivate));
                ips.setInt(3, type);
                ips.setString(4, tag);
                ips.execute();
                rs = ps.executeQuery();
                rs.next();
            }
            AccountDB db = mapper(rs);
            accounts.put(new Uint256(db.getPkHash()), db);
            return db;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<Uint256, AccountDB> getAccounts() {
        return Collections.unmodifiableMap(accounts);
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

    private AccTrans mapperTrans(ResultSet ps) throws SQLException {
        AccTrans ad = new AccTrans();
        int i = 1;
        ad.setId(ps.getInt(i++));
        ad.setPkHash(Base58.decode(ps.getString(i++)));
        ad.setTrans(Base58.decode(ps.getString(i++)));
        ad.setAmount(ps.getLong(i++));
        ad.setCreateTime(ps.getTimestamp(i++));
        Assert.isTrue(i == 6, "Read 5+");
        return ad;
    }

    public Env getEnv() {
        return env;
    }
}
