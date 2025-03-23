package com.example.workshop2.model;

public class User {

    public String fullName;
    public String dob;
    public String idCard;
    public String orgName;
    public String position;
    public String email;
    private String userId;  // private field for userId (from code 1)
    public String userType;
    public String phoneNumber;  // New field for phone number (from code 2)

    // Constructor
    public User(String fullName, String dob, String idCard, String orgName, String position, String email, String userId, String userType, String phoneNumber) {
        this.fullName = fullName;
        this.dob = dob != null ? dob : "";  // If dob is null, set it to an empty string
        this.idCard = idCard != null ? idCard : "";  // If idCard is null, set it to an empty string
        this.orgName = orgName != null ? orgName : "";  // If orgName is null, set it to an empty string
        this.position = position != null ? position : "";  // If position is null, set it to an empty string
        this.email = email;
        this.userId = userId;  // Set the userId
        this.userType = userType;
        this.phoneNumber = phoneNumber != null ? phoneNumber : "";  // If phoneNumber is null, set it to an empty string
    }

    // No-argument constructor (required by Firestore)
    public User() {
    }
    // Getter for userId (to access the private field)
    public String getUserId() {
        return userId;
    }

    // Getter methods for other fields
    public String getFullName() {
        return fullName;
    }

    public String getDob() {
        return dob;
    }

    public String getIdCard() {
        return idCard;
    }

    public String getOrgName() {
        return orgName;
    }

    public String getPosition() {
        return position;
    }

    public String getEmail() {
        return email;
    }

    public String getUserType() {
        return userType;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    // Setter methods for other fields
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
