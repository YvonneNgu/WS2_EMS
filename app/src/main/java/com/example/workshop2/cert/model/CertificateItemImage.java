package com.example.workshop2.cert.model;

import android.graphics.PointF;

public class CertificateItemImage {
    private PointF position;
    private String imageContent; // Base64 encoded image content
    private float imageWidth;
    private float imageHeight;

    public CertificateItemImage(String imageContent, float imageWidth, float imageHeight) {
        this.imageContent = imageContent;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.position = new PointF(0, 0); // Default position
    }

    // Getters and setters
    public PointF getPosition() {
        return position;
    }

    public void setPosition(PointF position) {
        this.position = position;
    }

    public String getImageContent() {
        return imageContent;
    }

    public void setImageContent(String imageContent) {
        this.imageContent = imageContent;
    }

    public float getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(float imageWidth) {
        this.imageWidth = imageWidth;
    }

    public float getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(float imageHeight) {
        this.imageHeight = imageHeight;
    }
}
