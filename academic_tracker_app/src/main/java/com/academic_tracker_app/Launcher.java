package com.academic_tracker_app;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) throws Exception {
        if ((args.length == 2 || args.length == 3) && args[0].equals("--initialize-data")) {
            System.setProperty("academic.tracker.legacyDatabase", args[1]);
            try (var connection = com.academic_tracker_app.database.Database.connect()) {
                System.out.println("Grades ready: " + com.academic_tracker_app.database.Database.getDatabasePath());
            }
            if (args.length == 3) {
                com.academic_tracker_app.database.Database.backupTo(java.nio.file.Path.of(args[2]));
            }
            return;
        }
        Application.launch(AcademicTrackerApplication.class, args);
    }
}
