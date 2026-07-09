module com.academic_tracker_app.academic_tracker_app {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens com.academic_tracker_app to javafx.fxml;
    exports com.academic_tracker_app;
}