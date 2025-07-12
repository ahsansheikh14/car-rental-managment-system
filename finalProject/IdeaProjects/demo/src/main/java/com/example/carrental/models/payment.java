package com.example.carrental.models;

import java.time.LocalDate;

public class payment {
    private int paymentId;
    private int bookingId;
    private double amount;
    private LocalDate paymentDate;
    private String method;

    public payment(int paymentId, int bookingId, double amount, LocalDate paymentDate, String method) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.method = method;
    }

    // Getters and setters
    public int getPaymentId() { return paymentId; }
    public int getBookingId() { return bookingId; }
    public double getAmount() { return amount; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public String getMethod() { return method; }
}