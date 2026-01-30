package keydust.passwordmanager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.prefs.Preferences;

public class PrefsOpt {
    private static final Preferences PREFS = Preferences.userRoot().node("KeyDust");
    private static final String LAST_DB = "last_db";

    private PrefsOpt() {}

    public static void setLastDbPath(String path) {
        if (path == null || path.isBlank()) return;
        PREFS.put(LAST_DB, path);
    }

    public static Optional<String> getLastDbPath() {
        String p = PREFS.get(LAST_DB, "");
        if (p == null || p.isBlank()) return Optional.empty();

        if (!Files.exists(Path.of(p))) return Optional.empty();

        return Optional.of(p);
    }

    public static void clearDbPath() {
        PREFS.remove(LAST_DB);
    }
}
