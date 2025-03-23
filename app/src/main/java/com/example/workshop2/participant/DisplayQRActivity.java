package com.example.workshop2.participant;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

public class DisplayQRActivity extends AppCompatActivity {

    private TextView eventNameTextView, eventLocationTextView, eventDateTextView, eventTimeTextView;
    private ImageView qrCodeImageView;
    private Button saveQRButton;
    private Bitmap qrCodeBitmap;  // Declare bitmap here to be accessible in multiple methods
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_qr);

        // Initialize UI elements
        eventNameTextView = findViewById(R.id.eventNameTextView);
        eventLocationTextView = findViewById(R.id.eventLocationTextView);
        eventDateTextView = findViewById(R.id.eventDateTextView);
        eventTimeTextView = findViewById(R.id.eventTimeTextView);
        qrCodeImageView = findViewById(R.id.qrCodeImageView);
        saveQRButton = findViewById(R.id.saveQRButton);

        // Get data from intent
        String eventName = getIntent().getStringExtra("eventName");
        String eventId = getIntent().getStringExtra("eventId");
        String userName = getIntent().getStringExtra("userName");
        String userDOB = getIntent().getStringExtra("userDOB");
        String userEmail = getIntent().getStringExtra("userEmail");
        String userPhoneNumber = getIntent().getStringExtra("userPhoneNumber");
        String userIdCard = getIntent().getStringExtra("userIdCard");
        String status = getIntent().getStringExtra("status");

        // Fetch event details from Firestore
        fetchEventDetails(eventId);

        auth = FirebaseAuth.getInstance();
        final String userId = auth.getCurrentUser().getUid();

        // Generate the QR Code
        generateQRCode(eventName, eventId, userId, userName, userDOB, userEmail, userPhoneNumber, userIdCard, status);

        // Save QR Code button functionality
        saveQRButton.setOnClickListener(v -> {
            if (qrCodeBitmap != null) {
                saveQRCodeImage(qrCodeBitmap, "event_qr_code");
            } else {
                Toast.makeText(this, "QR Code is not generated yet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateQRCode(String eventName, String eventId, String userId, String userName, String userDOB, String userEmail, String userPhoneNumber, String userIdCard, String status) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            // Create JSON object to encode in QR code
            JSONObject qrData = new JSONObject();
            qrData.put("eventName", eventName);
            qrData.put("eventId", eventId);
            qrData.put("userId", userId);
            qrData.put("userName", userName);
            qrData.put("userDOB", userDOB);
            qrData.put("userEmail", userEmail);
            qrData.put("userPhoneNumber", userPhoneNumber);
            qrData.put("userIdCard", userIdCard);
            qrData.put("status", status);

            String jsonString = qrData.toString();

            // Generate QR code
            qrCodeBitmap = encodeAsBitmap(jsonString);

            // Display QR code in ImageView
            qrCodeImageView.setImageBitmap(qrCodeBitmap);

        } catch (WriterException | JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap encodeAsBitmap(String str) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        Bitmap bitmap = null;
        try {
            com.google.zxing.common.BitMatrix bitMatrix = writer.encode(str, BarcodeFormat.QR_CODE, 600, 600);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
        } catch (WriterException e) {
            throw new WriterException();
        }

        return bitmap;
    }

    private void saveQRCodeImage(Bitmap bitmap, String fileName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName + ".png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QR Codes");

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (uri != null) {
            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                Toast.makeText(this, "QR Code saved to gallery", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save QR Code", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to create file", Toast.LENGTH_SHORT).show();
        }
    }


    private void fetchEventDetails(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Extract data safely
                        String eventName = documentSnapshot.getString("name");
                        String eventLocation = documentSnapshot.getString("location");
                        String eventDate = documentSnapshot.getString("date");
                        String eventTime = documentSnapshot.getString("time");

                        eventNameTextView.setText(eventName != null ? eventName : "N/A");
                        eventLocationTextView.setText("Location: " + (eventLocation != null ? eventLocation : "N/A"));
                        eventDateTextView.setText("Date: " + (eventDate != null ? eventDate : "N/A"));
                        eventTimeTextView.setText("Time: " + (eventTime != null ? eventTime : "N/A"));
                    } else {
                        Toast.makeText(DisplayQRActivity.this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DisplayQRActivity.this, "Failed to fetch event details", Toast.LENGTH_SHORT).show();
                });
    }
}
