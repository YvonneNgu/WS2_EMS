package com.example.workshop2.cert.model;
import com.google.firebase.Timestamp;

public class GeneratedCert {
    private String id;
    private String eventId;
    private String participantId;
    private String participantName;
    private Timestamp issueDate;
    private String fileContent;
    private String fileName;
    private String certificateType;

    // Default constructor required for Firestore
    public GeneratedCert() {}

    public GeneratedCert(String eventId, String participantId, Timestamp issueDate,
                         String fileContent, String fileName, String certificateType) {
        this.eventId = eventId;
        this.participantId = participantId;
        this.issueDate = issueDate;
        this.fileContent = fileContent;
        this.fileName = fileName;
        this.certificateType = certificateType;
    }

    // Getters and setters

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    public Timestamp getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Timestamp issueDate) {
        this.issueDate = issueDate;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }
}

