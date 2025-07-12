package com.example.carrental.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import com.example.carrental.DBConnection;
import com.example.carrental.models.car;
import com.example.carrental.models.customer;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.util.Callback;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javafx.scene.layout.HBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.TabPane;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.application.Platform;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.time.DayOfWeek;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.time.YearMonth;
import java.sql.DatabaseMetaData;

public class adminDashboardController {
    @FXML private TableView<car> carsTable;
    @FXML private TableColumn<car, String> makeColumn;
    @FXML private TableColumn<car, String> modelColumn;
    @FXML private TableColumn<car, Integer> yearColumn;
    @FXML private TableColumn<car, Double> priceColumn;
    @FXML private TableColumn<car, Integer> seatsColumn;
    @FXML private TableColumn<car, Boolean> specialColumn;
    @FXML private TableColumn<car, String> statusColumn;
    @FXML private Label statusLabel;
    
    // Add button to filter cars by seat capacity
    @FXML private Button allCarsButton;
    @FXML private Button fourSeaterButton;
    @FXML private Button fivePlusSeaterButton;
    
    // New fields for payment tracking
    @FXML private TableView<BookingPaymentData> bookingsTable;
    @FXML private TableColumn<BookingPaymentData, Integer> bookingIdColumn;
    @FXML private TableColumn<BookingPaymentData, String> customerNameColumn;
    @FXML private TableColumn<BookingPaymentData, String> carInfoColumn;
    @FXML private TableColumn<BookingPaymentData, LocalDate> startDateColumn;
    @FXML private TableColumn<BookingPaymentData, LocalDate> endDateColumn;
    @FXML private TableColumn<BookingPaymentData, String> bookingStatusColumn;
    @FXML private TableColumn<BookingPaymentData, Double> originalAmountColumn;
    @FXML private TableColumn<BookingPaymentData, Double> lateFeeColumn;
    @FXML private TableColumn<BookingPaymentData, Double> totalAmountColumn;
    @FXML private TableColumn<BookingPaymentData, String> paymentStatusColumn;
    @FXML private TableColumn<BookingPaymentData, String> actionColumn;
    
    // Analytics controls
    @FXML private BarChart<String, Number> revenueChart;
    @FXML private CategoryAxis periodAxis;
    @FXML private NumberAxis amountAxis;
    @FXML private Label totalRevenueLabel;
    @FXML private Label completedBookingsLabel;
    @FXML private Label pendingPaymentsLabel;
    @FXML private Label lateFeesLabel;
    
    @FXML private TableView<PaymentAnalyticsData> analyticsTable;
    @FXML private TableColumn<PaymentAnalyticsData, String> analyticsPeriodColumn;
    @FXML private TableColumn<PaymentAnalyticsData, Double> totalRevenueColumn;
    @FXML private TableColumn<PaymentAnalyticsData, Integer> bookingsCountColumn;
    @FXML private TableColumn<PaymentAnalyticsData, Double> avgBookingValueColumn;
    @FXML private TableColumn<PaymentAnalyticsData, Double> lateFeesCollectedColumn;
    
    private ContextMenu contextMenu;

    @FXML private TabPane mainTabPane;

    @FXML
    private void initialize() {
        try {
            // Initialize car table columns if they exist
            if (makeColumn != null && modelColumn != null) {
                makeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBrand()));
                modelColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getModel()));
                yearColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getYear()).asObject());
                priceColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPricePerDay()).asObject());
                seatsColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getSeats()).asObject());
                specialColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().isSpecial()));
                statusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
                
                // Setup context menu for updating car status
                setupContextMenu();
                
                // Load cars on initialization
                loadCars();
            } else {
                System.out.println("Car table columns not properly initialized");
            }
            
            // Initialize seat filter buttons if they exist
            Platform.runLater(() -> {
                try {
                    // Configure filter buttons if they exist
                    if (allCarsButton != null && fourSeaterButton != null && fivePlusSeaterButton != null) {
                        allCarsButton.setOnAction(e -> loadCars());
                        fourSeaterButton.setOnAction(e -> loadCarsBySeats(4, 4));
                        fivePlusSeaterButton.setOnAction(e -> loadCarsBySeats(5, 10));
                    }
                } catch (Exception e) {
                    System.out.println("Error initializing seat filter buttons: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
            // Initialize tabs when the scene is fully loaded
            Platform.runLater(() -> {
                try {
                    if (mainTabPane != null) {
                        // Initialize Payment Tracking tab first (needed by others)
                        try {
                            initializePaymentTrackingTab();
                        } catch (Exception e) {
                            System.out.println("Error initializing payment tracking tab: " + e.getMessage());
                            e.printStackTrace();
                        }
                        
                        // Then try to initialize analytics tab
                        try {
                            initializeAnalyticsTab();
                        } catch (Exception e) {
                            System.out.println("Error initializing analytics tab: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error initializing tabs: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.out.println("Error in dashboard initialization: " + e.getMessage());
            e.printStackTrace();
            
            // Set a message to the status label if it exists
            if (statusLabel != null) {
                statusLabel.setText("Dashboard initialization error. See console for details.");
            }
        }
    }
    
    private void initializePaymentTrackingTab() {
        try {
            // Initialize payment tracking table if it exists
            if (bookingsTable != null) {
                setupBookingPaymentTable();
                loadBookingsWithPaymentInfo();
            }
        } catch (Exception e) {
            System.out.println("Error initializing payment tracking: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializeAnalyticsTab() {
        try {
            if (analyticsTable != null && revenueChart != null) {
                // Configure analytics table
                analyticsPeriodColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().period));
                totalRevenueColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().totalRevenue).asObject());
                bookingsCountColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().bookingsCount).asObject());
                avgBookingValueColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().avgBookingValue).asObject());
                lateFeesCollectedColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().lateFees).asObject());
                
                // Format currency in table cells
                totalRevenueColumn.setCellFactory(col -> new TableCell<PaymentAnalyticsData, Double>() {
                    @Override
                    protected void updateItem(Double amount, boolean empty) {
                        super.updateItem(amount, empty);
                        if (empty || amount == null) {
                            setText(null);
                        } else {
                            setText(String.format("$%.2f", amount));
                        }
                    }
                });
                
                avgBookingValueColumn.setCellFactory(col -> new TableCell<PaymentAnalyticsData, Double>() {
                    @Override
                    protected void updateItem(Double amount, boolean empty) {
                        super.updateItem(amount, empty);
                        if (empty || amount == null) {
                            setText(null);
                        } else {
                            setText(String.format("$%.2f", amount));
                        }
                    }
                });
                
                lateFeesCollectedColumn.setCellFactory(col -> new TableCell<PaymentAnalyticsData, Double>() {
                    @Override
                    protected void updateItem(Double amount, boolean empty) {
                        super.updateItem(amount, empty);
                        if (empty || amount == null) {
                            setText(null);
                        } else {
                            setText(String.format("$%.2f", amount));
                        }
                    }
                });
                
                // Load daily analytics by default - safely
                try {
                    handleViewDailyAnalytics(null);
                } catch (Exception e) {
                    System.out.println("Could not load initial analytics data: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println("Error setting up analytics tab: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleViewDailyAnalytics(ActionEvent event) {
        try {
            LocalDate startDate = LocalDate.now().minusDays(6); // Last 7 days
            LocalDate endDate = LocalDate.now();
            loadAnalytics("daily", startDate, endDate);
        } catch (Exception e) {
            statusLabel.setText("Error loading daily analytics: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleViewWeeklyAnalytics(ActionEvent event) {
        try {
            LocalDate startDate = LocalDate.now().minusWeeks(3); // Last 4 weeks
            LocalDate endDate = LocalDate.now();
            loadAnalytics("weekly", startDate, endDate);
        } catch (Exception e) {
            statusLabel.setText("Error loading weekly analytics: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleViewMonthlyAnalytics(ActionEvent event) {
        try {
            LocalDate startDate = LocalDate.now().minusMonths(5); // Last 6 months
            LocalDate endDate = LocalDate.now();
            loadAnalytics("monthly", startDate, endDate);
        } catch (Exception e) {
            statusLabel.setText("Error loading monthly analytics: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadAnalytics(String period, LocalDate startDate, LocalDate endDate) {
        try (Connection conn = DBConnection.getConnection()) {
            // First, check if the payments table exists and has required columns
            boolean canProceed = false;
            boolean hasPaymentDateColumn = false;
            boolean hasStatusColumn = false;
            
            try {
                // Check if payments table exists
                DatabaseMetaData metaData = conn.getMetaData();
                ResultSet tables = metaData.getTables(null, null, "payments", null);
                boolean tableExists = tables.next();
                tables.close();
                
                if (tableExists) {
                    // Check for payment_date column
                    ResultSet columns = metaData.getColumns(null, null, "payments", "payment_date");
                    hasPaymentDateColumn = columns.next();
                    columns.close();
                    
                    // Check for status column
                    columns = metaData.getColumns(null, null, "payments", "status");
                    hasStatusColumn = columns.next();
                    columns.close();
                    
                    canProceed = hasPaymentDateColumn && hasStatusColumn;
                }
            } catch (SQLException e) {
                System.out.println("Payments table check failed: " + e.getMessage());
                statusLabel.setText("Analytics unavailable: payments table not found or columns missing");
                return;
            }
            
            if (!canProceed) {
                statusLabel.setText("Analytics unavailable: payments table missing required columns");
                System.out.println("Cannot proceed with analytics: payment_date column exists: " + 
                                 hasPaymentDateColumn + ", status column exists: " + hasStatusColumn);
                return;
            }
            
            // Clear previous chart data
            if (revenueChart != null) {
                revenueChart.getData().clear();
            } else {
                System.out.println("Revenue chart is null");
                return;
            }
            
            // Prepare data series
            XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
            revenueSeries.setName("Revenue");
            
            XYChart.Series<String, Number> lateFeesSeries = new XYChart.Series<>();
            lateFeesSeries.setName("Late Fees");
            
            // Data for analytics table
            ObservableList<PaymentAnalyticsData> analyticsData = FXCollections.observableArrayList();
            
            // Analytics summary totals
            double totalRevenue = 0;
            int totalBookings = 0;
            double totalLateFees = 0;
            double totalPendingPayments = 0;
            
            String sql;
            Map<String, PaymentPeriodData> periodDataMap = new HashMap<>();
            
            if ("daily".equals(period)) {
                // Daily analytics - get data for each day
                sql = "SELECT DATE(p.payment_date) as period, " +
                      "SUM(p.amount) as revenue, " +
                      "COUNT(p.id) as bookings_count, " +
                      "SUM(CASE WHEN b.end_date < p.payment_date THEN (p.amount - b.total_price) ELSE 0 END) as late_fees " +
                      "FROM payments p " +
                      "JOIN bookings b ON p.booking_id = b.id " +
                      "WHERE p.payment_date BETWEEN ? AND ? AND p.status = 'Paid' " +
                      "GROUP BY DATE(p.payment_date) " +
                      "ORDER BY period";
                
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setDate(1, java.sql.Date.valueOf(startDate));
                pstmt.setDate(2, java.sql.Date.valueOf(endDate));
                ResultSet rs = pstmt.executeQuery();
                
                // Create map with all dates in range (including ones with no data)
                for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                    String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    String displayDate = date.format(DateTimeFormatter.ofPattern("MMM dd"));
                    periodDataMap.put(dateStr, new PaymentPeriodData(displayDate, 0, 0, 0));
                }
                
                // Fill in actual data
                while (rs.next()) {
                    String dbPeriod = rs.getString("period");
                    double revenue = rs.getDouble("revenue");
                    int bookingsCount = rs.getInt("bookings_count");
                    double lateFees = rs.getDouble("late_fees");
                    
                    String displayDate = LocalDate.parse(dbPeriod).format(DateTimeFormatter.ofPattern("MMM dd"));
                    periodDataMap.put(dbPeriod, new PaymentPeriodData(displayDate, revenue, bookingsCount, lateFees));
                    
                    totalRevenue += revenue;
                    totalBookings += bookingsCount;
                    totalLateFees += lateFees;
                }
                
            } else if ("weekly".equals(period)) {
                // Weekly analytics - group by week
                sql = "SELECT YEARWEEK(p.payment_date, 1) as year_week, " +
                      "MIN(p.payment_date) as start_of_week, " +
                      "SUM(p.amount) as revenue, " +
                      "COUNT(p.id) as bookings_count, " +
                      "SUM(CASE WHEN b.end_date < p.payment_date THEN (p.amount - b.total_price) ELSE 0 END) as late_fees " +
                      "FROM payments p " +
                      "JOIN bookings b ON p.booking_id = b.id " +
                      "WHERE p.payment_date BETWEEN ? AND ? AND p.status = 'Paid' " +
                      "GROUP BY YEARWEEK(p.payment_date, 1) " +
                      "ORDER BY year_week";
                
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setDate(1, java.sql.Date.valueOf(startDate));
                pstmt.setDate(2, java.sql.Date.valueOf(endDate));
                ResultSet rs = pstmt.executeQuery();
                
                // Process weekly data
                while (rs.next()) {
                    String yearWeek = rs.getString("year_week");
                    LocalDate weekStart = rs.getDate("start_of_week").toLocalDate();
                    LocalDate weekEnd = weekStart.plusDays(6);
                    
                    String displayPeriod = weekStart.format(DateTimeFormatter.ofPattern("MMM dd")) + 
                                         " - " + weekEnd.format(DateTimeFormatter.ofPattern("MMM dd"));
                    
                    double revenue = rs.getDouble("revenue");
                    int bookingsCount = rs.getInt("bookings_count");
                    double lateFees = rs.getDouble("late_fees");
                    
                    periodDataMap.put(yearWeek, new PaymentPeriodData(displayPeriod, revenue, bookingsCount, lateFees));
                    
                    totalRevenue += revenue;
                    totalBookings += bookingsCount;
                    totalLateFees += lateFees;
                }
                
            } else if ("monthly".equals(period)) {
                // Monthly analytics
                sql = "SELECT DATE_FORMAT(p.payment_date, '%Y-%m') as month, " +
                      "SUM(p.amount) as revenue, " +
                      "COUNT(p.id) as bookings_count, " +
                      "SUM(CASE WHEN b.end_date < p.payment_date THEN (p.amount - b.total_price) ELSE 0 END) as late_fees " +
                      "FROM payments p " +
                      "JOIN bookings b ON p.booking_id = b.id " +
                      "WHERE p.payment_date BETWEEN ? AND ? AND p.status = 'Paid' " +
                      "GROUP BY DATE_FORMAT(p.payment_date, '%Y-%m') " +
                      "ORDER BY month";
                
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setDate(1, java.sql.Date.valueOf(startDate));
                pstmt.setDate(2, java.sql.Date.valueOf(endDate));
                ResultSet rs = pstmt.executeQuery();
                
                // Create map with all months in range (including ones with no data)
                LocalDate current = startDate.withDayOfMonth(1);
                while (!current.isAfter(endDate)) {
                    String monthKey = current.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                    String displayMonth = current.format(DateTimeFormatter.ofPattern("MMM yyyy"));
                    periodDataMap.put(monthKey, new PaymentPeriodData(displayMonth, 0, 0, 0));
                    current = current.plusMonths(1);
                }
                
                // Fill in actual data
                while (rs.next()) {
                    String month = rs.getString("month");
                    double revenue = rs.getDouble("revenue");
                    int bookingsCount = rs.getInt("bookings_count");
                    double lateFees = rs.getDouble("late_fees");
                    
                    // Parse year and month from the format YYYY-MM
                    int year = Integer.parseInt(month.substring(0, 4));
                    int monthNum = Integer.parseInt(month.substring(5, 7));
                    LocalDate monthDate = LocalDate.of(year, monthNum, 1);
                    String displayMonth = monthDate.format(DateTimeFormatter.ofPattern("MMM yyyy"));
                    
                    periodDataMap.put(month, new PaymentPeriodData(displayMonth, revenue, bookingsCount, lateFees));
                    
                    totalRevenue += revenue;
                    totalBookings += bookingsCount;
                    totalLateFees += lateFees;
                }
            }
            
            // Get pending payments
            PreparedStatement pendingStmt = conn.prepareStatement(
                "SELECT SUM(b.total_price) as total_pending " +
                "FROM bookings b " +
                "LEFT JOIN payments p ON b.id = p.booking_id " +
                "WHERE (p.status IS NULL OR p.status = 'Pending') " +
                "AND b.status != 'Cancelled'"
            );
            
            ResultSet pendingRs = pendingStmt.executeQuery();
            if (pendingRs.next()) {
                totalPendingPayments = pendingRs.getDouble("total_pending");
                if (pendingRs.wasNull()) {
                    totalPendingPayments = 0;
                }
            }
            
            // Add data to chart and table
            List<String> sortedPeriods = new ArrayList<>(periodDataMap.keySet());
            sortedPeriods.sort((a, b) -> a.compareTo(b));
            
            for (String key : sortedPeriods) {
                PaymentPeriodData data = periodDataMap.get(key);
                
                // Skip empty periods for a cleaner chart
                if (data.revenue <= 0 && data.bookingsCount <= 0) {
                    continue;
                }
                
                // Add to chart series
                revenueSeries.getData().add(new XYChart.Data<>(data.displayPeriod, data.revenue));
                lateFeesSeries.getData().add(new XYChart.Data<>(data.displayPeriod, data.lateFees));
                
                // Add to table data
                double avgValue = data.bookingsCount > 0 ? data.revenue / data.bookingsCount : 0;
                analyticsData.add(new PaymentAnalyticsData(
                    data.displayPeriod,
                    data.revenue,
                    data.bookingsCount,
                    avgValue,
                    data.lateFees
                ));
            }
            
            // Update the chart
            revenueChart.getData().addAll(revenueSeries, lateFeesSeries);
            
            // Update the table
            analyticsTable.setItems(analyticsData);
            
            // Update summary labels
            totalRevenueLabel.setText(String.format("$%.2f", totalRevenue));
            completedBookingsLabel.setText(String.valueOf(totalBookings));
            pendingPaymentsLabel.setText(String.format("$%.2f", totalPendingPayments));
            lateFeesLabel.setText(String.format("$%.2f", totalLateFees));
            
            // Update chart title
            String chartTitle = "";
            if ("daily".equals(period)) {
                chartTitle = "Daily Revenue (Last 7 Days)";
            } else if ("weekly".equals(period)) {
                chartTitle = "Weekly Revenue (Last 4 Weeks)";
            } else if ("monthly".equals(period)) {
                chartTitle = "Monthly Revenue (Last 6 Months)";
            }
            revenueChart.setTitle(chartTitle);
            
            statusLabel.setText("Analytics updated successfully");
            
        } catch (SQLException e) {
            statusLabel.setText("Error loading analytics: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Helper class to store payment analytics data for the table
    private static class PaymentAnalyticsData {
        private final String period;
        private final double totalRevenue;
        private final int bookingsCount;
        private final double avgBookingValue;
        private final double lateFees;
        
        public PaymentAnalyticsData(String period, double totalRevenue, int bookingsCount, 
                                    double avgBookingValue, double lateFees) {
            this.period = period;
            this.totalRevenue = totalRevenue;
            this.bookingsCount = bookingsCount;
            this.avgBookingValue = avgBookingValue;
            this.lateFees = lateFees;
        }
    }
    
    // Helper class to store temporary period data during analytics calculation
    private static class PaymentPeriodData {
        private final String displayPeriod;
        private final double revenue;
        private final int bookingsCount;
        private final double lateFees;
        
        public PaymentPeriodData(String displayPeriod, double revenue, int bookingsCount, double lateFees) {
            this.displayPeriod = displayPeriod;
            this.revenue = revenue;
            this.bookingsCount = bookingsCount;
            this.lateFees = lateFees;
        }
    }
    
    private void setupBookingPaymentTable() {
        // Configure the bookings table columns
        bookingIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().bookingId).asObject());
        customerNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().customerName));
        carInfoColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().carInfo));
        startDateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().startDate));
        endDateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().endDate));
        bookingStatusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().bookingStatus));
        originalAmountColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().originalAmount).asObject());
        lateFeeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().lateFee).asObject());
        totalAmountColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().totalAmount).asObject());
        paymentStatusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().paymentStatus));
        
        // Format dates in the table
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        startDateColumn.setCellFactory(col -> new TableCell<BookingPaymentData, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                }
            }
        });
        
        endDateColumn.setCellFactory(col -> new TableCell<BookingPaymentData, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                }
            }
        });
        
        // Setup the action column with buttons
        actionColumn.setCellFactory(col -> new TableCell<BookingPaymentData, String>() {
            private final Button completeButton = new Button("Complete");
            private final Button confirmReturnButton = new Button("Confirm Return");
            private final Button waivedLateReturnButton = new Button("Late Return - No Fee");
            private final Button processButton = new Button("Process Payment");
            private final Button cancelButton = new Button("Cancel");
            
            {
                completeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                completeButton.setOnAction(e -> {
                    BookingPaymentData booking = getTableView().getItems().get(getIndex());
                    completeBooking(booking);
                });
                
                confirmReturnButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
                confirmReturnButton.setOnAction(e -> {
                    BookingPaymentData booking = getTableView().getItems().get(getIndex());
                    confirmOnTimeReturn(booking);
                });
                
                waivedLateReturnButton.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white;");
                waivedLateReturnButton.setOnAction(e -> {
                    BookingPaymentData booking = getTableView().getItems().get(getIndex());
                    confirmLateReturnWithWaivedFee(booking);
                });
                
                processButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                processButton.setOnAction(e -> {
                    BookingPaymentData booking = getTableView().getItems().get(getIndex());
                    processPayment(booking);
                });
                
                cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                cancelButton.setOnAction(e -> {
                    BookingPaymentData booking = getTableView().getItems().get(getIndex());
                    cancelBooking(booking);
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    BookingPaymentData booking = getTableView().getItems().get(getIndex());
                    HBox buttons = new HBox(5);
                    
                    if ("Ongoing".equals(booking.bookingStatus)) {
                        buttons.getChildren().add(completeButton);
                        buttons.getChildren().add(confirmReturnButton);
                        
                        // Show the waived late return button only if there's a late fee to waive
                        if (booking.lateFee > 0) {
                            buttons.getChildren().add(waivedLateReturnButton);
                        }
                        
                        buttons.getChildren().add(cancelButton);
                    }
                    
                    if (!"Paid".equals(booking.paymentStatus) && !"Cancelled".equals(booking.bookingStatus)) {
                        buttons.getChildren().add(processButton);
                    }
                    
                    if (buttons.getChildren().isEmpty()) {
                        setGraphic(null);
                    } else {
                        setGraphic(buttons);
                    }
                }
            }
        });
    }
    
    private void loadBookingsWithPaymentInfo() {
        ObservableList<BookingPaymentData> bookingsList = FXCollections.observableArrayList();
        LocalDate today = LocalDate.now();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT b.id, b.car_id, b.customer_id, b.start_date, b.end_date, b.status as booking_status, b.total_price, " +
                 "c.brand, c.model, c.id as car_id, cust.name, p.status as payment_status " +
                 "FROM bookings b " +
                 "JOIN cars c ON b.car_id = c.id " +
                 "JOIN customers cust ON b.customer_id = cust.id " +
                 "LEFT JOIN payments p ON b.id = p.booking_id " +
                 "ORDER BY b.start_date DESC")) {
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int bookingId = rs.getInt("id");
                int carId = rs.getInt("car_id");
                String customerName = rs.getString("name");
                String carInfo = rs.getString("brand") + " " + rs.getString("model");
                LocalDate startDate = rs.getDate("start_date").toLocalDate();
                LocalDate endDate = rs.getDate("end_date").toLocalDate();
                String bookingStatus = rs.getString("booking_status");
                double originalAmount = rs.getDouble("total_price");
                
                // Calculate late fee (5% per day) if the booking is ongoing and past the end date
                double lateFee = 0.0;
                if ("Ongoing".equals(bookingStatus) && today.isAfter(endDate)) {
                    long daysLate = ChronoUnit.DAYS.between(endDate, today);
                    lateFee = originalAmount * 0.05 * daysLate;
                }
                
                double totalAmount = originalAmount + lateFee;
                String paymentStatus = rs.getString("payment_status");
                if (paymentStatus == null) paymentStatus = "Pending";
                
                BookingPaymentData bookingData = new BookingPaymentData(
                    bookingId, carId, customerName, carInfo, startDate, endDate, 
                    bookingStatus, originalAmount, lateFee, totalAmount, paymentStatus
                );
                
                bookingsList.add(bookingData);
            }
            
            if (bookingsTable != null) {
                bookingsTable.setItems(bookingsList);
                System.out.println("Loaded " + bookingsList.size() + " bookings with payment info");
            }
            
        } catch (SQLException e) {
            statusLabel.setText("Error loading bookings: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void completeBooking(BookingPaymentData booking) {
        try (Connection conn = DBConnection.getConnection()) {
            // First get the car ID from the booking
            PreparedStatement carIdStmt = conn.prepareStatement(
                "SELECT car_id FROM bookings WHERE id = ?"
            );
            carIdStmt.setInt(1, booking.bookingId);
            ResultSet rs = carIdStmt.executeQuery();
            
            if (rs.next()) {
                int carId = rs.getInt("car_id");
                
                // Update booking status
                PreparedStatement bookingStmt = conn.prepareStatement(
                    "UPDATE bookings SET status = 'Completed' WHERE id = ?"
                );
                bookingStmt.setInt(1, booking.bookingId);
                bookingStmt.executeUpdate();
                
                // Update car status
                PreparedStatement carStmt = conn.prepareStatement(
                    "UPDATE cars SET status = 'Available' WHERE id = ?"
                );
                carStmt.setInt(1, carId);
                carStmt.executeUpdate();
                
                statusLabel.setText("Booking #" + booking.bookingId + " marked as completed and car is now available");
                
                // Refresh the data
                loadBookingsWithPaymentInfo();
                loadCars();
            } else {
                statusLabel.setText("Error: Could not find car ID for booking #" + booking.bookingId);
            }
        } catch (SQLException e) {
            statusLabel.setText("Error completing booking: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void processPayment(BookingPaymentData booking) {
        // Create payment dialog
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Process Payment");
        dialog.setHeaderText("Process Payment for Booking #" + booking.bookingId);
        
        // Set the button types
        ButtonType payButtonType = new ButtonType("Process Payment", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(payButtonType, ButtonType.CANCEL);
        
        // Create the payment form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Create fields
        ComboBox<String> paymentMethodCombo = new ComboBox<>();
        paymentMethodCombo.getItems().addAll("Cash", "Credit Card", "Bank Transfer");
        paymentMethodCombo.setValue("Cash");
        
        TextField amountField = new TextField(String.format("%.2f", booking.totalAmount));
        amountField.setEditable(false);
        
        // Add fields to grid
        grid.add(new Label("Customer:"), 0, 0);
        grid.add(new Label(booking.customerName), 1, 0);
        grid.add(new Label("Car:"), 0, 1);
        grid.add(new Label(booking.carInfo), 1, 1);
        grid.add(new Label("Original Amount:"), 0, 2);
        grid.add(new Label(String.format("$%.2f", booking.originalAmount)), 1, 2);
        
        if (booking.lateFee > 0) {
            grid.add(new Label("Late Fee:"), 0, 3);
            grid.add(new Label(String.format("$%.2f", booking.lateFee)), 1, 3);
            grid.add(new Label("Total Amount:"), 0, 4);
            grid.add(new Label(String.format("$%.2f", booking.totalAmount)), 1, 4);
            grid.add(new Label("Payment Method:"), 0, 5);
            grid.add(paymentMethodCombo, 1, 5);
        } else {
            grid.add(new Label("Payment Method:"), 0, 3);
            grid.add(paymentMethodCombo, 1, 3);
        }
        
        dialog.getDialogPane().setContent(grid);
        
        // Convert the result when the payment button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == payButtonType) {
                return paymentMethodCombo.getValue();
            }
            return null;
        });
        
        // Show the dialog and process the result
        Optional<String> result = dialog.showAndWait();
        
        result.ifPresent(paymentMethod -> {
            try (Connection conn = DBConnection.getConnection()) {
                // Check if payment exists first
                PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT id FROM payments WHERE booking_id = ?"
                );
                checkStmt.setInt(1, booking.bookingId);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    // Update existing payment
                    PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE payments SET amount = ?, method = ?, status = 'Paid' WHERE booking_id = ?"
                    );
                    updateStmt.setDouble(1, booking.totalAmount);
                    updateStmt.setString(2, paymentMethod);
                    updateStmt.setInt(3, booking.bookingId);
                    updateStmt.executeUpdate();
                } else {
                    // Create new payment
                    PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO payments (booking_id, amount, method, status) VALUES (?, ?, ?, 'Paid')"
                    );
                    insertStmt.setInt(1, booking.bookingId);
                    insertStmt.setDouble(2, booking.totalAmount);
                    insertStmt.setString(3, paymentMethod);
                    insertStmt.executeUpdate();
                }
                
                statusLabel.setText("Payment processed successfully for booking #" + booking.bookingId);
                
                // Refresh the data
                loadBookingsWithPaymentInfo();
                
            } catch (SQLException e) {
                statusLabel.setText("Error processing payment: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    @FXML
    private void handleViewPayments(ActionEvent event) {
        // Switch to the Payment Tracking tab and refresh the data
        if (mainTabPane != null) {
            // If there's a second tab (index 1), select it
            if (mainTabPane.getTabs().size() > 1) {
                mainTabPane.getSelectionModel().select(1);
            }
        }
        // Load the payment data regardless of tab selection
        loadBookingsWithPaymentInfo();
        statusLabel.setText("Payment tracking data refreshed");
    }
    
    private void setupContextMenu() {
        contextMenu = new ContextMenu();
        
        MenuItem availableItem = new MenuItem("Set Available");
        availableItem.setOnAction(e -> updateCarStatus("Available"));
        
        MenuItem rentedItem = new MenuItem("Set Rented");
        rentedItem.setOnAction(e -> updateCarStatus("Rented"));
        
        MenuItem maintenanceItem = new MenuItem("Set Maintenance");
        maintenanceItem.setOnAction(e -> updateCarStatus("Maintenance"));
        
        MenuItem bookCarItem = new MenuItem("Book Car for User");
        bookCarItem.setOnAction(e -> bookCarForUser());
        
        MenuItem deleteItem = new MenuItem("Delete Car");
        deleteItem.setOnAction(e -> deleteCar());
        
        contextMenu.getItems().addAll(availableItem, rentedItem, maintenanceItem, bookCarItem, deleteItem);
        
        // Add context menu to table on right-click
        carsTable.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.SECONDARY) { // Right click
                car selectedCar = carsTable.getSelectionModel().getSelectedItem();
                
                // Only enable booking for Available cars
                bookCarItem.setDisable(selectedCar == null || !"Available".equals(selectedCar.getStatus()));
                
                contextMenu.show(carsTable, event.getScreenX(), event.getScreenY());
            }
        });
    }
    
    private void deleteCar() {
        car selectedCar = carsTable.getSelectionModel().getSelectedItem();
        if (selectedCar == null) {
            statusLabel.setText("Please select a car to delete");
            return;
        }
        
        // Confirm before deletion
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Car");
        confirmAlert.setHeaderText("Delete Car Confirmation");
        confirmAlert.setContentText("Are you sure you want to delete the selected car? \n" + 
                                   "Make: " + selectedCar.getBrand() + "\n" +
                                   "Model: " + selectedCar.getModel());
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM cars WHERE id = ?")) {
                pstmt.setInt(1, selectedCar.getCarId());
                
                int affected = pstmt.executeUpdate();
                if (affected > 0) {
                    statusLabel.setText("Car deleted successfully");
                    // Remove from the table
                    loadCars(); // Refresh the table to reflect changes
                } else {
                    statusLabel.setText("Failed to delete car");
                }
            } catch (SQLException e) {
                statusLabel.setText("Error deleting car: " + e.getMessage());
                e.printStackTrace();
                
                // Show error alert
                Alert errorAlert = new Alert(AlertType.ERROR);
                errorAlert.setTitle("Database Error");
                errorAlert.setHeaderText("Error Deleting Car");
                errorAlert.setContentText("An error occurred while deleting the car: " + e.getMessage());
                errorAlert.showAndWait();
            }
        }
    }
    
    private void updateCarStatus(String newStatus) {
        car selectedCar = carsTable.getSelectionModel().getSelectedItem();
        if (selectedCar == null) {
            statusLabel.setText("Please select a car to update status");
            return;
        }
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE cars SET status = ? WHERE id = ?")) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, selectedCar.getCarId());
            
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                statusLabel.setText("Car status updated to: " + newStatus);
                // Update the local object to reflect the change
                selectedCar.setStatus(newStatus);
                carsTable.refresh();
            } else {
                statusLabel.setText("Failed to update car status");
            }
        } catch (SQLException e) {
            statusLabel.setText("Error updating status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewCars(ActionEvent event) {
        loadCars();
    }

    @FXML
    private void handleViewFourSeaters(ActionEvent event) {
        loadCarsBySeats(4, 4);
    }
    
    @FXML
    private void handleViewFivePlusSeaters(ActionEvent event) {
        loadCarsBySeats(5, 10);
    }

    @FXML
    private void handleAddCar(ActionEvent event) throws IOException {
        // Load the addCar.fxml window with the correct path
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrental/addCar.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Add New Car");
        stage.show();
    }

    @FXML
    private void handleManageUsers(ActionEvent event) throws IOException {
        try {
            // Load the manageUsers.fxml window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/carrental/manageUsers.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Manage Users");
            stage.show();
            statusLabel.setText("Manage Users window opened");
        } catch (IOException e) {
            statusLabel.setText("Error opening Manage Users window: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void bookCarForUser() {
        car selectedCar = carsTable.getSelectionModel().getSelectedItem();
        if (selectedCar == null) {
            statusLabel.setText("Please select a car to book");
            return;
        }
        
        if (!"Available".equals(selectedCar.getStatus())) {
            statusLabel.setText("Selected car is not available for booking");
            return;
        }
        
        // Create and configure the booking dialog
        Dialog<BookingData> dialog = new Dialog<>();
        dialog.setTitle("Book Car for User");
        dialog.setHeaderText("Book " + selectedCar.getBrand() + " " + selectedCar.getModel() + " for a User");
        
        // Set the button types
        ButtonType bookButtonType = new ButtonType("Book", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(bookButtonType, ButtonType.CANCEL);
        
        // Create the booking form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Load users into ComboBox
        ComboBox<UserData> userComboBox = new ComboBox<>();
        loadUsersIntoComboBox(userComboBox);
        
        DatePicker startDatePicker = new DatePicker(LocalDate.now());
        DatePicker endDatePicker = new DatePicker(LocalDate.now().plusDays(1));
        
        // Add Label for estimated price/payment due
        Label estimatedPriceLabel = new Label("Estimated Price: $" + selectedCar.getPricePerDay());
        Label bookingStatusLabel = new Label("");
        bookingStatusLabel.setStyle("-fx-text-fill: red;");
        
        // Add listeners to update price when dates change and check for conflicts
        startDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            updateEstimatedPrice(startDatePicker, endDatePicker, selectedCar, estimatedPriceLabel);
            checkBookingConflicts(selectedCar.getCarId(), startDatePicker.getValue(), endDatePicker.getValue(), bookingStatusLabel);
        });
        
        endDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            updateEstimatedPrice(startDatePicker, endDatePicker, selectedCar, estimatedPriceLabel);
            checkBookingConflicts(selectedCar.getCarId(), startDatePicker.getValue(), endDatePicker.getValue(), bookingStatusLabel);
        });
        
        // Initialize with current selection
        updateEstimatedPrice(startDatePicker, endDatePicker, selectedCar, estimatedPriceLabel);
        
        grid.add(new Label("Select User:"), 0, 0);
        grid.add(userComboBox, 1, 0);
        grid.add(new Label("Start Date:"), 0, 1);
        grid.add(startDatePicker, 1, 1);
        grid.add(new Label("End Date:"), 0, 2);
        grid.add(endDatePicker, 1, 2);
        grid.add(new Label("Payment Due:"), 0, 3);
        grid.add(estimatedPriceLabel, 1, 3);
        grid.add(bookingStatusLabel, 0, 4, 2, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // Check for conflicts initially
        checkBookingConflicts(selectedCar.getCarId(), startDatePicker.getValue(), endDatePicker.getValue(), bookingStatusLabel);
        
        // Disable the book button if there's a conflict
        final Button bookButton = (Button) dialog.getDialogPane().lookupButton(bookButtonType);
        bookButton.disableProperty().bind(bookingStatusLabel.textProperty().isNotEmpty());
        
        // Convert the result to BookingData when the book button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == bookButtonType) {
                if (userComboBox.getValue() == null) {
                    statusLabel.setText("Please select a user");
                    return null;
                }
                
                LocalDate startDate = startDatePicker.getValue();
                LocalDate endDate = endDatePicker.getValue();
                
                if (startDate == null || endDate == null) {
                    statusLabel.setText("Please select both start and end dates");
                    return null;
                }
                
                if (startDate.isAfter(endDate)) {
                    statusLabel.setText("Start date cannot be after end date");
                    return null;
                }
                
                return new BookingData(
                    userComboBox.getValue().id,
                    selectedCar.getCarId(),
                    startDate,
                    endDate,
                    selectedCar.getPricePerDay()
                );
            }
            return null;
        });
        
        // Show the dialog and process the result
        Optional<BookingData> result = dialog.showAndWait();
        
        result.ifPresent(bookingData -> {
            // Calculate total price (days * price per day)
            long days = ChronoUnit.DAYS.between(bookingData.startDate, bookingData.endDate) + 1;
            double totalPrice = days * bookingData.pricePerDay;
            
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO bookings (car_id, customer_id, start_date, end_date, status, total_price) VALUES (?, ?, ?, ?, ?, ?)"
                 )) {
                pstmt.setInt(1, bookingData.carId);
                pstmt.setInt(2, bookingData.customerId);
                pstmt.setDate(3, java.sql.Date.valueOf(bookingData.startDate));
                pstmt.setDate(4, java.sql.Date.valueOf(bookingData.endDate));
                pstmt.setString(5, "Ongoing");
                pstmt.setDouble(6, totalPrice);
                
                int affected = pstmt.executeUpdate();
                
                if (affected > 0) {
                    // Update car status to Rented
                    updateCarStatus("Rented");
                    
                    // Show booking confirmation with payment due
                    showBookingConfirmation(bookingData, totalPrice);
                    
                    // Refresh payment tracking data
                    loadBookingsWithPaymentInfo();
                    
                    // Update status label
                    statusLabel.setText("Car booked successfully. Payment due: $" + String.format("%.2f", totalPrice));
                } else {
                    statusLabel.setText("Failed to book car");
                }
            } catch (SQLException e) {
                statusLabel.setText("Error booking car: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private void loadUsersIntoComboBox(ComboBox<UserData> comboBox) {
        ObservableList<UserData> users = FXCollections.observableArrayList();
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT id, name, license_number FROM customers");
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(new UserData(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("license_number")
                ));
            }
            
            comboBox.setItems(users);
            
            // Set custom cell factory to display user name and license
            comboBox.setCellFactory(param -> new ListCell<UserData>() {
                @Override
                protected void updateItem(UserData item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.name + " (License: " + item.licenseNumber + ")");
                    }
                }
            });
            
            // Also set a custom string converter for the selected value
            comboBox.setConverter(new javafx.util.StringConverter<UserData>() {
                @Override
                public String toString(UserData user) {
                    return user == null ? "" : user.name + " (License: " + user.licenseNumber + ")";
                }
                
                @Override
                public UserData fromString(String string) {
                    return null; // Not needed for this use case
                }
            });
            
        } catch (SQLException e) {
            statusLabel.setText("Error loading users: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Helper class to store user data for the combo box
    private static class UserData {
        private final int id;
        private final String name;
        private final String licenseNumber;
        
        public UserData(int id, String name, String licenseNumber) {
            this.id = id;
            this.name = name;
            this.licenseNumber = licenseNumber;
        }
    }
    
    // Helper class to store booking form data
    private static class BookingData {
        private final int customerId;
        private final int carId;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final double pricePerDay;
        
        public BookingData(int customerId, int carId, LocalDate startDate, LocalDate endDate, double pricePerDay) {
            this.customerId = customerId;
            this.carId = carId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.pricePerDay = pricePerDay;
        }
    }
    
    // Helper class to store booking and payment data for the table
    private static class BookingPaymentData {
        private final int bookingId;
        private final int carId;
        private final String customerName;
        private final String carInfo;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final String bookingStatus;
        private final double originalAmount;
        private final double lateFee;
        private final double totalAmount;
        private final String paymentStatus;
        
        public BookingPaymentData(int bookingId, int carId, String customerName, String carInfo, 
                                  LocalDate startDate, LocalDate endDate, String bookingStatus,
                                  double originalAmount, double lateFee, double totalAmount,
                                  String paymentStatus) {
            this.bookingId = bookingId;
            this.carId = carId;
            this.customerName = customerName;
            this.carInfo = carInfo;
            this.startDate = startDate;
            this.endDate = endDate;
            this.bookingStatus = bookingStatus;
            this.originalAmount = originalAmount;
            this.lateFee = lateFee;
            this.totalAmount = totalAmount;
            this.paymentStatus = paymentStatus;
        }
    }

    // Helper method to update estimated price based on selected dates
    private void updateEstimatedPrice(DatePicker startDatePicker, DatePicker endDatePicker, 
                                    car selectedCar, Label priceLabel) {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                priceLabel.setText("Invalid date range");
            } else {
                long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                double totalPrice = days * selectedCar.getPricePerDay();
                priceLabel.setText(String.format("$%.2f (%d days × $%.2f)", 
                        totalPrice, days, selectedCar.getPricePerDay()));
            }
        }
    }

    // Show booking confirmation dialog with payment details
    private void showBookingConfirmation(BookingData bookingData, double totalPrice) {
        // Get user and car details
        String userName = "";
        String carInfo = "";
        
        try (Connection conn = DBConnection.getConnection()) {
            // Get user info
            PreparedStatement userStmt = conn.prepareStatement(
                "SELECT name FROM customers WHERE id = ?"
            );
            userStmt.setInt(1, bookingData.customerId);
            ResultSet userRs = userStmt.executeQuery();
            if (userRs.next()) {
                userName = userRs.getString("name");
            }
            
            // Get car info
            PreparedStatement carStmt = conn.prepareStatement(
                "SELECT brand, model FROM cars WHERE id = ?"
            );
            carStmt.setInt(1, bookingData.carId);
            ResultSet carRs = carStmt.executeQuery();
            if (carRs.next()) {
                carInfo = carRs.getString("brand") + " " + carRs.getString("model");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Create dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Booking Confirmation");
        dialog.setHeaderText("Car booked successfully");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        grid.add(new Label("Customer:"), 0, 0);
        grid.add(new Label(userName), 1, 0);
        grid.add(new Label("Car:"), 0, 1);
        grid.add(new Label(carInfo), 1, 1);
        grid.add(new Label("Start Date:"), 0, 2);
        grid.add(new Label(bookingData.startDate.toString()), 1, 2);
        grid.add(new Label("End Date:"), 0, 3);
        grid.add(new Label(bookingData.endDate.toString()), 1, 3);
        grid.add(new Label("Total Payment Due:"), 0, 4);
        grid.add(new Label(String.format("$%.2f", totalPrice)), 1, 4);
        
        ButtonType okButton = new ButtonType("OK", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(okButton);
        dialog.getDialogPane().setContent(grid);
        
        dialog.showAndWait();
    }

    // Add method to check for booking conflicts
    private void checkBookingConflicts(int carId, LocalDate startDate, LocalDate endDate, Label statusLabel) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            statusLabel.setText("Invalid date range");
            return;
        }
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT * FROM bookings " +
                 "WHERE car_id = ? " +
                 "AND status NOT IN ('Cancelled', 'Completed') " +
                 "AND ((start_date BETWEEN ? AND ?) " +
                 "OR (end_date BETWEEN ? AND ?) " +
                 "OR (start_date <= ? AND end_date >= ?))"
             )) {
            
            pstmt.setInt(1, carId);
            pstmt.setDate(2, java.sql.Date.valueOf(startDate));
            pstmt.setDate(3, java.sql.Date.valueOf(endDate));
            pstmt.setDate(4, java.sql.Date.valueOf(startDate));
            pstmt.setDate(5, java.sql.Date.valueOf(endDate));
            pstmt.setDate(6, java.sql.Date.valueOf(startDate));
            pstmt.setDate(7, java.sql.Date.valueOf(endDate));
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // Found a conflict
                LocalDate conflictStart = rs.getDate("start_date").toLocalDate();
                LocalDate conflictEnd = rs.getDate("end_date").toLocalDate();
                
                statusLabel.setText("Booking conflict: Car already booked from " + 
                                    conflictStart + " to " + conflictEnd);
            } else {
                // No conflict
                statusLabel.setText("");
            }
        } catch (SQLException e) {
            statusLabel.setText("Error checking booking conflicts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Add a method to cancel a booking
    private void cancelBooking(BookingPaymentData booking) {
        System.out.println("Attempting to cancel booking #" + booking.bookingId + " for car ID " + booking.carId);
        
        // Confirm before cancellation
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cancel Booking");
        confirmAlert.setHeaderText("Cancel Booking Confirmation");
        confirmAlert.setContentText("Are you sure you want to cancel booking #" + booking.bookingId + "?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DBConnection.getConnection()) {
                // First update the booking status
                PreparedStatement bookingStmt = conn.prepareStatement(
                    "UPDATE bookings SET status = 'Cancelled' WHERE id = ?"
                );
                bookingStmt.setInt(1, booking.bookingId);
                int bookingUpdated = bookingStmt.executeUpdate();
                
                // Then update the car status back to Available - use car ID directly
                PreparedStatement carStmt = conn.prepareStatement(
                    "UPDATE cars SET status = 'Available' WHERE id = ?"
                );
                carStmt.setInt(1, booking.carId);
                int carUpdated = carStmt.executeUpdate();
                
                statusLabel.setText("Booking #" + booking.bookingId + " has been cancelled (booking updated: " + 
                                  bookingUpdated + ", car updated: " + carUpdated + ")");
                
                // Refresh the data
                loadBookingsWithPaymentInfo();
                loadCars();
                
                System.out.println("Booking cancelled successfully: booking #" + booking.bookingId + 
                                 " for car ID " + booking.carId + " updated status");
            } catch (SQLException e) {
                statusLabel.setText("Error cancelling booking: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Add a method to confirm on-time return
    private void confirmOnTimeReturn(BookingPaymentData booking) {
        System.out.println("Attempting to confirm on-time return for booking #" + booking.bookingId + 
                         " for car ID " + booking.carId);
        
        // Confirm before processing
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm On-Time Return");
        confirmAlert.setHeaderText("Confirm Car Returned On Time");
        confirmAlert.setContentText("Confirm that the car was returned on time for booking #" + booking.bookingId + 
                                   "? This will waive any late fees.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DBConnection.getConnection()) {
                // Update the booking to completed status
                PreparedStatement bookingStmt = conn.prepareStatement(
                    "UPDATE bookings SET status = 'Completed' WHERE id = ?"
                );
                bookingStmt.setInt(1, booking.bookingId);
                int bookingUpdated = bookingStmt.executeUpdate();
                
                // Update the car to available - use car ID directly
                PreparedStatement carStmt = conn.prepareStatement(
                    "UPDATE cars SET status = 'Available' WHERE id = ?"
                );
                carStmt.setInt(1, booking.carId);
                int carUpdated = carStmt.executeUpdate();
                
                statusLabel.setText("Booking #" + booking.bookingId + " completed with on-time return (booking updated: " + 
                                  bookingUpdated + ", car updated: " + carUpdated + ")");
                
                // Refresh the data
                loadBookingsWithPaymentInfo();
                loadCars();
                
                System.out.println("On-time return confirmed successfully for booking #" + booking.bookingId);
            } catch (SQLException e) {
                statusLabel.setText("Error processing on-time return: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Method to handle late return with waived fee
    private void confirmLateReturnWithWaivedFee(BookingPaymentData booking) {
        System.out.println("Attempting to confirm late return with waived fee for booking #" + booking.bookingId);
        
        // Confirm before processing
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Late Return - Waive Fee");
        confirmAlert.setHeaderText("Confirm Car Returned Late - Waive Late Fee");
        confirmAlert.setContentText("Confirm that the car was returned late for booking #" + booking.bookingId + 
                                   " but you want to waive the late fee of $" + String.format("%.2f", booking.lateFee) + "?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DBConnection.getConnection()) {
                // Update the booking to completed status
                PreparedStatement bookingStmt = conn.prepareStatement(
                    "UPDATE bookings SET status = 'Completed' WHERE id = ?"
                );
                bookingStmt.setInt(1, booking.bookingId);
                int bookingUpdated = bookingStmt.executeUpdate();
                
                // Update the car to available
                PreparedStatement carStmt = conn.prepareStatement(
                    "UPDATE cars SET status = 'Available' WHERE id = ?"
                );
                carStmt.setInt(1, booking.carId);
                int carUpdated = carStmt.executeUpdate();
                
                // Check if a payment record exists, if not create one with original amount only (no late fee)
                PreparedStatement checkPaymentStmt = conn.prepareStatement(
                    "SELECT id FROM payments WHERE booking_id = ?"
                );
                checkPaymentStmt.setInt(1, booking.bookingId);
                ResultSet rs = checkPaymentStmt.executeQuery();
                
                if (rs.next()) {
                    // Update existing payment to original amount only (no late fee)
                    PreparedStatement updatePaymentStmt = conn.prepareStatement(
                        "UPDATE payments SET amount = ?, status = 'Pending' WHERE booking_id = ?"
                    );
                    updatePaymentStmt.setDouble(1, booking.originalAmount);
                    updatePaymentStmt.setInt(2, booking.bookingId);
                    updatePaymentStmt.executeUpdate();
                } else {
                    // Create new payment with original amount only
                    PreparedStatement insertPaymentStmt = conn.prepareStatement(
                        "INSERT INTO payments (booking_id, amount, status) VALUES (?, ?, 'Pending')"
                    );
                    insertPaymentStmt.setInt(1, booking.bookingId);
                    insertPaymentStmt.setDouble(2, booking.originalAmount);
                    insertPaymentStmt.executeUpdate();
                }
                
                statusLabel.setText("Booking #" + booking.bookingId + " completed with late return but fee waived");
                
                // Refresh the data
                loadBookingsWithPaymentInfo();
                loadCars();
                
                System.out.println("Late return with waived fee confirmed for booking #" + booking.bookingId);
            } catch (SQLException e) {
                statusLabel.setText("Error processing late return with waived fee: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Method to load cars filtered by seat capacity
    private void loadCarsBySeats(int minSeats, int maxSeats) {
        ObservableList<car> carList = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT * FROM cars WHERE seats >= ? AND seats <= ?")) {
            
            pstmt.setInt(1, minSeats);
            pstmt.setInt(2, maxSeats);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                car car = new car(
                        rs.getInt("id"),
                        rs.getString("model"),
                        rs.getString("brand"),
                        rs.getInt("year"),
                        rs.getDouble("price_per_day"),
                        rs.getString("status"),
                        rs.getInt("seats"),
                        rs.getBoolean("is_special")
                );
                carList.add(car);
            }
            
            carsTable.setItems(carList);
            carsTable.refresh();
            
            if (minSeats == maxSeats) {
                statusLabel.setText("Showing " + carList.size() + " cars with " + minSeats + " seats");
            } else {
                statusLabel.setText("Showing " + carList.size() + " cars with " + minSeats + "+ seats");
            }
            
        } catch (SQLException e) {
            statusLabel.setText("Error loading cars by seat capacity: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadCars() {
        ObservableList<car> carList = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM cars");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                car car = new car(
                        rs.getInt("id"),
                        rs.getString("model"),
                        rs.getString("brand"),
                        rs.getInt("year"),
                        rs.getDouble("price_per_day"),
                        rs.getString("status"),
                        rs.getInt("seats"),
                        rs.getBoolean("is_special")
                );
                carList.add(car);
                System.out.println("Loaded car: " + car.getBrand() + " " + car.getModel() + " (Seats: " + car.getSeats() + ", Special: " + car.isSpecial() + ")");
            }
            carsTable.setItems(carList);
            System.out.println("Set " + carList.size() + " items to TableView");
            carsTable.refresh(); // Ensure the TableView refreshes
            statusLabel.setText("Loaded " + carList.size() + " cars successfully");
        } catch (SQLException e) {
            statusLabel.setText("Error loading cars: " + e.getMessage());
            e.printStackTrace();
        }
    }
}