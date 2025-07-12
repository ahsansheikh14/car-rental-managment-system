package com.example.carrental.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;
import com.example.carrental.SceneSwitcher;
import com.example.carrental.models.car;
import com.example.carrental.dsa.carStack;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class adminController {
    @FXML private TableView<car> carTable;
    @FXML private TableColumn<car, Integer> idColumn;
    @FXML private TableColumn<car, String> modelColumn;
    @FXML private TableColumn<car, String> brandColumn;
    @FXML private TableColumn<car, Integer> yearColumn;
    @FXML private TableColumn<car, Double> priceColumn;
    @FXML private Button addCarButton;
    @FXML private Button removeCarButton;
    @FXML private Button backButton;

    private carStack carStackDSA = new carStack(); // DSA for in-memory car stack (undo/redo or recent cars)

    @FXML
    private void handleAddCar(ActionEvent event) {
        // Example: Add a car to stack and DB
        car newCar = new car(0, "ModelZ", "BrandA", 2024, 120.0, "Available");
        carStackDSA.push(newCar); // Add to DSA
        // Add to DB (you can use your addCarController logic here)
        try (Connection conn = com.example.carrental.DBConnection.getConnection()) {
            String sql = "INSERT INTO cars (model, brand, year, price_per_day, status) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newCar.getModel());
            pstmt.setString(2, newCar.getBrand());
            pstmt.setInt(3, newCar.getYear());
            pstmt.setDouble(4, newCar.getPricePerDay());
            pstmt.setString(5, newCar.getStatus());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRemoveCar(ActionEvent event) {
        // Example: Remove the most recently added car from stack and DB
        car removedCar = carStackDSA.pop();
        if (removedCar != null) {
            try (Connection conn = com.example.carrental.DBConnection.getConnection()) {
                String sql = "DELETE FROM cars WHERE model=? AND brand=? AND year=? AND price_per_day=? AND status=? LIMIT 1";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, removedCar.getModel());
                pstmt.setString(2, removedCar.getBrand());
                pstmt.setInt(3, removedCar.getYear());
                pstmt.setDouble(4, removedCar.getPricePerDay());
                pstmt.setString(5, removedCar.getStatus());
                pstmt.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            SceneSwitcher.switchScene(stage, "/com/example/carrental/dashboard.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}