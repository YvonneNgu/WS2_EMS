package com.example.workshop2.cert;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import com.example.workshop2.R;
import com.example.workshop2.cert.model.CertificateItem;
import com.example.workshop2.cert.model.CertificateItemImage;
import com.example.workshop2.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class CertificateItemController {
    private Map<String, CertificateItem> certificateItemText;
    private Map<String, CertificateItemImage> certificateItemImages;
    private int templateWidth = 1000;
    private int templateHeight = 1000;
    private static float maxTextLength;
    private String certificateType;
    private Event event;
    private  String organizerName;

    public CertificateItemController(){

    }

    public CertificateItemController(String certificateType, String eventId, EventCallback callback) {
        this.certificateItemText = new HashMap<>();
        this.certificateItemImages = new HashMap<>();
        this.certificateType = certificateType;

        // Fetch the event asynchronously
        getEvent(eventId, callback);
    }

    public void setTemplateDimensions(int templateWidth, int templateHeight) {
        this.templateWidth = templateWidth;
        this.templateHeight = templateHeight;
        maxTextLength = templateWidth * 0.85f;
    }

    public void defaultSetup(String[] itemTextList){
        // set up text
        for(String text : itemTextList){
            addItem(text);
        }
    }

    // Create and setup
    public void addItem(String text){
        // set up text
        String itemText = getItemDefaultText(text); // set text
        if(itemText!=null) {
            Log.d("Controller - add item", text + ": " + itemText);
            certificateItemText.put(text, new CertificateItem(itemText));
            setDefaultStyle(text);  // text style
            resetPosition(text);    // position
        }
    }

    public void addItem(String text, CertificateItem item){
        certificateItemText.put(text, item);
    }

    public String getItemDefaultText(String key){
        switch (key){
            case "Certificate Type":
                return "CERTIFICATE OF " + certificateType.toUpperCase();
            case "Header":
                if(certificateType.equals("Achievement"))
                    return "This certificate is awarded to";
                else
                    return "This certificate is proudly presented to";
            case "Recipient Name":
                return "[Recipient Name]";
            case "Reason for Certificate":
                if(certificateType.equals("Participation"))
                    return "for attending and participating in";
                else
                    return "In recognition of outstanding performance in";
            case "Achievement":
                if(certificateType.equals("Achievement"))
                    return "[Achievement Description]";
                else
                    return null;
            case "Event Name":
                if(certificateType.equals("Participation"))
                    return event.getName();
                else
                    return "during " + event.getName();
            case "Event Date":
                return "held on "+ event.getDate();
            case "Issuer Name":
                return "Presented by " + organizerName;
            case "Issue Date":

                return "Issued on [Date]";
            default:
                return null;
        }
    }

    private void setDefaultStyle(String key){
        String fontStyle = "DEFAULT";
        float fontSize = templateHeight * 0.04f;
        boolean isBold = false;

        switch (key){
            case "Certificate Type":
                fontSize = templateHeight * 0.075f;
                isBold = true;
                break;
            case "Header":
                break;
            case "Recipient Name":
                fontSize = templateHeight * 0.06f;
                break;
            case "Reason for Certificate":
                isBold = true;
                break;
            case "Achievement":
                isBold = true;
                break;
            case "Event Name":
                isBold = true;
                break;
            case "Event Date":
                isBold = true;
                break;
            case "Issuer Name":
                break;
            case "Issue Date":
                fontSize = templateHeight * 0.03f;
                break;
            default:
        }
        updateItemFont(key, fontStyle);
        updateItemFontSize(key, (int) fontSize);
        updateItemBold(key, isBold);
    }

    private void resetPosition(String key) {
        // Set default position based on key
        int x = templateWidth/2;
        int y = templateHeight/2;
        switch (key) {
            case "Certificate Type":
                y = (int) (templateHeight * 0.28);
                break;
            case "Header":
                y = (int) (templateHeight * 0.34);
                break;
            case "Recipient Name":
                y = (int) (templateHeight * 0.475);
                break;
            case "Reason for Certificate":
                y = (int) (templateHeight * 0.6);
                break;
            case "Achievement":
                y = (int) (templateHeight * 0.64);
                break;
            case "Event Name":
                if(Objects.equals(certificateType, "Achievement"))
                    y = (int) (templateHeight * 0.68);
                else
                    y = (int) (templateHeight * 0.67);  // Participation
                break;
            case "Event Date":
                if(Objects.equals(certificateType, "Achievement"))
                    y = (int) (templateHeight * 0.72);
                else
                    y = (int) (templateHeight * 0.72);
                break;
            case "Issuer Name":
                y = (int) (templateHeight * 0.81);
                break;
            case "Issue Date":
                y = (int) (templateHeight * 0.9);
                break;
        }
        updateItemPosition(key, new PointF(x,y));
    }

    private void getEvent(String id, EventCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        event = documentSnapshot.toObject(Event.class);
                        callback.onEventLoaded(event); // Notify that the event is loaded
                    } else {
                        callback.onEventLoaded(null); // Notify that the event is not found
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CertificateItemController", "Error getting event", e);
                    callback.onEventLoaded(null); // Notify that there was an error
                });
    }

    public void getOrganizer(String userId, OrganizerCallback callback) {
        Log.d("Controller - get Org Name", "User Id: " + userId);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String organizerName = documentSnapshot.getString("orgName");
                        Log.d("Controller - get Org Name", "Organizer: " + organizerName);
                        callback.onOrganizerLoaded(organizerName);
                    } else {
                        Log.d("Controller - get Org Name", "Org not found");
                        callback.onOrganizerLoaded(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Controller - get Org Name", "Error getting organizer", e);
                    callback.onOrganizerLoaded(null);
                });
    }

    // Define callback interface
    public interface OrganizerCallback {
        void onOrganizerLoaded(String organizerName);
    }
    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    // Get a certificate item
    public CertificateItem getItem(String key) {
        return certificateItemText.get(key);
    }

    // Update the position of a certificate item
    public void updateItemPosition(String key, PointF position) {
        CertificateItem item = certificateItemText.get(key);
        if (item != null) {
            item.setPosition(position);
        }
    }

    public void updateItemFont(String key, String fontStyle) {
        CertificateItem item = certificateItemText.get(key);
        if (item != null) {
            item.setFontStyle(fontStyle);
        }
    }

    public void updateItemAlignment(String key, CertificateItem.Alignment alignment) {
        CertificateItem item = certificateItemText.get(key);

        int newXPosition;
        if (alignment == CertificateItem.Alignment.CENTER) {
            newXPosition = (int) (templateWidth * 0.5);
        } else if (alignment == CertificateItem.Alignment.RIGHT) {
            newXPosition = (int) (templateWidth * 0.93);
        } else newXPosition = (int) (templateWidth * 0.07);

        item.setPosition(new PointF(newXPosition, item.getPosition().y));
        item.setAlignment(alignment);
    }

    // Update the text of a certificate item
    public void updateItemText(String key, String text) {
        CertificateItem item = certificateItemText.get(key);
        if (item != null) {
            item.setText(text);
        }
    }

    // Update the style of a certificate item
    public void updateItemFontSize(String key, float fontSize) {
        CertificateItem item = certificateItemText.get(key);
        if (item != null) {
            item.setFontSize(fontSize);
            Log.d("CertificateItemController", "Font size updated: " + fontSize + " for item: " + key);
        }
    }

    public void updateItemTextColor(String key, int textColor) {
        CertificateItem item = certificateItemText.get(key);
        if (item != null) {
            item.setTextColor(textColor);
        }
    }

    public void updateItemBold(String key, boolean isBold) {
        CertificateItem item = certificateItemText.get(key);
        if (item != null) {
            item.setBold(isBold);
        }
    }

    public void updateItemItalic(String key, boolean isItalic) {
        CertificateItem item = certificateItemText.get(key);
        if (item != null) {
            item.setItalic(isItalic);
        }
    }

    public void updateItemUnderline(String key, boolean isUnderline) {
        CertificateItem item = certificateItemText.get(key);
        if (item != null) {
            item.setUnderline(isUnderline);
        }
    }

    // Get all certificate items
    public Map<String, CertificateItem> getAllItems() {
        return new HashMap<>(certificateItemText);
    }

    // Delete
    public void deleteItem(String key) {
        Log.d("removed", key);
        certificateItemText.remove(key);
    }

    /*public static Paint setupPaint(CertificateItem item){
        Paint paint = new Paint();
        paint.setTextSize(item.getFontSize());
        paint.setColor(item.getTextColor());
        paint.setAntiAlias(true);

        // Set font style
        switch (item.getFontStyle()) {
            case "MONOSPACE":
                paint.setTypeface(Typeface.MONOSPACE);
                break;
            case "SANS_SERIF":
                paint.setTypeface(Typeface.SANS_SERIF);
                break;
            case "SERIF":
                paint.setTypeface(Typeface.SERIF);
                break;
            default:
                paint.setTypeface(Typeface.DEFAULT);
        }

        if (item.isBold()) {
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        }
        if (item.isItalic()) {
            Typeface currentTypeface = paint.getTypeface();
            paint.setTypeface(Typeface.create(currentTypeface, currentTypeface.getStyle() | Typeface.ITALIC));
        }
        if (item.isUnderline()) {
            paint.setFlags(paint.getFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }

        // Handle text alignment
        switch (item.getAlignment()) {
            case CENTER:
                paint.setTextAlign(Paint.Align.CENTER);
                break;
            case RIGHT:
                paint.setTextAlign(Paint.Align.RIGHT);
                break;
            default:
                paint.setTextAlign(Paint.Align.LEFT);
                break;
        }
        return paint;
    }*/

    public static Paint setupPaint(CertificateItem item) {
        Paint paint = new Paint();
        paint.setTextSize(item.getFontSize());
        paint.setColor(item.getTextColor());
        paint.setAntiAlias(true);

        Context context = MyApplication.getAppContext();
        Typeface customTypeface = null;

        // Set font style using custom fonts
        switch (item.getFontStyle()) {
            case "FONT_MONOSPACE":
                customTypeface = ResourcesCompat.getFont(context, R.font.monospace);
                break;
            case "FONT_SANS_SERIF":
                customTypeface = ResourcesCompat.getFont(context, R.font.sans_serif);
                break;
            case "FONT_SERIF":
                customTypeface = ResourcesCompat.getFont(context, R.font.serif);
                break;
            case "FONT_SIGNATURE":
                customTypeface = ResourcesCompat.getFont(context, R.font.signature);
                break;
            default:
                customTypeface = Typeface.DEFAULT;
        }

        paint.setTypeface(customTypeface);

        // Apply text styling
        if (item.isBold()) {
            paint.setTypeface(Typeface.create(customTypeface, Typeface.BOLD));
        }
        if (item.isItalic()) {
            Typeface currentTypeface = paint.getTypeface();
            paint.setTypeface(Typeface.create(currentTypeface, currentTypeface.getStyle() | Typeface.ITALIC));
        }
        if (item.isUnderline()) {
            paint.setFlags(paint.getFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }

        // Handle text alignment
        switch (item.getAlignment()) {
            case CENTER:
                paint.setTextAlign(Paint.Align.CENTER);
                break;
            case RIGHT:
                paint.setTextAlign(Paint.Align.RIGHT);
                break;
            default:
                paint.setTextAlign(Paint.Align.LEFT);
                break;
        }
        return paint;
    }

    public float getItemTop(CertificateItem item){
        Paint paint = setupPaint(item);
        float lineHeight = paint.getFontSpacing();
        List<String> lines = splitTextIntoLines(item.getText(), paint);
        return item.getPosition().y - lineHeight - (lines.size() - 1) * lineHeight / 2;
    }

    public float getItemBottom(CertificateItem item) {
        Paint paint = setupPaint(item);
        List<String> lines = splitTextIntoLines(item.getText(), paint);
        float lineHeight = paint.getFontSpacing();
        return item.getPosition().y + (lines.size() - 1) * lineHeight / 2;
    }

    public float getItemRight(CertificateItem item){
        float itemWidth = getItemWidth(item);
        float right = item.getPosition().x; // right align

        switch (item.getAlignment()) {
            case CENTER:
                right += itemWidth / 2;
                break;
            case LEFT:
                right += itemWidth;
                break;
        }
        return right;
    }

    public float getItemLeft(CertificateItem item){
        float itemWidth = getItemWidth(item);
        float left = item.getPosition().x; // left align

        switch (item.getAlignment()) {
            case CENTER:
                left -= itemWidth / 2;
                break;
            case RIGHT:
                left -= itemWidth;
                break;
        }
        return left;
    }

    public float getItemWidth(CertificateItem item){
        Paint paint = setupPaint(item);
        List<String> lines = splitTextIntoLines(item.getText(), paint);
        return getMaxTextLength(lines, paint);
    }

    // Get greatest value of multiple lines(text)
    private float getMaxTextLength(List<String> lines, Paint paint){
        float maxLength = 0;
        for (String line : lines) {
            float textLength = paint.measureText(line);
            if(textLength > maxLength)
                maxLength = textLength;
        }
        return maxLength;
    }

    public void checkTextLength(String text, Paint paint){

    }

    public static List<String> splitTextIntoLines(String text, Paint paint) {
        List<String> lines = new ArrayList<>();
        String[] paragraphs = text.split("\n");

        // text too long
        if(paint.measureText(text)>maxTextLength)
            for (String paragraph : paragraphs) {
                String[] words = paragraph.split("\\s");
                StringBuilder line = new StringBuilder();

                for (String word : words) {
                    if (line.length() == 0) {
                        line.append(word);
                    } else {
                        float lineWidth = paint.measureText(line + " " + word);
                        if (lineWidth <= maxTextLength) {
                            line.append(" ").append(word);
                        } else {
                            lines.add(line.toString());
                            line = new StringBuilder(word);
                        }
                    }
                }

                if (line.length() > 0) {
                    lines.add(line.toString());
                }
            }
        else    // check enter
            lines.addAll(Arrays.asList(paragraphs));

        return lines;
    }

    // Add methods for handling image items
    public void addImageItem(String key, CertificateItemImage item) {
        resetImagePosition(key);
        certificateItemImages.put(key, item);
    }

    private void resetImagePosition(String key) {
        // Set default position for images
        int x = templateWidth/2;
        int y = templateHeight/2;

        CertificateItemImage item = certificateItemImages.get(key);
        if (item != null) {
            // Center the image by default
            x -= item.getImageWidth() / 2;
            y -= item.getImageHeight() / 2;
            updateImagePosition(key, new PointF(x, y));
        }
    }

    public void updateImagePosition(String key, PointF position) {
        CertificateItemImage item = certificateItemImages.get(key);
        if (item != null) {
            item.setPosition(position);
        }
    }

    public CertificateItemImage getImageItem(String key) {
        return certificateItemImages.get(key);
    }

    public void deleteImageItem(String key) {
        certificateItemImages.remove(key);
    }

    public Map<String, CertificateItemImage> getAllImageItems() {
        return new HashMap<>(certificateItemImages);
    }

    //  for image bounds
    public float getImageLeft(CertificateItemImage item) {
        return item.getPosition().x;
    }

    public float getImageRight(CertificateItemImage item) {
        return item.getPosition().x + item.getImageWidth();
    }

    public float getImageTop(CertificateItemImage item) {
        return item.getPosition().y;
    }

    public float getImageBottom(CertificateItemImage item) {
        return item.getPosition().y + item.getImageHeight();
    }

    // Method to check if an image is within template bounds
    public boolean isImageWithinBounds(CertificateItemImage item) {
        float left = getImageLeft(item);
        float right = getImageRight(item);
        float top = getImageTop(item);
        float bottom = getImageBottom(item);

        return left >= 0 && right <= templateWidth &&
                top >= 0 && bottom <= templateHeight;
    }

    // Method to constrain image position within template bounds
    public PointF constrainImagePosition(CertificateItemImage item, PointF newPosition) {
        float x = newPosition.x;
        float y = newPosition.y;

        // Constrain x position
        if (x < 0) x = 0;
        if (x + item.getImageWidth() > templateWidth) {
            x = templateWidth - item.getImageWidth();
        }

        // Constrain y position
        if (y < 0) y = 0;
        if (y + item.getImageHeight() > templateHeight) {
            y = templateHeight - item.getImageHeight();
        }

        return new PointF(x, y);
    }
}

