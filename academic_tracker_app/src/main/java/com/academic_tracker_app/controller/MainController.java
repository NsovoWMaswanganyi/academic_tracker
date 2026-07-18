package com.academic_tracker_app.controller;

import com.academic_tracker_app.database.Database;
import com.academic_tracker_app.database.ModuleDAO;
import com.academic_tracker_app.model.Module;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.academic_tracker_app.model.*;

public class MainController {
    @FXML
    public TextField txtModuleName;
    public TextField txtCredits;
    public TextField txtMark;
    public Button btnAddModule;

    public TableView<Module> moduleTable;
    public TableColumn<Module, String> moduleColumn;
    public TableColumn<Module, Integer> creditsColumn;
    public TableColumn<Module, Double> markColumn;

    public Label lblAverage;

    private final ModuleDAO moduleDAO = new ModuleDAO();
    private ObservableList<Module> modules;

    @FXML
    public void initialize() {

        moduleColumn.setCellValueFactory(new PropertyValueFactory<>("moduleName"));
        creditsColumn.setCellValueFactory(new PropertyValueFactory<>("credits"));
        markColumn.setCellValueFactory(new PropertyValueFactory<>("mark"));

        modules = moduleDAO.getModules();
        moduleTable.setItems(modules);
        calculateAverage();

    }

    @FXML
    private void addModule() {

        try {

            String moduleName = txtModuleName.getText();

            int credits = Integer.parseInt(txtCredits.getText());

            double mark = Double.parseDouble(txtMark.getText());

            Module module = new Module(moduleName, credits, mark);

            moduleDAO.addModule(module);
            modules.setAll(moduleDAO.getModules());

            calculateAverage();

            txtModuleName.clear();
            txtCredits.clear();
            txtMark.clear();

            txtModuleName.requestFocus();

        }
        catch (NumberFormatException e) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Input");
            alert.setHeaderText(null);
            alert.setContentText("Credits and Mark must be numeric.");
            alert.showAndWait();

        }

    }

    private void calculateAverage() {

        double weightedTotal = 0;
        int totalCredits = 0;

        for (Module module : modules) {

            weightedTotal += module.getMark() * module.getCredits();

            totalCredits += module.getCredits();

        }

        if (totalCredits > 0) {

            double average = weightedTotal / totalCredits;

            lblAverage.setText(String.format("%.2f%%", average));

        }
        else {

            lblAverage.setText("0.00%");

        }

    }

}
