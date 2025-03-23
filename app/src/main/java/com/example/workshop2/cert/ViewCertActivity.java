package com.example.workshop2.cert;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop2.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ViewCertActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String certificateId;

    private ImageView ivCertificate;
    private TextView tvEventName;
    private TextView tvCertificateType;
    private TextView tvParticipantName;
    private TextView tvIssueDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_cert);

        db = FirebaseFirestore.getInstance();
        certificateId = getIntent().getStringExtra("certificateId");

        initViews();
        loadCertificate();
    }

    private void initViews() {
        ivCertificate = findViewById(R.id.ivCertificate);
        tvEventName = findViewById(R.id.tvEventName);
        tvCertificateType = findViewById(R.id.tvCertificateType);
        tvParticipantName = findViewById(R.id.tvParticipantName);
        tvIssueDate = findViewById(R.id.tvIssueDate);
    }

    private void loadCertificate() {
        db.collection("certificates").document(certificateId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fileContent = documentSnapshot.getString("fileContent");
                        String eventId = documentSnapshot.getString("eventId");
                        String participantId = documentSnapshot.getString("participantId");
                        String certificateType = documentSnapshot.getString("certificateType");
                        Timestamp issueDate = documentSnapshot.getTimestamp("issueDate");

                        displayCertificate(fileContent);
                        loadEventName(eventId);
                        loadParticipantName(participantId);
                        tvCertificateType.setText("Certificate Type: " + certificateType);

                        if (issueDate != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
                            String issueDateString = sdf.format(issueDate.toDate());
                            tvIssueDate.setText("Issue Date: " + issueDateString);
                        }
                    } else {
                        showToast("Certificate not found!");
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to load certificate: " + e.getMessage());
                });
    }

    private void displayCertificate(String fileContent) {
        try {
            byte[] decodedString = Base64.decode(fileContent, Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            ivCertificate.setImageBitmap(decodedBitmap);
        } catch (Exception e) {
            showToast("Failed to display certificate: " + e.getMessage());
        }
    }

    private void loadEventName(String eventId) {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String eventName = documentSnapshot.getString("name");
                        tvEventName.setText("Event: " + eventName);
                    } else {
                        tvEventName.setText("Event not found!");
                    }
                })
                .addOnFailureListener(e -> {
                    tvEventName.setText("Failed to load event!");
                    showToast("Error loading event: " + e.getMessage());
                });
    }

    private void loadParticipantName(String participantId) {
        db.collection("users").document(participantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        tvParticipantName.setText("Participant: " + fullName);
                    } else {
                        tvParticipantName.setText("Participant not found!");
                    }
                })
                .addOnFailureListener(e -> {
                    tvParticipantName.setText("Failed to load participant!");
                    showToast("Error loading participant: " + e.getMessage());
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
