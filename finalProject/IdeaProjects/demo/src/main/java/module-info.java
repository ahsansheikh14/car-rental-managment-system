module com.example.carrental {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.carrental to javafx.fxml;
    opens com.example.carrental.controllers to javafx.fxml;
    exports com.example.carrental;
    exports com.example.carrental.controllers to javafx.fxml;

}