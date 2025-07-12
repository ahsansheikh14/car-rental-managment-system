package com.example.carrental.models;

public class customer {
    private int customerId;
    private String name;
    private String contact;
    private String licenseNo;
    private String password;

    public customer(int customerId, String name, String contact, String licenseNo, String password) {
        this.customerId = customerId;
        this.name = name;
        this.contact = contact;
        this.licenseNo = licenseNo;
        this.password = password;
    }

    // Getters and setters
    public int getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getContact() { return contact; }
    public String getLicenseNo() { return licenseNo; }
    public String getPassword() { return password; }
}