package com.example.workshop2.admin;

import java.util.Date;

public class ReactivateRequest {
    private String userId;
    private Date requestDate;

    public ReactivateRequest(String userId, Date requestDate) {
        this.userId = userId;
        this.requestDate = requestDate;
    }

    public String getUserId() {
        return userId;
    }

    public Date getRequestDate() {
        return requestDate;
    }
}
