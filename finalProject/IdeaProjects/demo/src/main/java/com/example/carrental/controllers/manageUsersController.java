package com.example.carrental.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Label;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import com.example.carrental.DBConnection;
import com.example.carrental.models.customer;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class manageUsersController {
    @FXML private TableView<customer> usersTable;
    @FXML private TableColumn<customer, Integer> idColumn;
    @FXML private TableColumn<customer, String> nameColumn;
    @FXML private TableColumn<customer, String> contactColumn;
    @FXML private TableColumn<customer, String> licenseColumn;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        // Initialize table columns
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getCustomerId()).asObject());
        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        contactColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getContact()));
        licenseColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLicenseNo()));

        // Load users on initialization
        loadUsers();
    }

    @FXML
    private void handleAddUser(ActionEvent event) {
        // Create a series of dialogs to get user information
        String name = showInputDialog("User Name", "Enter user's full name:");
        if (name == null) return;
        
        String email = showInputDialog("User Email", "Enter user's email:");
        if (email == null) return;
        
        String phone = showInputDialog("User Phone", "Enter user's phone number:");
        if (phone == null) return;
        
        String licenseNo = showInputDialog("User License", "Enter user's license number:");
        if (licenseNo == null) return;
        
        String password = showInputDialog("User Password", "Enter user's password:");
        if (password == null) return;
        
        // Insert the new user into the database
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT INTO customers (name, email, phone, license_number, password_hash) VALUES (?, ?, ?, ?, ?)")) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.setString(4, licenseNo);
            pstmt.setString(5, password);
            
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                statusLabel.setText("User added successfully");
                loadUsers(); // Refresh the table
            } else {
                statusLabel.setText("Failed to add user");
            }
        } catch (SQLException e) {
            statusLabel.setText("Error adding user: " + e.getMessage());
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "Error adding user", e.getMessage());
        }
    }

    private String showInputDialog(String title, String content) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(content);
        
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }
    
    private void showAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleDeleteUser(ActionEvent event) {
        customer selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            statusLabel.setText("Please select a user to delete");
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM customers WHERE id = ?")) {
            pstmt.setInt(1, selectedUser.getCustomerId());
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                statusLabel.setText("User deleted successfully");
                loadUsers(); // Refresh the table
            } else {
                statusLabel.setText("User not found or could not be deleted");
            }
        } catch (SQLException e) {
            statusLabel.setText("Error deleting user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadUsers();
    }

    private void loadUsers() {
        ObservableList<customer> userList = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM customers");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                customer user = new customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("license_number"),
                        rs.getString("password_hash")
                );
                userList.add(user);
                System.out.println("Loaded user: " + user.getName() + ", ID: " + user.getCustomerId());
            }
            usersTable.setItems(userList);
            usersTable.refresh(); // Ensure the TableView refreshes
            statusLabel.setText("Loaded " + userList.size() + " users successfully");
        } catch (SQLException e) {
            statusLabel.setText("Error loading users: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 