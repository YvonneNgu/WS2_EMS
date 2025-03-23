package com.example.workshop2.model;

public class EventQR {
    private String eventId;
    private String eventName;
    private String userName;
    private String userDOB;
    private String userEmail;
    private String userPhoneNumber;
    private String userIdCard;
    private String eventDate;
    private String status;

    // Required for Firestore deserialization
    public EventQR() {}

    public EventQR(String eventId, String eventName, String userName, String userDOB,
                   String userEmail, String userPhoneNumber, String userIdCard, String status) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.userName = userName;
        this.userDOB = userDOB;
        this.userEmail = userEmail;
        this.userPhoneNumber = userPhoneNumber;
        this.userIdCard = userIdCard;
        this.status = status;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
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

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public String getUserIdCard() {
        return userIdCard;
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

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }
}
