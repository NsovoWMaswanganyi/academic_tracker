package com.academic_tracker_app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.IOException;

public class AcademicTrackerApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AcademicTrackerApplication.class.getResource("/view/main-view.fxml"));
        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load(), 700, 760);
        } catch (IOException | RuntimeException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Academic Tracker could not start");
            alert.setHeaderText("Your grades could not be loaded.");
            Throwable cause = e;
            while (cause.getCause() != null) cause = cause.getCause();
            alert.setContentText(cause.getMessage() + "\nData: "
                    + com.academic_tracker_app.database.Database.getDatabasePath());
            alert.showAndWait();
            Platform.exit();
            return;
        }
        stage.setTitle("Academic Tracker");
        stage.setMinWidth(700);
        stage.setMinHeight(720);
        var icon = AcademicTrackerApplication.class.getResourceAsStream("/icons/academic-tracker.png");
        if (icon != null) stage.getIcons().add(new Image(icon));
        stage.setScene(scene);
        stage.show();
    }
}
