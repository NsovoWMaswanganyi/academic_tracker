package com.academic_tracker_app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class MainController {
    @FXML
    public TextField txtModuleName;
    public TextField txtcredits;
    public TextField txtmark;
    public Button btnAddModule;

    private ObservableList<Module> modules;
}
