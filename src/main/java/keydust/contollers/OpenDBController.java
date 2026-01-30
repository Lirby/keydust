package keydust.contollers;

import keydust.db.SqliteDB;
import keydust.models.MetadataModel;
import keydust.passwordmanager.Password;

import java.sql.SQLException;

public class OpenDBController {

    SqliteDB sqlite;

    public OpenDBController(String path) {
        this.sqlite = new SqliteDB(path);

    }

    public boolean checkPassword(String password) throws SQLException {
        MetadataModel metadata = new MetadataModel(sqlite);

        String salt = metadata.getSalt();
        String pwdHash = metadata.getHash();

        Password pwd = new Password(password, salt);
        boolean ok = pwd.checkHash(pwdHash);

        if (ok) {
            metadata.getOrCreateEncSalt();
        }

        return ok;
    }

    public SqliteDB getSqlite() {
        return this.sqlite;
    }
}
