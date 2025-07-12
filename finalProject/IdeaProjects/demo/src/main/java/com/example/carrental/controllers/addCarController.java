package com.example.carrental.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CheckBox;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.example.carrental.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class addCarController {
    @FXML private TextField makeField;
    @FXML private TextField modelField;
    @FXML private TextField yearField;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> seatsComboBox;
    @FXML private CheckBox specialCarCheckBox;
    @FXML private Button submitButton;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        // Initialize the seats combo box
        ObservableList<String> seatOptions = FXCollections.observableArrayList("2", "4", "5", "7", "8");
        seatsComboBox.setItems(seatOptions);
        seatsComboBox.setValue("4"); // Default value
    }

    @FXML
    private void handleAddCar(ActionEvent event) {
        try {
            // Validate input
            String brand = makeField.getText();
            String model = modelField.getText();
            if (brand.isEmpty() || model.isEmpty()) {
                statusLabel.setText("Make and Model cannot be empty");
                return;
            }

            int year = Integer.parseInt(yearField.getText());
            double pricePerDay = Double.parseDouble(priceField.getText());
            
            // Get seats value
            String seatsStr = seatsComboBox.getValue();
            if (seatsStr == null) {
                statusLabel.setText("Please select number of seats");
                return;
            }
            int seats = Integer.parseInt(seatsStr);
            
            // Get special car status
            boolean isSpecial = specialCarCheckBox.isSelected();
            
            // Default status is "Available"
            String status = "Available";

            // Get database connection and prepare statement
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                         "INSERT INTO cars (brand, model, year, price_per_day, seats, is_special, status) VALUES (?, ?, ?, ?, ?, ?, ?)"
                 )) {
                pstmt.setString(1, brand);
                pstmt.setString(2, model);
                pstmt.setInt(3, year);
                pstmt.setDouble(4, pricePerDay);
                pstmt.setInt(5, seats);
                pstmt.setBoolean(6, isSpecial);
                pstmt.setString(7, status);

                // Execute the statement
                pstmt.executeUpdate();

                statusLabel.setText("Car added successfully!");

                // Clear the fields
                makeField.clear();
                modelField.clear();
                yearField.clear();
                priceField.clear();
                seatsComboBox.setValue("4");
                specialCarCheckBox.setSelected(false);
            }
        } catch (SQLException e) {
            statusLabel.setText("Error adding car: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            statusLabel.setText("Please enter valid numbers for year and price");
        }
    }
}