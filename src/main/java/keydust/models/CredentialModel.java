package keydust.models;

import keydust.contollers.CryptoController;
import keydust.db.SqliteDB;
import java.sql.*;
import java.util.ArrayList;

public class CredentialModel extends Model {
    private final CryptoController crypto = new CryptoController();

    public CredentialModel(SqliteDB sqlite) {
        super(sqlite);
    }

    public void createTable() throws SQLException {
        String sqlCredentialsTable =
                "CREATE TABLE IF NOT EXISTS credential (" +
                        "id INTEGER PRIMARY KEY, "+
                        "description VARCHAR(255) NOT NULL, " +
                        "username VARCHAR(255) NOT NULL, " +
                        "password VARCHAR(255) NOT NULL)";

        createGenericTable(sqlCredentialsTable);
    }

    public void saveCredential(String masterPassword, String description, String username, String password) throws SQLException {
        String sql = "INSERT INTO credential (description, username, password) VALUES (?, ?, ?)";

        MetadataModel metadata = new MetadataModel(sqlite);
        byte[] encSalt = metadata.getOrCreateEncSalt();

        try (Connection connection = sqlite.getConnection();
            PreparedStatement st = connection.prepareStatement(sql)) {

            st.setString(1, crypto.encrypt(masterPassword, encSalt, description));
            st.setString(2, crypto.encrypt(masterPassword, encSalt, username));
            st.setString(3, crypto.encrypt(masterPassword, encSalt, password));
            st.executeUpdate();
        }
    }

    public ArrayList<String[]> loadCredentials(String masterPassword) throws SQLException {
        String sql = "SELECT * FROM credential";

        MetadataModel metadata = new MetadataModel(sqlite);
        byte[] encSalt = metadata.getOrCreateEncSalt();

        ArrayList<String[]> credentials = new ArrayList<>();

        try (Connection connection = sqlite.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int idInt = rs.getInt("id");
                String id = String.valueOf(idInt);
                String eDesc = rs.getString("description");
                String eUser = rs.getString("username");
                String ePwd = rs.getString("password");

                if (!crypto.isV2(eDesc) || !crypto.isV2(eUser) || !crypto.isV2(ePwd)) {
                    throw new IllegalStateException(
                            "Datenbase contains unsupported encryption. "
                    );
                }


                String desc = crypto.decrypt(masterPassword, encSalt, eDesc);
                String user = crypto.decrypt(masterPassword, encSalt, eUser);
                String pwd = crypto.decrypt(masterPassword, encSalt, ePwd);

                credentials.add(new String[]{id, desc, user, pwd});
            }
        }
        return credentials;
    }

    public void updateCredential(String masterPassword, int id, String description, String username, String password) throws SQLException {
        String sql = "UPDATE credential SET description = ?, username = ?, password = ? WHERE id =?";

        MetadataModel metadata = new MetadataModel(sqlite);
        byte[] encSalt = metadata.getOrCreateEncSalt();

        try (Connection connection = sqlite.getConnection();
            PreparedStatement st = connection.prepareStatement(sql)) {

            st.setString(1, crypto.encrypt(masterPassword, encSalt, description));
            st.setString(2, crypto.encrypt(masterPassword, encSalt, username));
            st.setString(3, crypto.encrypt(masterPassword, encSalt, password));
            st.setInt(4, id);
            st.executeUpdate();
        }
    }
}
