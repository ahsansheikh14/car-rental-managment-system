package com.example.carrental.models;

import java.time.LocalDate;

public class booking {
    private int bookingId;
    private int carId;
    private int customerId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private double totalCost;

    public booking(int bookingId, int carId, int customerId, LocalDate startDate, LocalDate endDate, String status, double totalCost) {
        this.bookingId = bookingId;
        this.carId = carId;
        this.customerId = customerId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.totalCost = totalCost;
    }

    // Getters and setters
    public int getBookingId() { return bookingId; }
    public int getCarId() { return carId; }
    public int getCustomerId() { return customerId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getStatus() { return status; }
    public double getTotalCost() { return totalCost; }

    public void setStatus(String status) { this.status = status; }
}