package com.example.workshop2.cert.model;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

public class CertificateItem {
    private String title;
    private String text;
    private PointF position;
    private String fontStyle;
    private float fontSize;
    private int textColor;
    private boolean isBold;
    private boolean isItalic;
    private boolean isUnderline;
    private Alignment alignment;

    public enum Alignment {
        LEFT, CENTER, RIGHT
    }

    public CertificateItem(String text) {
        this.text = text;
        this.position = new PointF(0, 0);
        this.fontStyle = "DEFAULT";  // Initialize with DEFAULT font style
        this.fontSize = 40;
        this.textColor = Color.BLACK;
        this.isBold = false;
        this.isItalic = false;
        this.isUnderline = false;
        this.alignment = Alignment.CENTER;
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public PointF getPosition() {
        return position;
    }

    public void setPosition(PointF position) {
        this.position = position;
    }

    public String getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
    }

    public float getFontSize() {
        return fontSize;
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public boolean isBold() {
        return isBold;
    }

    public void setBold(boolean bold) {
        isBold = bold;
    }

    public boolean isItalic() {
        return isItalic;
    }

    public void setItalic(boolean italic) {
        isItalic = italic;
    }

    public boolean isUnderline() {
        return isUnderline;
    }

    public void setUnderline(boolean underline) {
        isUnderline = underline;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }

    // Helper method to set text style on a Paint object
    public void applyStyle(Paint paint) {
        paint.setTextSize(fontSize);
        paint.setColor(textColor);
        int flags = 0;
        if (isBold) flags |= Paint.FAKE_BOLD_TEXT_FLAG;
        if (isUnderline) flags |= Paint.UNDERLINE_TEXT_FLAG;
        paint.setFlags(flags);
        paint.setTextSkewX(isItalic ? -0.25f : 0);

        switch (alignment) {
            case LEFT:
                paint.setTextAlign(Paint.Align.LEFT);
                break;
            case CENTER:
                paint.setTextAlign(Paint.Align.CENTER);
                break;
            case RIGHT:
                paint.setTextAlign(Paint.Align.RIGHT);
                break;
        }
    }
}