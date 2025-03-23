package com.example.workshop2.admin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop2.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EventReviewActivity extends AppCompatActivity {

    private TextView eventName, eventDescription, eventLocation, eventDate, eventTime, eventFeeType, eventCapacity, eventPrice, eventCreatorName;
    private Button approveButton, rejectButton;
    private FirebaseFirestore db;

    private String eventId; // Event ID passed from the previous activity

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_review);

        db = FirebaseFirestore.getInstance();

        // Initialize views
        eventName = findViewById(R.id.eventName);
        eventDescription = findViewById(R.id.eventDescription);
        eventLocation = findViewById(R.id.eventLocation);
        eventDate = findViewById(R.id.eventDate);
        eventTime = findViewById(R.id.eventTime);
        eventFeeType = findViewById(R.id.eventFeeType);
        eventCapacity = findViewById(R.id.eventCapacity);
        eventPrice = findViewById(R.id.eventPrice);
        eventCreatorName = findViewById(R.id.eventCreatorName); // New TextView for creator's name
        approveButton = findViewById(R.id.approveButton);
        rejectButton = findViewById(R.id.rejectButton);

        // Get the event ID from the intent
        eventId = getIntent().getStringExtra("eventId");

        if (eventId != null) {
            loadEventDetails(eventId);
        } else {
            Toast.makeText(this, "Error loading event details", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set up click listeners
        approveButton.setOnClickListener(v -> updateEventStatus("Approved"));
        rejectButton.setOnClickListener(v -> updateEventStatus("Rejected"));
    }

    private void loadEventDetails(String eventId) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        bindEventDetails(documentSnapshot);
                        String userId = documentSnapshot.getString("userId");
                        if (userId != null) {
                            fetchUserName(userId);
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error fetching event details", Toast.LENGTH_SHORT).show());
    }

    private void bindEventDetails(DocumentSnapshot document) {
        eventName.setText(document.getString("name"));
        eventDescription.setText(document.getString("description"));
        eventLocation.setText(document.getString("location"));
        eventDate.setText(document.getString("date"));
        eventTime.setText(document.getString("time"));
        eventFeeType.setText(document.getString("feeType"));
        eventCapacity.setText(String.valueOf(document.getLong("capacity")));
        eventPrice.setText(String.valueOf(document.getDouble("entryPrice")));
    }

    private void fetchUserName(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        if (fullName != null && !fullName.isEmpty()) {
                            eventCreatorName.setText("Submitted By: " + fullName);
                        } else {
                            eventCreatorName.setText("Submitted By: Unknown User");
                        }
                    } else {
                        eventCreatorName.setText("Submitted By: Unknown User");
                    }
                })
                .addOnFailureListener(e -> {
                    eventCreatorName.setText("Error fetching user name");
                });
    }

    private void updateEventStatus(String status) {
        if (eventId != null) {
            db.collection("events").document(eventId)
                    .update("status", status)
                    .addOnSuccessListener(aVoid -> {
                        String message = status.equals("Approved") ? "Event Approved!" : "Event Rejected!";
                        Toast.makeText(EventReviewActivity.this, message, Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity after updating
                    })
                    .addOnFailureListener(e -> Toast.makeText(EventReviewActivity.this, "Error updating event status", Toast.LENGTH_SHORT).show());
        }
    }
}
