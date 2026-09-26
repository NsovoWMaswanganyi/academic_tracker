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
}
