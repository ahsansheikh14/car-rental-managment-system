package com.example.carrental.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;
import com.example.carrental.SceneSwitcher;

public class dashboardController {
    @FXML private Button adminButton;
    @FXML private Button bookingButton;
    @FXML private Button logoutButton;

    @FXML
    private void goToAdmin(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            SceneSwitcher.switchScene(stage, "/com/example/carrental/admin.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToBooking(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            SceneSwitcher.switchScene(stage, "/com/example/carrental/booking.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            SceneSwitcher.switchScene(stage, "/com/example/carrental/login.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}