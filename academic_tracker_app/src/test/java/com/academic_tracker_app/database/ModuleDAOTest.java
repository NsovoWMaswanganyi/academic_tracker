package com.academic_tracker_app.database;

import com.academic_tracker_app.model.Module;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

class ModuleDAOTest {
    @TempDir Path directory;
    ModuleDAO dao = new ModuleDAO();
    @BeforeEach void setup() { System.setProperty("academic.tracker.database", directory.resolve("grades.db").toString()); }
    @AfterEach void cleanup() { System.clearProperty("academic.tracker.database"); }

    @Test void updateAndDeleteOnlySelectedIdAndPersistAcrossConnections() throws Exception {
        dao.addModule(new Module("Statistics", 12, 80));
        dao.addModule(new Module("Statistics", 12, 80));
        var original = dao.getModules();
        Module edited = new Module("Statistics II", 24, 95.5);
        edited.setId(original.getFirst().getId());
        dao.updateModule(edited);
        var updated = new ModuleDAO().getModules();
        assertEquals(2, updated.size());
        assertEquals("Statistics II", updated.getFirst().getModuleName());
        assertEquals(24, updated.getFirst().getCredits());
        assertEquals(95.5, updated.getFirst().getMark());
        assertEquals("Statistics", updated.getLast().getModuleName());
        dao.deleteModule(edited.getId());
        assertEquals(original.getLast().getId(), dao.getModules().getFirst().getId());
        dao.deleteModule(original.getLast().getId());
        assertTrue(dao.getModules().isEmpty());
    }

    @Test void missingRowsAndStorageErrorsAreReported() throws Exception {
        Module missing = new Module("Missing", 12, 75);
        missing.setId(1234);
        assertThrows(SQLException.class, () -> dao.updateModule(missing));
        assertThrows(SQLException.class, () -> dao.deleteModule(1234));
        System.setProperty("academic.tracker.database", directory.toString());
        assertThrows(SQLException.class, () -> dao.getModules());
    }

    @Test void migrationPreservesOriginalAndNeverOverwritesExistingData() throws Exception {
        dao.addModule(new Module("Existing grade", 16, 88));
        Path source = Database.getDatabasePath();
        Path destination = directory.resolve("migrated.db");
        byte[] original = Files.readAllBytes(source);
        Database.migrateLegacy(destination, source);
        assertArrayEquals(original, Files.readAllBytes(source));
        System.setProperty("academic.tracker.database", destination.toString());
        assertEquals("Existing grade", dao.getModules().getFirst().getModuleName());
        dao.addModule(new Module("New grade", 8, 91));
        Database.migrateLegacy(destination, source);
        assertEquals(2, dao.getModules().size());
    }

    @Test void migrationFailureDoesNotLeaveAnEmptyDestination() throws Exception {
        Path corrupt = directory.resolve("corrupt.db");
        Files.writeString(corrupt, "This is not a database.");
        Path destination = directory.resolve("migrated.db");
        assertThrows(SQLException.class, () -> Database.migrateLegacy(destination, corrupt));
        assertFalse(Files.exists(destination));
    }

    @Test void installerSnapshotPreservesGradesAndCannotOverwriteBackup() throws Exception {
        dao.addModule(new Module("Saved module", 12, 84));
        Path snapshot = directory.resolve("installed-app/first-run-grades.db");
        Database.backupTo(snapshot);
        byte[] originalSnapshot = Files.readAllBytes(snapshot);
        dao.addModule(new Module("Later addition", 24, 90));
        assertThrows(java.io.IOException.class, () -> Database.backupTo(snapshot));
        assertArrayEquals(originalSnapshot, Files.readAllBytes(snapshot));
        Path firstLaunch = directory.resolve("first-launch.db");
        Database.migrateLegacy(firstLaunch, snapshot);
        System.setProperty("academic.tracker.database", firstLaunch.toString());
        assertEquals(1, dao.getModules().size());
        assertEquals("Saved module", dao.getModules().getFirst().getModuleName());
        dao.deleteModule(dao.getModules().getFirst().getId());
        Database.migrateLegacy(firstLaunch, snapshot);
        assertTrue(dao.getModules().isEmpty(), "An intentionally emptied database must stay empty.");
    }

    @Test void migratesExactlyTenFinalYearModulesOnceWithoutChangingMarks() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:sqlite:" + Database.getDatabasePath());
             var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE modules(id INTEGER PRIMARY KEY AUTOINCREMENT, moduleName TEXT NOT NULL, credits INTEGER NOT NULL, mark REAL NOT NULL)");
            try (var insert = connection.prepareStatement("INSERT INTO modules(moduleName,credits,mark) VALUES(?,12,80)")) {
                for (int i = 1; i <= 38; i++) {
                    insert.setString(1, i == 28 ? " Financial Management " : "Module " + i);
                    insert.executeUpdate();
                }
            }
        }
        var migrated = dao.getModules();
        assertEquals(38, migrated.size());
        assertEquals(10, migrated.stream().filter(Module::isFinalYear).count());
        for (Module module : migrated) {
            assertEquals(module.getId() >= 28 && module.getId() <= 37, module.isFinalYear());
            assertEquals(12, module.getCredits());
            assertEquals(80, module.getMark());
        }
        try (var files = Files.list(directory)) {
            assertEquals(1, files.filter(file -> file.getFileName().toString().startsWith("grades-before-final-year-")).count());
        }
        Module renamed = migrated.get(27);
        renamed.setModuleName("Finance renamed");
        dao.updateModule(renamed);
        assertTrue(dao.getModules().get(27).isFinalYear());
        renamed.setFinalYear(false);
        dao.updateModule(renamed);
        dao.deleteModule(37);
        var reopened = new ModuleDAO().getModules();
        assertEquals(8, reopened.stream().filter(Module::isFinalYear).count());
        assertFalse(reopened.getLast().isFinalYear(), "The next module must not replace a deleted final-year module.");
    }
}
