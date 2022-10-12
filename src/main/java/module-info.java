module com.opongapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens com.opongapp to javafx.fxml;
    exports com.opongapp;
}
