package com.academic_tracker_app.database;

import com.academic_tracker_app.model.Module;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class RestartPersistenceTest {
    @TempDir Path directory;

    @Test void defaultDataLocationPersistsAcrossProcessesAndWorkingDirectories() throws Exception {
        run("add", directory.resolve("first-launch"));
        run("update", directory.resolve("second-launch"));
        run("verify", directory.resolve("third-launch"));
        run("delete", directory.resolve("fourth-launch"));
        run("verify-delete", directory.resolve("fifth-launch"));
    }

    private void run(String action, Path workingDirectory) throws Exception {
        Files.createDirectories(workingDirectory);
        String classpath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        String absoluteClasspath = Pattern.compile(Pattern.quote(System.getProperty("path.separator")))
                .splitAsStream(classpath).map(path -> Path.of(path).toAbsolutePath().toString())
                .collect(Collectors.joining(System.getProperty("path.separator")));
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        Path output = directory.resolve(action + ".log");
        ProcessBuilder builder = new ProcessBuilder(java, "--enable-native-access=ALL-UNNAMED",
                "-cp", absoluteClasspath, Probe.class.getName(), action);
        builder.environment().put("LOCALAPPDATA", directory.resolve("user-data").toString());
        builder.directory(workingDirectory.toFile());
        builder.redirectErrorStream(true).redirectOutput(output.toFile());
        Process process = builder.start();
        try {
            assertTrue(process.waitFor(20, TimeUnit.SECONDS), "Child process timed out: " + action);
            assertEquals(0, process.exitValue(), Files.readString(output));
        } finally {
            if (process.isAlive()) process.destroyForcibly();
        }
    }

    public static class Probe {
        public static void main(String[] args) throws Exception {
            if (System.getProperty("academic.tracker.database") != null)
                throw new AssertionError("Test must exercise the default database location.");
            ModuleDAO dao = new ModuleDAO();
            switch (args[0]) {
                case "add" -> {
                    Module finalYear = new Module("Persistent module", 12, 80);
                    finalYear.setFinalYear(true);
                    dao.addModule(finalYear);
                    dao.addModule(new Module("Second module", 24, 60));
                }
                case "update" -> {
                    var modules = dao.getModules();
                    if (modules.size() != 2) throw new AssertionError("Added grades did not survive exit.");
                    Module edited = modules.getFirst();
                    edited.setMark(95);
                    dao.updateModule(edited);
                }
                case "verify" -> {
                    var modules = dao.getModules();
                    if (modules.size() != 2 || modules.getFirst().getMark() != 95 || !modules.getFirst().isFinalYear())
                        throw new AssertionError("Updated grade did not survive exit.");
                }
                case "delete" -> dao.deleteModule(dao.getModules().getFirst().getId());
                case "verify-delete" -> {
                    var modules = dao.getModules();
                    if (modules.size() != 1 || !modules.getFirst().getModuleName().equals("Second module"))
                        throw new AssertionError("Delete did not persist or affected another module.");
                }
                default -> throw new AssertionError("Unknown action");
            }
        }
    }
}
