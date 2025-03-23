package com.example.workshop2.model;

public class Attendance {
    private String userName;
    private String userEmail;
    private String userPhoneNumber;
    private String userIdCard;
    private String status;

    // Constructor
    public Attendance(String userName, String userEmail, String userPhoneNumber, String userIdCard, String status) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPhoneNumber = userPhoneNumber;
        this.userIdCard = userIdCard;
        this.status = status;
    }

    // Getter methods
    public String getUserName() {
        return userName;
    }
    public String getUserEmail() {
        return userEmail;
    }
    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }
    public String getUserIdCard() {
        return userIdCard;
    }
    public String getStatus(){
        return status;
    }

    // Setter methods
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }
    public void setStatus(String status){
        this.status = status;
    }
    public void setUserIdCard(String userIdCard){
        this.userIdCard = userIdCard;
    }
}
