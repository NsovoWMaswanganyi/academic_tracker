package com.academic_tracker_app.database;

import com.academic_tracker_app.model.Module;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class ModuleDAO {

    public void addModule(Module module) {

        String sql = """
                INSERT INTO modules(moduleName, credits, mark)
                VALUES(?,?,?)
                """;

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, module.getModuleName());
            pstmt.setInt(2, module.getCredits());
            pstmt.setDouble(3, module.getMark());

            pstmt.executeUpdate();

        }

        catch (SQLException e) {

            e.printStackTrace();

        }

    }

    public ObservableList<Module> getModules() {

        ObservableList<Module> modules =
                FXCollections.observableArrayList();

        String sql = "SELECT * FROM modules";

        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                Module module = new Module(

                        rs.getString("moduleName"),

                        rs.getInt("credits"),

                        rs.getDouble("mark")

                );

                module.setId(rs.getInt("id"));

                modules.add(module);

            }

        }

        catch (SQLException e) {

            e.printStackTrace();

        }

        return modules;

    }

}