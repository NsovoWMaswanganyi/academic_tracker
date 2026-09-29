package com.academic_tracker_app.database;

import com.academic_tracker_app.model.Module;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

public class ModuleDAO {
    public void addModule(Module module) throws SQLException {
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO modules(moduleName, credits, mark, finalYear) VALUES(?,?,?,?)")) {
            setValues(statement, module);
            statement.executeUpdate();
        }
    }

    public void updateModule(Module module) throws SQLException {
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE modules SET moduleName=?, credits=?, mark=?, finalYear=? WHERE id=?")) {
            setValues(statement, module);
            statement.setInt(5, module.getId());
            requireExisting(statement.executeUpdate());
        }
    }

    public void deleteModule(int id) throws SQLException {
        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM modules WHERE id=?")) {
            statement.setInt(1, id);
            requireExisting(statement.executeUpdate());
        }
    }

    private void setValues(PreparedStatement statement, Module module) throws SQLException {
        statement.setString(1, module.getModuleName());
        statement.setInt(2, module.getCredits());
        statement.setDouble(3, module.getMark());
        statement.setBoolean(4, module.isFinalYear());
    }

    private void requireExisting(int count) throws SQLException {
        if (count != 1) throw new SQLException("This module no longer exists. Reopen the app to refresh the list.");
    }

    public ObservableList<Module> getModules() throws SQLException {
        ObservableList<Module> modules = FXCollections.observableArrayList();
        try (Connection connection = Database.connect();
             Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT * FROM modules ORDER BY id")) {
            while (rows.next()) {
                Module module = new Module(rows.getString("moduleName"), rows.getInt("credits"), rows.getDouble("mark"));
                module.setId(rows.getInt("id"));
                module.setFinalYear(rows.getBoolean("finalYear"));
                modules.add(module);
            }
        }
        return modules;
    }
}
