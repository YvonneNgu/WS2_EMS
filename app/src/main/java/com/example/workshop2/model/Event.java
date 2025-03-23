package com.example.workshop2.model;

public class Event {

    private String eventId; // Firestore document ID
    private String name;
    private String description;
    private String location;
    private String date;
    private String category;
    private int capacity;
    private double entryPrice;
    private String feeType;
    private String userId;
    private String time;
    private String status;

    // Empty constructor needed for Firestore deserialization
    public Event() {
    }

    // Constructor with parameters
    public Event(String eventId, String name, String description, String location, String date,
                 String category, int capacity, double entryPrice, String feeType, String userId, String time,String status) {
        this.eventId = eventId;
        this.name = name;
        this.description = description;
        this.location = location;
        this.date = date;
        this.category = category;
        this.capacity = capacity;
        this.entryPrice = entryPrice;
        this.feeType = feeType;
        this.userId = userId;
        this.time = time;
        this.status = status;
    }

    // Getters and Setters for each field
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public double getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(double entryPrice) {
        this.entryPrice = entryPrice;
    }

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;

    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
