package com.example.workshop2.participant;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop2.model.EventQRData;
import com.example.workshop2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import android.graphics.Bitmap;
import android.graphics.Color;
import com.google.zxing.BarcodeFormat;
//test
import com.google.zxing.common.BitMatrix;

//test
import org.json.JSONObject;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class GenerateQR extends AppCompatActivity {

    private ImageView qrCodeImageView;
    private TextView eventNameLabel, userNameLabel;
    private Button downloadButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_qr);  // Ensure this layout exists in your project

        qrCodeImageView = findViewById(R.id.qrCode);
        eventNameLabel = findViewById(R.id.eventNameLabel);
        userNameLabel = findViewById(R.id.userNameLabel);
        downloadButton = findViewById(R.id.downloadButton);

        if (qrCodeImageView == null) {
            Toast.makeText(this, "Error: QR Code ImageView not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the event name and ID passed from the Intent
        final String eventName = getIntent().getStringExtra("eventName");
        final String eventId = getIntent().getStringExtra("eventId");

        auth = FirebaseAuth.getInstance();
        final String userId = auth.getCurrentUser().getUid();

        // Retrieve the user's document from Firestore
        DocumentReference userDocRef = FirebaseFirestore.getInstance().collection("users").document(userId);
        userDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                final String currentUserName = task.getResult().getString("fullName");
                final String userDOB = task.getResult().getString("dob");
                final String userEmail = task.getResult().getString("email");
                final String userPhoneNumber = task.getResult().getString("phoneNumber");
                final String userIdCard = task.getResult().getString("idCard");
                final String status = "Absent"; // Initialize status as "Absent"

                // Set event name and user name labels
                eventNameLabel.setText("Event: " + eventName);
                userNameLabel.setText("User: " + currentUserName);

                // Display a toast with the event name for debugging
                Toast.makeText(this, "Generating QR for: " + eventName, Toast.LENGTH_SHORT).show();

                // Call the method to generate the QR code
                generateQRCode(eventName, eventId, userId, currentUserName, userDOB, userEmail, userPhoneNumber, userIdCard, status);
                storeQRCodeInFirestore(eventName, eventId, userId, currentUserName, userDOB, userEmail, userPhoneNumber, userIdCard, status);
            } else {
                Toast.makeText(this, "Failed to retrieve user details", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            // Handle any errors that occur during the document retrieval
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });

        // Set up download button
        downloadButton.setOnClickListener(v -> {
            if (qrCodeImageView.getDrawable() != null) {
                Bitmap bitmap = ((BitmapDrawable) qrCodeImageView.getDrawable()).getBitmap();
                saveBitmapToGallery(bitmap, "EventQR_" + eventId);
            } else {
                Toast.makeText(this, "No QR code to download", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateQRCode(String eventName, String eventId, String userId, String currentUserName, String userDOB, String userEmail, String userPhoneNumber, String userIdCard, String status) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            // Create a JSON object to encode data in structured format
            JSONObject qrData = new JSONObject();
            qrData.put("eventName", eventName);
            qrData.put("eventId", eventId);
            qrData.put("userId", userId);
            qrData.put("userName", currentUserName);
            qrData.put("userDOB", userDOB);
            qrData.put("userEmail", userEmail);
            qrData.put("userPhoneNumber", userPhoneNumber);
            qrData.put("userIdCard", userIdCard);
            qrData.put("status", status);

            // Encode the JSON string into a QR code
            String jsonString = qrData.toString();
            BitMatrix bitMatrix = writer.encode(
                    jsonString,
                    BarcodeFormat.QR_CODE,
                    600, 600
            );

            // Convert the BitMatrix to a Bitmap
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            // Draw text labels on the QR code bitmap
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(25);
            paint.setTextAlign(Paint.Align.LEFT);

            // Set the typeface to Calibri Bold
            paint.setTypeface(Typeface.create("Calibri", Typeface.BOLD)); // "CALIBRI" for uppercase, if necessary

            float textWidthEvent = paint.measureText(eventName);
            float textWidthName = paint.measureText(currentUserName);

            // Draw text on top left corner
            canvas.drawText("Event : " + eventName, 10, 25, paint); // event name
            canvas.drawText("Name : " + currentUserName, 10, 50, paint); // event name

            // Display the QR code in the ImageView
            qrCodeImageView.setImageBitmap(bitmap);

        } catch (WriterException | JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "QR Code generation failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void storeQRCodeInFirestore(String eventName, String eventId, String userId, String currentUserName, String userDOB, String userEmail, String userPhoneNumber, String userIdCard, String status) {

        // Prepare the data to be stored
        EventQRData qrData = new EventQRData(eventName, eventId, userId, currentUserName, userDOB, userEmail, userPhoneNumber, userIdCard, status);

        // Store the data in Firestore
        FirebaseFirestore.getInstance().collection("eventQR")
                .add(qrData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "QR code details stored successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to store QR code details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveBitmapToGallery(Bitmap bitmap, String fileName) {
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File qrFile = new File(pictureDirectory, fileName + ".png");

        try (FileOutputStream fos = new FileOutputStream(qrFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            Toast.makeText(this, "QR Code saved to gallery", Toast.LENGTH_SHORT).show();
            // Refresh the gallery to include the new image
            MediaScannerConnection.scanFile(this, new String[]{qrFile.getAbsolutePath()}, null, null);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save QR Code", Toast.LENGTH_SHORT).show();
        }
    }


}