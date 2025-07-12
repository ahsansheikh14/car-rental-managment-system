package com.example.carrental.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;
import com.example.carrental.SceneSwitcher;
import com.example.carrental.DBConnection;
import java.sql.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class loginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Check if tables exist before trying to query them
            DatabaseMetaData metaData = conn.getMetaData();
            boolean usersTableExists = false;
            boolean adminsTableExists = false;
            boolean customersTableExists = false;
            
            // Check for table existence
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME").toLowerCase();
                if (tableName.equals("users")) usersTableExists = true;
                if (tableName.equals("admins")) adminsTableExists = true;
                if (tableName.equals("customers")) customersTableExists = true;
            }
            tables.close();
            
            // Try login in customers table first (most reliable based on code examination)
            if (customersTableExists && tryCustomerLogin(conn, username, password, event)) {
                return; // Successful login
            }
            
            // Try admins table next
            if (adminsTableExists && tryAdminLogin(conn, username, password, event)) {
                return; // Successful login
            }
            
            // Try users table as last resort
            if (usersTableExists && tryUserLogin(conn, username, password, event)) {
                return; // Successful login
            }
            
            // If we get here, no valid login was found
            errorLabel.setText("Invalid username or password");
            
        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Login error: " + e.getMessage());
        }
    }
    
    private boolean tryCustomerLogin(Connection conn, String username, String password, ActionEvent event) {
        try {
            // First check for email column in customers table
            DatabaseMetaData metaData = conn.getMetaData();
            boolean hasEmail = false;
            boolean hasPasswordHash = false;
            boolean hasPassword = false;
            
            // Check column structure
            ResultSet columns = metaData.getColumns(null, null, "customers", "email");
            hasEmail = columns.next();
            columns.close();
            
            columns = metaData.getColumns(null, null, "customers", "password_hash");
            hasPasswordHash = columns.next();
            columns.close();
            
            columns = metaData.getColumns(null, null, "customers", "password");
            hasPassword = columns.next();
            columns.close();
            
            // Skip if necessary columns don't exist
            if (!hasEmail && !hasPasswordHash && !hasPassword) {
                return false;
            }
            
            // Determine which query to use based on available columns
            String query;
            PreparedStatement pstmt;
            
            if (hasEmail && hasPasswordHash) {
                query = "SELECT * FROM customers WHERE email = ? AND password_hash = ?";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, username);
                pstmt.setString(2, password);
            } else if (hasEmail && hasPassword) {
                query = "SELECT * FROM customers WHERE email = ? AND password = ?";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, username);
                pstmt.setString(2, password);
            } else {
                // Try a more flexible approach
                query = "SELECT * FROM customers WHERE (email = ? OR username = ?) AND (password = ? OR password_hash = ?)";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, username);
                pstmt.setString(2, username);
                pstmt.setString(3, password);
                pstmt.setString(4, password);
            }
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int customerId = rs.getInt("id");
                // Load user dashboard for customer login
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrental/userDashboard.fxml"));
                    Parent userRoot = loader.load();
                    
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    Scene scene = new Scene(userRoot);
                    stage.setScene(scene);
                    stage.setTitle("User Dashboard");
                    stage.show();
                    return true;
                } catch (Exception e) {
                    System.err.println("Error loading user dashboard: " + e.getMessage());
                    e.printStackTrace();
                    errorLabel.setText("Failed to load user dashboard");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error trying customer login: " + e.getMessage());
            // Continue to next auth method
        }
        return false;
    }
    
    private boolean tryAdminLogin(Connection conn, String username, String password, ActionEvent event) {
        try {
            // First check for required columns in admins table
            DatabaseMetaData metaData = conn.getMetaData();
            boolean hasUsername = false;
            boolean hasPasswordHash = false;
            boolean hasPassword = false;
            
            // Check column structure
            ResultSet columns = metaData.getColumns(null, null, "admins", "username");
            hasUsername = columns.next();
            columns.close();
            
            columns = metaData.getColumns(null, null, "admins", "password_hash");
            hasPasswordHash = columns.next();
            columns.close();
            
            columns = metaData.getColumns(null, null, "admins", "password");
            hasPassword = columns.next();
            columns.close();
            
            // Skip if necessary columns don't exist
            if (!hasUsername && !hasPasswordHash && !hasPassword) {
                return false;
            }
            
            // Determine which query to use based on available columns
            String query;
            PreparedStatement pstmt;
            
            if (hasUsername && hasPasswordHash) {
                query = "SELECT * FROM admins WHERE username = ? AND password_hash = ?";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, username);
                pstmt.setString(2, password);
            } else if (hasUsername && hasPassword) {
                query = "SELECT * FROM admins WHERE username = ? AND password = ?";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, username);
                pstmt.setString(2, password);
            } else {
                // Try a more flexible approach
                query = "SELECT * FROM admins WHERE username = ? AND (password = ? OR password_hash = ?)";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, password);
            }
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Load admin dashboard for admin login
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrental/adminDashboard.fxml"));
                    Parent adminRoot = loader.load();
                    
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    Scene scene = new Scene(adminRoot);
                    stage.setScene(scene);
                    stage.setTitle("Admin Dashboard");
                    stage.show();
                    return true;
                } catch (Exception e) {
                    System.err.println("Error loading admin dashboard: " + e.getMessage());
                    e.printStackTrace();
                    errorLabel.setText("Failed to load admin dashboard");
                    
                    // Try to initialize the database with missing columns
                    try {
                        initializeDatabase();
                    } catch (Exception dbEx) {
                        System.err.println("Could not initialize database: " + dbEx.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error trying admin login: " + e.getMessage());
            // Continue to next auth method
        }
        return false;
    }
    
    private boolean tryUserLogin(Connection conn, String username, String password, ActionEvent event) {
        try {
            // First check the structure of users table
            DatabaseMetaData metaData = conn.getMetaData();
            boolean hasUsername = false;
            boolean hasPasswordHash = false;
            boolean hasPassword = false;
            boolean hasRole = false;
            
            // Check if columns exist
            ResultSet columns = metaData.getColumns(null, null, "users", "username");
            hasUsername = columns.next();
            columns.close();
            
            columns = metaData.getColumns(null, null, "users", "password_hash");
            hasPasswordHash = columns.next();
            columns.close();
            
            columns = metaData.getColumns(null, null, "users", "password");
            hasPassword = columns.next();
            columns.close();
            
            columns = metaData.getColumns(null, null, "users", "role");
            hasRole = columns.next();
            columns.close();
            
            // Skip if we don't have the necessary columns
            if (!hasUsername || (!hasPassword && !hasPasswordHash)) {
                return false;
            }
            
            // Prepare the appropriate query based on actual column names
            PreparedStatement pstmt;
            if (hasPasswordHash) {
                pstmt = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password_hash = ?");
            } else if (hasPassword) {
                pstmt = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?");
            } else {
                // Try with multiple column names
                pstmt = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND (passwd = ? OR pass = ?)");
                pstmt.setString(2, password);
                pstmt.setString(3, password);
            }
            
            pstmt.setString(1, username);
            if (hasPasswordHash || hasPassword) {
                pstmt.setString(2, password);
            }
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Check role if it exists
                String role = hasRole ? rs.getString("role") : "user";
                int userId = rs.getInt("id");
                
                if ("admin".equals(role)) {
                    try {
                        // Load admin dashboard
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrental/adminDashboard.fxml"));
                        Parent adminRoot = loader.load();
                        
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        Scene scene = new Scene(adminRoot);
                        stage.setScene(scene);
                        stage.setTitle("Admin Dashboard");
                        stage.show();
                        return true;
                    } catch (Exception e) {
                        System.err.println("Error loading admin dashboard: " + e.getMessage());
                        e.printStackTrace();
                        errorLabel.setText("Failed to load admin dashboard");
                        
                        // Try to initialize the database with missing columns
                        try {
                            initializeDatabase();
                        } catch (Exception dbEx) {
                            System.err.println("Could not initialize database: " + dbEx.getMessage());
                        }
                    }
                } else {
                    // Regular user login
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrental/userDashboard.fxml"));
                        Parent userRoot = loader.load();
                        
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        Scene scene = new Scene(userRoot);
                        stage.setScene(scene);
                        stage.setTitle("User Dashboard");
                        stage.show();
                        return true;
                    } catch (Exception e) {
                        System.err.println("Error loading user dashboard: " + e.getMessage());
                        e.printStackTrace();
                        errorLabel.setText("Failed to load user dashboard");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error trying user login: " + e.getMessage());
            // Continue to next auth method
        }
        return false;
    }

    // Method to add missing columns to the database tables
    private void initializeDatabase() throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            // First check if the columns exist before trying to add them
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "payments", "payment_date");
            boolean paymentDateExists = columns.next();
            columns.close();
            
            columns = metaData.getColumns(null, null, "payments", "status");
            boolean statusExists = columns.next();
            columns.close();
            
            // Add payment_date column if it doesn't exist
            if (!paymentDateExists) {
                try {
                    PreparedStatement stmt = conn.prepareStatement(
                        "ALTER TABLE payments ADD COLUMN payment_date DATE DEFAULT CURRENT_DATE"
                    );
                    stmt.executeUpdate();
                    System.out.println("Successfully added payment_date column to payments table");
                } catch (SQLException e) {
                    System.err.println("Error adding payment_date column: " + e.getMessage());
                }
            }
            
            // Add status column if it doesn't exist
            if (!statusExists) {
                try {
                    PreparedStatement stmt = conn.prepareStatement(
                        "ALTER TABLE payments ADD COLUMN status VARCHAR(20) DEFAULT 'Pending'"
                    );
                    stmt.executeUpdate();
                    System.out.println("Successfully added status column to payments table");
                } catch (SQLException e) {
                    System.err.println("Error adding status column: " + e.getMessage());
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking/updating database structure: " + e.getMessage());
        }
    }
}