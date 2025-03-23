package com.example.workshop2.cert.model;

import com.google.firebase.Timestamp;

import java.util.List;
import java.util.Map;

public class Design {
    String id;          // Unique identifier for the design
    String designName;
    String certificateType;   // Type of certificate (Participation/Achievement)
    String templateId;        // ID of the template used
    List<Map<String, Object>> certificateItems;  // List of items on the certificate
    Timestamp lastModified;

    public Design(){}

    public Design(String id, String name, String templateId, List<Map<String, Object>> certificateItems) {
        this.id = id;
        this.designName = name;
        this.templateId = templateId;
        this.certificateItems = certificateItems;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDesignName() {
        return designName;
    }

    public void setDesignName(String designName) {
        this.designName = designName;
    }

    public String getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public List<Map<String, Object>> getCertificateItems() {
        return certificateItems;
    }

    public void setCertificateItems(List<Map<String, Object>> certificateItems) {
        this.certificateItems = certificateItems;
    }

    public Timestamp getLastModified() {
        return lastModified;
    }

    public void setLastModified(Timestamp lastModified) {
        this.lastModified = lastModified;
    }
}