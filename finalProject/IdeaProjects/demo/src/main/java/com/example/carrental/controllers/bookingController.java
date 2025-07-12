package com.example.carrental.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;
import com.example.carrental.SceneSwitcher;
import com.example.carrental.DBConnection;
import com.example.carrental.models.car;
import com.example.carrental.dsa.bookingQueue;
import com.example.carrental.models.booking;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class bookingController {
    @FXML private TableView<car> carTable;
    @FXML private TableColumn<car, Integer> carIdColumn;
    @FXML private TableColumn<car, String> modelColumn;
    @FXML private TableColumn<car, String> brandColumn;
    @FXML private TableColumn<car, Integer> yearColumn;
    @FXML private TableColumn<car, Double> priceColumn;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label totalPriceLabel;
    @FXML private Label statusLabel;
    @FXML private Button bookButton;
    @FXML private TableView<BookingSummary> bookingTable;
    @FXML private Button backButton;

    private car selectedCar;
    private int currentUserId = 1; // This should be set based on logged-in user
    private bookingQueue bookingQueueDSA = new bookingQueue(); // DSA for in-memory booking management

    @FXML
    public void initialize() {
        // Set up car table columns
        carIdColumn = new TableColumn<>("ID");
        carIdColumn.setCellValueFactory(new PropertyValueFactory<>("carId"));
        
        modelColumn = new TableColumn<>("Model");
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        
        brandColumn = new TableColumn<>("Brand");
        brandColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));
        
        yearColumn = new TableColumn<>("Year");
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        
        priceColumn = new TableColumn<>("Price/Day");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerDay"));
        
        carTable.getColumns().setAll(carIdColumn, modelColumn, brandColumn, yearColumn, priceColumn);
        
        // Set default dates
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now().plusDays(1));
        
        // Add listener for date changes to update price
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateTotalPrice());
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateTotalPrice());
        
        // Add listener for car selection
        carTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedCar = newSelection;
                updateTotalPrice();
            }
        });
        
        // Load available cars
        loadAvailableCars();
        
        // Setup booking table
        TableColumn<BookingSummary, Integer> bookingIdCol = new TableColumn<>("Booking ID");
        bookingIdCol.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        
        TableColumn<BookingSummary, Integer> carIdCol = new TableColumn<>("Car ID");
        carIdCol.setCellValueFactory(new PropertyValueFactory<>("carId"));
        
        TableColumn<BookingSummary, LocalDate> startCol = new TableColumn<>("Start");
        startCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        
        TableColumn<BookingSummary, LocalDate> endCol = new TableColumn<>("End");
        endCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        
        TableColumn<BookingSummary, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        if (bookingTable != null) {
            bookingTable.getColumns().setAll(bookingIdCol, carIdCol, startCol, endCol, statusCol);
            loadRecentBookings();
        }
    }

    private void loadAvailableCars() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM cars WHERE status = 'Available'";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            List<car> cars = new ArrayList<>();
            while (rs.next()) {
                car car = new car(
                    rs.getInt("id"),
                    rs.getString("model"),
                    rs.getString("brand"),
                    rs.getInt("year"),
                    rs.getDouble("price_per_day"),
                    rs.getString("status")
                );
                cars.add(car);
            }

            ObservableList<car> carData = FXCollections.observableArrayList(cars);
            carTable.setItems(carData);
            statusLabel.setText("Available cars loaded successfully");

        } catch (SQLException e) {
            statusLabel.setText("Error loading cars: " + e.getMessage());
        }
    }
    
    private void loadRecentBookings() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM bookings WHERE customer_id = ? ORDER BY id DESC LIMIT 5";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();

            List<BookingSummary> bookings = new ArrayList<>();
            while (rs.next()) {
                BookingSummary summary = new BookingSummary(
                    rs.getInt("id"),
                    rs.getInt("car_id"),
                    rs.getDate("start_date").toLocalDate(),
                    rs.getDate("end_date").toLocalDate(),
                    rs.getString("status")
                );
                bookings.add(summary);
            }

            ObservableList<BookingSummary> bookingData = FXCollections.observableArrayList(bookings);
            bookingTable.setItems(bookingData);

        } catch (SQLException e) {
            statusLabel.setText("Error loading recent bookings: " + e.getMessage());
        }
    }
    
    private void updateTotalPrice() {
        if (selectedCar != null && startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            
            if (!endDate.isAfter(startDate)) {
                totalPriceLabel.setText("End date must be after start date");
                bookButton.setDisable(true);
                return;
            }
            
            // Calculate number of days
            long days = ChronoUnit.DAYS.between(startDate, endDate);
            double totalPrice = days * selectedCar.getPricePerDay();
            
            totalPriceLabel.setText(String.format("Total Price: $%.2f (%d days)", totalPrice, days));
            bookButton.setDisable(false);
        } else {
            totalPriceLabel.setText("Select a car and dates");
            bookButton.setDisable(true);
        }
    }
    
    @FXML
    private void handleBook(ActionEvent event) {
        if (selectedCar == null) {
            statusLabel.setText("Please select a car first");
            return;
        }
        
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (startDate == null || endDate == null) {
            statusLabel.setText("Please select start and end dates");
            return;
        }
        
        if (!endDate.isAfter(startDate)) {
            statusLabel.setText("End date must be after start date");
            return;
        }
        
        // Calculate number of days and total price
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        double totalPrice = days * selectedCar.getPricePerDay();
        
        try (Connection conn = DBConnection.getConnection()) {
            // Begin transaction
            conn.setAutoCommit(false);
            
            try {
                // Create booking
                String insertBookingSql = "INSERT INTO bookings (car_id, customer_id, start_date, end_date, status, total_price) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(insertBookingSql, Statement.RETURN_GENERATED_KEYS);
                pstmt.setInt(1, selectedCar.getCarId());
                pstmt.setInt(2, currentUserId);
                pstmt.setDate(3, Date.valueOf(startDate));
                pstmt.setDate(4, Date.valueOf(endDate));
                pstmt.setString(5, "Confirmed");
                pstmt.setDouble(6, totalPrice);
                pstmt.executeUpdate();
                
                // Get generated booking ID
                ResultSet rs = pstmt.getGeneratedKeys();
                int bookingId = 0;
                if (rs.next()) {
                    bookingId = rs.getInt(1);
                }
                
                // Update car status
                String updateCarSql = "UPDATE cars SET status = 'Rented' WHERE id = ?";
                pstmt = conn.prepareStatement(updateCarSql);
                pstmt.setInt(1, selectedCar.getCarId());
                pstmt.executeUpdate();
                
                // Commit transaction
                conn.commit();
                
                // Add to booking queue DSA
                booking newBooking = new booking(
                    bookingId, 
                    selectedCar.getCarId(),
                    currentUserId,
                    startDate,
                    endDate,
                    "Confirmed",
                    totalPrice
                );
                bookingQueueDSA.enqueue(newBooking);
                
                statusLabel.setText("Booking confirmed successfully! Booking ID: " + bookingId);
                
                // Refresh tables
                loadAvailableCars();
                loadRecentBookings();
                
                // Reset selection
                selectedCar = null;
                startDatePicker.setValue(LocalDate.now());
                endDatePicker.setValue(LocalDate.now().plusDays(1));
                
            } catch (SQLException e) {
                // Rollback in case of errors
                conn.rollback();
                throw e;
            } finally {
                // Reset auto-commit
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            statusLabel.setText("Error creating booking: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            SceneSwitcher.switchScene(stage, "/com/example/carrental/userDashboard.fxml");
        } catch (Exception e) {
            statusLabel.setText("Error returning to dashboard: " + e.getMessage());
        }
    }
    
    // Inner class for booking summary display
    public static class BookingSummary {
        private int bookingId;
        private int carId;
        private LocalDate startDate;
        private LocalDate endDate;
        private String status;
        
        public BookingSummary(int bookingId, int carId, LocalDate startDate, LocalDate endDate, String status) {
            this.bookingId = bookingId;
            this.carId = carId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.status = status;
        }
        
        // Getters
        public int getBookingId() { return bookingId; }
        public int getCarId() { return carId; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public String getStatus() { return status; }
    }
}