package com.example.carrental.models;

public class car {
    private int carId;
    private String model;
    private String brand;
    private int year;
    private double pricePerDay;
    private String status;
    private int seats;
    private boolean isSpecial;

    // Constructor with direct fields for seats and isSpecial
    public car(int carId, String model, String brand, int year, double pricePerDay, String status, int seats, boolean isSpecial) {
        this.carId = carId;
        this.model = model;
        this.brand = brand;
        this.year = year;
        this.pricePerDay = pricePerDay;
        this.status = status;
        this.seats = seats;
        this.isSpecial = isSpecial;
    }

    // Constructor that maintains backward compatibility
    public car(int carId, String model, String brand, int year, double pricePerDay, String status) {
        this.carId = carId;
        this.model = model;
        this.brand = brand;
        this.year = year;
        this.pricePerDay = pricePerDay;
        this.status = status;
        this.seats = 4; // Default
        this.isSpecial = false; // Default
    }

    // Getters and setters
    public int getCarId() { return carId; }
    public String getModel() { return model; }
    public String getBrand() { return brand; }
    public int getYear() { return year; }
    public double getPricePerDay() { return pricePerDay; }
    public String getStatus() { return status; }
    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }
    public boolean isSpecial() { return isSpecial; }
    public void setSpecial(boolean special) { isSpecial = special; }

    public void setStatus(String status) { this.status = status; }
}