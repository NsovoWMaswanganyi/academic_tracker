package com.academic_tracker_app.controller;

import com.academic_tracker_app.database.Database;
import com.academic_tracker_app.database.ModuleDAO;
import com.academic_tracker_app.model.Module;
import com.academic_tracker_app.model.GradeAverages;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.SQLException;

public class MainController {
    @FXML public TextField txtModuleName;
    @FXML public TextField txtCredits;
    @FXML public TextField txtMark;
    @FXML public CheckBox chkFinalYear;
    @FXML public Button btnAddModule;
    @FXML public Button btnUpdateModule;
    @FXML public Button btnDeleteModule;
    @FXML public TableView<Module> moduleTable;
    @FXML public TableColumn<Module, String> moduleColumn;
    @FXML public TableColumn<Module, Integer> creditsColumn;
    @FXML public TableColumn<Module, Double> markColumn;
    @FXML public TableColumn<Module, String> finalYearColumn;
    @FXML public Label lblAverage;
    @FXML public Label lblFinalYearAverage;
    @FXML public Label lblFinalYearDetails;
    @FXML public Label lblStatus;
    @FXML public Label lblDataLocation;

    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final ObservableList<Module> modules = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        moduleColumn.setCellValueFactory(new PropertyValueFactory<>("moduleName"));
        creditsColumn.setCellValueFactory(new PropertyValueFactory<>("credits"));
        markColumn.setCellValueFactory(new PropertyValueFactory<>("mark"));
        finalYearColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().isFinalYear() ? "Yes" : ""));
        moduleTable.setItems(modules);
        moduleTable.setPlaceholder(new Label("Add a module to start tracking your grades."));
        btnUpdateModule.disableProperty().bind(moduleTable.getSelectionModel().selectedItemProperty().isNull());
        btnDeleteModule.disableProperty().bind(moduleTable.getSelectionModel().selectedItemProperty().isNull());
        btnAddModule.disableProperty().bind(moduleTable.getSelectionModel().selectedItemProperty().isNotNull());
        moduleTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selected) -> {
            if (selected != null) {
                txtModuleName.setText(selected.getModuleName());
                txtCredits.setText(Integer.toString(selected.getCredits()));
                txtMark.setText(Double.toString(selected.getMark()));
                chkFinalYear.setSelected(selected.isFinalYear());
                lblStatus.setText("Editing selected module. Save changes or choose New / Clear.");
            }
        });
        lblDataLocation.setText("Grades: " + Database.getDatabasePath());
        lblDataLocation.setTooltip(new Tooltip(Database.getDatabasePath().toString()));
        try {
            refresh();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load grades from " + Database.getDatabasePath(), e);
        }
    }

    @FXML private void addModule() { saveModule(false); }
    @FXML private void updateModule() { saveModule(true); }

    private void saveModule(boolean updating) {
        Module selected = moduleTable.getSelectionModel().getSelectedItem();
        if (updating && selected == null) return;
        try {
            Module module = readInput(txtModuleName.getText(), txtCredits.getText(), txtMark.getText());
            module.setFinalYear(chkFinalYear.isSelected());
            if (updating) {
                module.setId(selected.getId());
                moduleDAO.updateModule(module);
            } else {
                moduleDAO.addModule(module);
            }
            refresh();
            clearForm();
            lblStatus.setText(updating ? "Module updated." : "Module added.");
        } catch (IllegalArgumentException e) {
            showError("Invalid input", e.getMessage());
        } catch (SQLException e) {
            showError("Could not save grades", e.getMessage());
        }
    }

    static Module readInput(String name, String creditsText, String markText) {
        name = name.trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Enter a module name.");
        int credits;
        double mark;
        try {
            credits = Integer.parseInt(creditsText.trim());
            mark = Double.parseDouble(markText.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Credits must be a whole number and mark must be numeric.");
        }
        if (credits <= 0) throw new IllegalArgumentException("Credits must be greater than zero.");
        if (!Double.isFinite(mark) || mark < 0 || mark > 100)
            throw new IllegalArgumentException("Mark must be between 0 and 100.");
        return new Module(name, credits, mark);
    }

    @FXML
    private void deleteModule() {
        Module selected = moduleTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + selected.getModuleName() + "\"? This cannot be undone.", ButtonType.CANCEL, ButtonType.OK);
        confirmation.setTitle("Delete module");
        confirmation.setHeaderText(null);
        confirmation.initOwner(moduleTable.getScene().getWindow());
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            moduleDAO.deleteModule(selected.getId());
            refresh();
            clearForm();
            lblStatus.setText("Module deleted.");
        } catch (SQLException e) {
            showError("Could not delete module", e.getMessage());
        }
    }

    @FXML
    private void clearForm() {
        moduleTable.getSelectionModel().clearSelection();
        txtModuleName.clear();
        txtCredits.clear();
        txtMark.clear();
        chkFinalYear.setSelected(false);
        lblStatus.setText("Add a module, or select a row to update or delete it.");
        txtModuleName.requestFocus();
    }

    private void refresh() throws SQLException {
        modules.setAll(moduleDAO.getModules());
        var overall = GradeAverages.summarize(modules, false);
        var finalYear = GradeAverages.summarize(modules, true);
        lblAverage.setText(String.format("%.2f%%", overall.average()));
        lblFinalYearAverage.setText(finalYear.credits() > 0 ? String.format("%.2f%%", finalYear.average()) : "—");
        lblFinalYearDetails.setText(finalYear.moduleCount() == 0
                ? "Select a module and tick Final-year module to include it."
                : String.format("%d final-year %s · %d credits", finalYear.moduleCount(),
                        finalYear.moduleCount() == 1 ? "module" : "modules", finalYear.credits()));
        lblFinalYearAverage.setTooltip(new Tooltip("Sum of (mark × credits) ÷ total final-year credits"));
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.initOwner(moduleTable.getScene().getWindow());
        alert.showAndWait();
    }
}
