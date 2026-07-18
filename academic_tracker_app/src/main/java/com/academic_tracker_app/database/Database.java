package com.academic_tracker_app.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String URL = "jdbc:sqlite:grades.db";

    public static Connection connect() {

        try {

            Connection conn = DriverManager.getConnection(URL);

            createTable(conn);

            return conn;

        }

        catch (SQLException e) {

            e.printStackTrace();

            return null;

        }

    }

    private static void createTable(Connection conn) {

        String sql = """
                CREATE TABLE IF NOT EXISTS modules(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    moduleName TEXT NOT NULL,
                    credits INTEGER NOT NULL,
                    mark REAL NOT NULL
                );
                """;

        try {

            Statement stmt = conn.createStatement();

            stmt.execute(sql);

        }

        catch (SQLException e) {

            e.printStackTrace();

        }

    }

}