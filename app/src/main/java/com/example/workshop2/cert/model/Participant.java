package com.example.workshop2.cert.model;

public class Participant {
    private String id;
    private String fullName;
    private String attendanceStatus;
    private boolean hasParticipationCert;
    private boolean hasAchievementCert;
    private boolean isSelected;

    public Participant(String id, String fullName, String attendanceStatus) {
        this.id = id;
        this.fullName = fullName;
        this.attendanceStatus = attendanceStatus;
        this.hasParticipationCert = false;
        this.hasAchievementCert = false;
        this.isSelected = false;
    }

    // Getters and setters
    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getAttendanceStatus() { return attendanceStatus; }
    public boolean hasParticipationCert() { return hasParticipationCert; }
    public boolean hasAchievementCert() { return hasAchievementCert; }
    public boolean isSelected() { return isSelected; }

    public void setHasParticipationCert(boolean hasParticipationCert) {
        this.hasParticipationCert = hasParticipationCert;
    }

    public void setHasAchievementCert(boolean hasAchievementCert) {
        this.hasAchievementCert = hasAchievementCert;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}

