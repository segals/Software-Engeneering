module gcm {
    requires javafx.controls;
    requires javafx.fxml;

    opens gcm to javafx.fxml;
    exports gcm;
}
