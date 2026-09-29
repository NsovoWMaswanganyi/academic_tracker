package com.academic_tracker_app.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

public class Database {
    public static Path getDatabasePath() {
        String override = System.getProperty("academic.tracker.database");
        if (override != null) return Path.of(override).toAbsolutePath();
        String localData = System.getenv("LOCALAPPDATA");
        Path base = localData == null || localData.isBlank()
                ? Path.of(System.getProperty("user.home")) : Path.of(localData);
        return base.resolve("Academic Tracker").resolve("grades.db");
    }

    public static Connection connect() throws SQLException {
        Path database = getDatabasePath();
        try {
            Files.createDirectories(database.getParent());
            // SQLite's backup includes committed WAL data and leaves the original intact.
            Path legacy = Path.of(System.getProperty("academic.tracker.legacyDatabase", "grades.db"))
                    .toAbsolutePath();
            if (System.getProperty("academic.tracker.database") == null) migrateLegacy(database, legacy);
        } catch (IOException e) {
            throw new SQLException("Cannot prepare the grades folder: " + database.getParent(), e);
        }
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database);
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS modules(
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        moduleName TEXT NOT NULL,
                        credits INTEGER NOT NULL,
                        mark REAL NOT NULL
                    )
                    """);
            migrateFinalYear(connection);
            return connection;
        } catch (SQLException e) {
            connection.close();
            throw e;
        }
    }

    private static void migrateFinalYear(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet columns = statement.executeQuery("PRAGMA table_info(modules)")) {
            while (columns.next()) {
                if (columns.getString("name").equals("finalYear")) return;
            }
        }
        // Mark this user's existing group once; later edits/deletes never shift membership.
        boolean populated;
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT COUNT(*) FROM modules")) {
            populated = rows.next() && rows.getInt(1) > 0;
        }
        if (populated) {
            Path backupPath = getDatabasePath().resolveSibling("grades-before-final-year-"
                    + java.util.UUID.randomUUID() + ".db");
            try (PreparedStatement backup = connection.prepareStatement("VACUUM INTO ?")) {
                backup.setString(1, backupPath.toString());
                backup.execute();
            }
        }
        connection.setAutoCommit(false);
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE modules ADD COLUMN finalYear INTEGER NOT NULL DEFAULT 0");
            statement.executeUpdate("""
                    UPDATE modules SET finalYear = 1 WHERE id IN (
                        SELECT id FROM modules
                        WHERE id >= (SELECT MIN(id) FROM modules
                                     WHERE LOWER(TRIM(moduleName)) = 'financial management')
                        ORDER BY id LIMIT 10
                    )
                    """);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    static void migrateLegacy(Path database, Path legacy) throws SQLException, IOException {
        if (Files.exists(database) || !Files.isRegularFile(legacy) || database.equals(legacy)) return;
        Path backupPath = database.resolveSibling("grades-migration-" + java.util.UUID.randomUUID() + ".db");
        try {
            try (Connection source = DriverManager.getConnection("jdbc:sqlite:" + legacy);
                 PreparedStatement backup = source.prepareStatement("VACUUM INTO ?")) {
                backup.setString(1, backupPath.toString());
                backup.execute();
            }
            Files.move(backupPath, database);
        } finally {
            Files.deleteIfExists(backupPath);
        }
    }

    /** Snapshot for this user's installed launcher; never overwrite an existing backup. */
    public static void backupTo(Path destination) throws SQLException, IOException {
        destination = destination.toAbsolutePath();
        Files.createDirectories(destination.getParent());
        if (Files.exists(destination)) throw new IOException("Backup already exists: " + destination);
        try (Connection connection = connect();
             PreparedStatement backup = connection.prepareStatement("VACUUM INTO ?")) {
            backup.setString(1, destination.toString());
            backup.execute();
        }
    }
}
