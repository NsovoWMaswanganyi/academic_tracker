package com.academic_tracker_app.controller;

import com.academic_tracker_app.database.ModuleDAO;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class MainControllerTest {
    @TempDir Path directory;
    @BeforeAll static void startJavaFx() throws Exception {
        CountDownLatch ready = new CountDownLatch(1);
        Platform.startup(() -> { Platform.setImplicitExit(false); ready.countDown(); });
        assertTrue(ready.await(10, TimeUnit.SECONDS));
    }
    @AfterAll static void stopJavaFx() { Platform.exit(); }

    @Test void formSelectionUpdateDeleteCancellationAndAverage() throws Exception {
        System.setProperty("academic.tracker.database", directory.resolve("ui-test.db").toString());
        FutureTask<Void> test = new FutureTask<>(() -> {
            Stage stage = new Stage();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main-view.fxml"));
                Parent root = loader.load();
                MainController controller = loader.getController();
                stage.setScene(new Scene(root));
                stage.show();
                assertTrue(controller.btnUpdateModule.isDisabled());
                assertTrue(controller.btnDeleteModule.isDisabled());
                fill(controller, "Statistics", "12", "80");
                controller.btnAddModule.fire();
                fill(controller, "Physics", "24", "50");
                controller.btnAddModule.fire();
                assertEquals(String.format("%.2f%%", 60.0), controller.lblAverage.getText());
                controller.moduleTable.getSelectionModel().selectFirst();
                assertEquals("Statistics", controller.txtModuleName.getText());
                assertTrue(controller.btnAddModule.isDisabled());
                fill(controller, "Statistics II", "24", "100");
                controller.btnUpdateModule.fire();
                assertEquals(String.format("%.2f%%", 75.0), controller.lblAverage.getText());
                assertEquals("Statistics II", new ModuleDAO().getModules().getFirst().getModuleName());
                assertTrue(controller.txtModuleName.getText().isEmpty());
                controller.moduleTable.getSelectionModel().selectFirst();
                respondToDialog(ButtonType.CANCEL);
                controller.btnDeleteModule.fire();
                assertEquals(2, controller.moduleTable.getItems().size());
                respondToDialog(ButtonType.OK);
                controller.btnDeleteModule.fire();
                assertEquals(1, controller.moduleTable.getItems().size());
                assertEquals(String.format("%.2f%%", 50.0), controller.lblAverage.getText());
                controller.moduleTable.getSelectionModel().selectFirst();
                respondToDialog(ButtonType.OK);
                controller.btnDeleteModule.fire();
                assertEquals(String.format("%.2f%%", 0.0), controller.lblAverage.getText());
                assertTrue(new ModuleDAO().getModules().isEmpty());
                assertTrue(controller.btnDeleteModule.isDisabled());
            } finally {
                stage.close();
            }
            return null;
        });
        try {
            Platform.runLater(test);
            test.get(30, TimeUnit.SECONDS);
        } finally {
            System.clearProperty("academic.tracker.database");
        }
    }

    private static void fill(MainController controller, String name, String credits, String mark) {
        controller.txtModuleName.setText(name);
        controller.txtCredits.setText(credits);
        controller.txtMark.setText(mark);
    }

    private static void respondToDialog(ButtonType response) {
        Platform.runLater(() -> {
            for (Window window : new ArrayList<>(Window.getWindows())) {
                if (window.getScene() != null && window.getScene().getRoot() instanceof DialogPane pane) {
                    ((Button) pane.lookupButton(response)).fire();
                    return;
                }
            }
            throw new AssertionError("Delete confirmation did not open.");
        });
    }
}
