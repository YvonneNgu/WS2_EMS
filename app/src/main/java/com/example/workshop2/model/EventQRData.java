package com.example.workshop2.model;

public class EventQRData {
    private String eventName;
    private String eventId;
    private String userId;
    private String userName;
    private String userDOB;
    private String userEmail;
    private String userPhoneNumber;
    private String userIdCard;
    private String status;

    // Default constructor required for Firestore
    public EventQRData() {
    }

    // Constructor to initialize all fields
    public EventQRData(String eventName, String eventId, String userId, String userName, String userDOB, String userEmail, String userPhoneNumber, String userIdCard, String status) {
        this.eventName = eventName;
        this.eventId = eventId;
        this.userId = userId;
        this.userName = userName;
        this.userDOB = userDOB;
        this.userEmail = userEmail;
        this.userPhoneNumber = userPhoneNumber;
        this.userIdCard = userIdCard;
        this.status = status;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserDOB() {
        return userDOB;
    }

    public void setUserDOB(String userDOB) {
        this.userDOB = userDOB;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserIdCard() {
        return userIdCard;
    }

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public void setUserIdCard(String userIdCard) {
        this.userIdCard = userIdCard;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
