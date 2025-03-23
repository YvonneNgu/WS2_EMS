/*
package com.example.workshop2.organiser;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreateEventActivity extends AppCompatActivity {

    private EditText eventNameEditText, eventDescriptionEditText, eventLocationEditText, eventDateEditText, eventTimeEditText, eventCapacityEditText, eventPriceEditText;
    private RadioGroup eventFeeGroup;
    private Spinner eventCategorySpinner;
    private Button createEventButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // Initialize Firebase components
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        eventNameEditText = findViewById(R.id.eventName);
        eventDescriptionEditText = findViewById(R.id.eventDescription);
        eventLocationEditText = findViewById(R.id.eventLocation);
        eventDateEditText = findViewById(R.id.eventDate);
        eventTimeEditText = findViewById(R.id.eventTime);  // New field for event time
        eventCapacityEditText = findViewById(R.id.eventCapacity);
        eventPriceEditText = findViewById(R.id.eventPrice);
        eventFeeGroup = findViewById(R.id.eventFeeGroup);
        eventCategorySpinner = findViewById(R.id.eventCategory);
        createEventButton = findViewById(R.id.createEventButton);

        // Set up Spinner with categories
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.event_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventCategorySpinner.setAdapter(adapter);

        // Date picker for event date
        eventDateEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(CreateEventActivity.this,
                    (view, year, monthOfYear, dayOfMonth) -> eventDateEditText.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth),
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        // Time picker for event time
        eventTimeEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(CreateEventActivity.this,
                    (view, hourOfDay, minute1) -> eventTimeEditText.setText(String.format("%02d:%02d", hourOfDay, minute1)),
                    hour, minute, true);
            timePickerDialog.show();
        });

        // Show or hide the price field based on the fee type selection
        eventFeeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedFeeButton = findViewById(checkedId);
            if (selectedFeeButton.getText().toString().equals("Paid")) {
                eventPriceEditText.setVisibility(View.VISIBLE); // Show the price input field
            } else {
                eventPriceEditText.setVisibility(View.GONE); // Hide the price input field
            }
        });

        createEventButton.setOnClickListener(v -> {
            // Collect event details
            String eventName = eventNameEditText.getText().toString();
            String eventDescription = eventDescriptionEditText.getText().toString();
            String eventLocation = eventLocationEditText.getText().toString();
            String eventDate = eventDateEditText.getText().toString();
            String eventTime = eventTimeEditText.getText().toString();  // Get event time input
            String eventCapacity = eventCapacityEditText.getText().toString();
            String eventCategory = eventCategorySpinner.getSelectedItem().toString(); // Get selected category

            int selectedFeeId = eventFeeGroup.getCheckedRadioButtonId();
            RadioButton selectedFeeButton = findViewById(selectedFeeId);
            String feeType = selectedFeeButton.getText().toString();

            String entryPrice = "0"; // Default entry price
            if (feeType.equals("Paid")) {
                entryPrice = eventPriceEditText.getText().toString();
            }

            if (eventName.isEmpty() || eventDescription.isEmpty() || eventLocation.isEmpty() || eventDate.isEmpty() || eventTime.isEmpty() || eventCapacity.isEmpty()) {
                Toast.makeText(CreateEventActivity.this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = auth.getCurrentUser().getUid();

            // Create event map with status as Pending
            Map<String, Object> event = new HashMap<>();
            event.put("name", eventName);
            event.put("description", eventDescription);
            event.put("category", eventCategory);
            event.put("location", eventLocation);
            event.put("date", eventDate);
            event.put("time", eventTime);
            event.put("capacity", Integer.parseInt(eventCapacity));
            event.put("feeType", feeType);
            event.put("entryPrice", feeType.equals("Paid") ? Double.parseDouble(entryPrice) : 0);
            event.put("userId", userId);
            event.put("status", "Pending"); // Add status field

            // Add event to Firestore
            db.collection("events").add(event)
                    .addOnSuccessListener(documentReference -> {
                        String eventId = documentReference.getId();
                        Map<String, Object> eventIdMap = new HashMap<>();
                        eventIdMap.put("eventId", eventId);

                        documentReference.update(eventIdMap)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(CreateEventActivity.this, "Event Submitted for Approval", Toast.LENGTH_SHORT).show();
                                    finish(); // Close the activity
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(CreateEventActivity.this, "Error updating event ID", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CreateEventActivity.this, "Error creating event", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
*/
package com.example.workshop2.organiser;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreateEventActivity extends AppCompatActivity {

    private EditText eventNameEditText, eventDescriptionEditText, eventLocationEditText, eventDateEditText, eventTimeEditText, eventCapacityEditText, eventPriceEditText;
    private RadioGroup eventFeeGroup;
    private Spinner eventCategorySpinner;
    private Button createEventButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // Initialize Firebase components
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        eventNameEditText = findViewById(R.id.eventName);
        eventDescriptionEditText = findViewById(R.id.eventDescription);
        eventLocationEditText = findViewById(R.id.eventLocation);
        eventDateEditText = findViewById(R.id.eventDate);
        eventTimeEditText = findViewById(R.id.eventTime);
        eventCapacityEditText = findViewById(R.id.eventCapacity);
        eventPriceEditText = findViewById(R.id.eventPrice);
        eventFeeGroup = findViewById(R.id.eventFeeGroup);
        eventCategorySpinner = findViewById(R.id.eventCategory);
        createEventButton = findViewById(R.id.createEventButton);

        // Set up Spinner with categories
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.event_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventCategorySpinner.setAdapter(adapter);

        // Date picker for event date
        eventDateEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(CreateEventActivity.this,
                    (view, year, monthOfYear, dayOfMonth) -> eventDateEditText.setText(String.format("%04d-%02d-%02d", year, monthOfYear + 1, dayOfMonth)),
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        // Time picker for event time
        eventTimeEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(CreateEventActivity.this,
                    (view, hourOfDay, minute1) -> eventTimeEditText.setText(String.format("%02d:%02d", hourOfDay, minute1)),
                    hour, minute, true);
            timePickerDialog.show();
        });

        // Show or hide the price field based on the fee type selection
        eventFeeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedFeeButton = findViewById(checkedId);
            if (selectedFeeButton != null && selectedFeeButton.getText().toString().equals("Paid")) {
                eventPriceEditText.setVisibility(View.VISIBLE); // Show the price input field
            } else {
                eventPriceEditText.setVisibility(View.GONE); // Hide the price input field
            }
        });

        createEventButton.setOnClickListener(v -> {
            // Collect event details
            String eventName = eventNameEditText.getText().toString().trim();
            String eventDescription = eventDescriptionEditText.getText().toString().trim();
            String eventLocation = eventLocationEditText.getText().toString().trim();
            String eventDate = eventDateEditText.getText().toString().trim();
            String eventTime = eventTimeEditText.getText().toString().trim();
            String eventCapacity = eventCapacityEditText.getText().toString().trim();
            String eventCategory = eventCategorySpinner.getSelectedItem().toString();

            int selectedFeeId = eventFeeGroup.getCheckedRadioButtonId();
            if (selectedFeeId == -1) {
                Toast.makeText(CreateEventActivity.this, "Please select a fee type", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedFeeButton = findViewById(selectedFeeId);
            String feeType = selectedFeeButton.getText().toString();
            String entryPrice = feeType.equals("Paid") ? eventPriceEditText.getText().toString().trim() : "0";

            // Validate all inputs
            if (eventName.isEmpty()) {
                Toast.makeText(CreateEventActivity.this, "Event name is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (eventDescription.isEmpty()) {
                Toast.makeText(CreateEventActivity.this, "Event description is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (eventLocation.isEmpty()) {
                Toast.makeText(CreateEventActivity.this, "Event location is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (eventDate.isEmpty()) {
                Toast.makeText(CreateEventActivity.this, "Event date is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (eventTime.isEmpty()) {
                Toast.makeText(CreateEventActivity.this, "Event time is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (eventCapacity.isEmpty()) {
                Toast.makeText(CreateEventActivity.this, "Event capacity is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (feeType.equals("Paid") && entryPrice.isEmpty()) {
                Toast.makeText(CreateEventActivity.this, "Event price is required for paid events", Toast.LENGTH_SHORT).show();
                return;
            }

            int capacity;
            double price;
            try {
                capacity = Integer.parseInt(eventCapacity);
                if (capacity <= 0) {
                    Toast.makeText(CreateEventActivity.this, "Event capacity must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(CreateEventActivity.this, "Event capacity must be a valid number", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                price = feeType.equals("Paid") ? Double.parseDouble(entryPrice) : 0;
                if (feeType.equals("Paid") && price <= 0) {
                    Toast.makeText(CreateEventActivity.this, "Price must be greater than 0 for paid events", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(CreateEventActivity.this, "Event price must be a valid number", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = auth.getCurrentUser().getUid();

            // Create event map with all required fields
            Map<String, Object> event = new HashMap<>();
            event.put("name", eventName);
            event.put("description", eventDescription);
            event.put("category", eventCategory);
            event.put("location", eventLocation);
            event.put("date", eventDate);
            event.put("time", eventTime);
            event.put("capacity", capacity);
            event.put("feeType", feeType);
            event.put("entryPrice", price);
            event.put("userId", userId);
            event.put("status", "Pending"); // Default status

            // Add event to Firestore
            db.collection("events").add(event)
                    .addOnSuccessListener(documentReference -> {
                        String eventId = documentReference.getId();
                        Map<String, Object> eventIdMap = new HashMap<>();
                        eventIdMap.put("eventId", eventId);

                        documentReference.update(eventIdMap)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(CreateEventActivity.this, "Event Submitted for Approval", Toast.LENGTH_SHORT).show();
                                    finish(); // Close the activity
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(CreateEventActivity.this, "Error updating event ID", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CreateEventActivity.this, "Error creating event", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
