package keydust.models;

import keydust.db.SqliteDB;

import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;

public class MetadataModel extends Model {

    private static final String SQL_SELECT_VALUE =
            "SELECT value FROM metadata WHERE key = ? ORDER BY rowid DESC LIMIT 1";

    private static final String SQL_UPDATE_VALUE =
            "UPDATE metadata SET value = ? WHERE key = ?";

    private static final String SQL_INSERT_VALUE =
            "INSERT INTO metadata (key, value) VALUES (?, ?)";

    public MetadataModel(SqliteDB sqlite) {
        super(sqlite);
    }

    public void createTable() throws SQLException {
        String sql =
                "CREATE TABLE IF NOT EXISTS metadata (" +
                        "key VARCHAR(255) NOT NULL," +
                        "value VARCHAR(255) NOT NULL" +
                        ")";
        createGenericTable(sql);
    }

    public void saveHash(String hash) throws SQLException { saveData("hash", hash); }
    public String getHash() throws SQLException { return  getValue("hash"); }

    public void saveSalt(String salt) throws SQLException { saveData("salt", salt); }
    public String getSalt() throws SQLException { return getValue("salt"); }

    public void saveEncSalt(String encSaltBase64) throws SQLException { saveData("enc_salt", encSaltBase64); }
    public String getEncSalt() throws SQLException { return  getValue("enc_salt"); }

    public byte[] getOrCreateEncSalt() throws SQLException {
        String v = getEncSalt();
        if (v != null && !v.isBlank()) {
            return Base64.getDecoder().decode(v);
        }
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        saveEncSalt(Base64.getEncoder().encodeToString(salt));
        return salt;
    }

    private String getValue(String key) throws  SQLException {
        try (Connection con = sqlite.getConnection();
            PreparedStatement ps = con.prepareStatement(SQL_SELECT_VALUE)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return  rs.getString("value");
            }
        }
        return "";
    }

    private void saveData(String name, String data) throws  SQLException {
        try (Connection con = sqlite.getConnection()) {
            try (PreparedStatement upd = con.prepareStatement(SQL_UPDATE_VALUE)) {
                upd.setString(1, data);
                upd.setString(2, name);
                if (upd.executeUpdate() > 0) return;
            }
            try (PreparedStatement ins = con.prepareStatement(SQL_INSERT_VALUE)) {
                ins.setString(1, name);
                ins.setString(2, data);
                ins.executeUpdate();
            }
        }
    }

}
