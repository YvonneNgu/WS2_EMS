package com.example.workshop2.organiser;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop2.R;
import com.example.workshop2.model.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditEventActivity extends AppCompatActivity {

    private EditText eventNameEditText, eventDescriptionEditText, eventLocationEditText,
            eventDateEditText, eventPriceEditText, eventCapacityEditText, eventTimeEditText;
    private Spinner eventCategorySpinner;
    private Button saveButton;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        // Initialize views
        eventNameEditText = findViewById(R.id.eventNameEditText);
        eventDescriptionEditText = findViewById(R.id.eventDescriptionEditText);
        eventLocationEditText = findViewById(R.id.eventLocationEditText);
        eventDateEditText = findViewById(R.id.eventDateEditText);
        eventPriceEditText = findViewById(R.id.eventPriceEditText);
        eventCapacityEditText = findViewById(R.id.eventCapacityEditText);
        eventCategorySpinner = findViewById(R.id.eventCategorySpinner);
        saveButton = findViewById(R.id.saveButton);
        eventTimeEditText = findViewById(R.id.eventTimeEditText);

        // Populate the category spinner
        loadCategoriesIntoSpinner();

        // Get the event ID passed from the previous activity
        eventId = getIntent().getStringExtra("eventId");

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Event ID is missing", Toast.LENGTH_SHORT).show();
            finish(); // End activity if no event ID is provided
        } else {
            // Load event details using the event ID
            loadEventDetails(eventId);
        }

        // Set save button click listener to save updated event details
        saveButton.setOnClickListener(v -> saveEventChanges());

        // Set date picker dialog for eventDateEditText
        eventDateEditText.setOnClickListener(v -> showDatePickerDialog());

        // Set time picker dialog for eventTimeEditText
        eventTimeEditText.setOnClickListener(v -> showTimePickerDialog());
    }

    // Method to load categories into the spinner from strings.xml
    private void loadCategoriesIntoSpinner() {
        // Get categories from strings.xml
        String[] categories = getResources().getStringArray(R.array.event_categories);

        // Create an ArrayAdapter using the categories from strings.xml
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventCategorySpinner.setAdapter(adapter);
    }

    // Method to load the event details from Firestore and populate the EditText fields
    private void loadEventDetails(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        if (event != null) {
                            // Populate EditText fields with event details
                            eventNameEditText.setText(event.getName());
                            eventDescriptionEditText.setText(event.getDescription());
                            eventLocationEditText.setText(event.getLocation());
                            eventDateEditText.setText(event.getDate());
                            eventPriceEditText.setText(String.valueOf(event.getEntryPrice()));
                            eventCapacityEditText.setText(String.valueOf(event.getCapacity()));
                            eventTimeEditText.setText(event.getTime());

                            // Set the category spinner selection
                            ArrayAdapter<String> adapter = (ArrayAdapter<String>) eventCategorySpinner.getAdapter();
                            int position = adapter.getPosition(event.getCategory());
                            eventCategorySpinner.setSelection(position);
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load event details", Toast.LENGTH_SHORT).show());
    }

    // Method to show the date picker dialog
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    eventDateEditText.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    // Method to show the time picker dialog
    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    eventTimeEditText.setText(timeFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    // Method to save the changes made by the user
    private void saveEventChanges() {
        String updatedName = eventNameEditText.getText().toString().trim();
        String updatedDescription = eventDescriptionEditText.getText().toString().trim();
        String updatedLocation = eventLocationEditText.getText().toString().trim();
        String updatedDate = eventDateEditText.getText().toString().trim();
        String updatedPriceStr = eventPriceEditText.getText().toString().trim();
        String updatedCapacityStr = eventCapacityEditText.getText().toString().trim();
        String updatedCategory = eventCategorySpinner.getSelectedItem().toString();
        String updatedTime = eventTimeEditText.getText().toString().trim();
        String updatedStatus = "Approved";

        // Validate input fields
        if (updatedName.isEmpty() || updatedDescription.isEmpty() || updatedLocation.isEmpty() ||
                updatedDate.isEmpty() || updatedTime.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double updatedPrice;
        int updatedCapacity;
        try {
            updatedPrice = Double.parseDouble(updatedPriceStr);
            updatedCapacity = Integer.parseInt(updatedCapacityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numeric values for price and capacity", Toast.LENGTH_SHORT).show();
            return;
        }

        String updatedFeeType = updatedPrice > 0 ? "Paid" : "Free";

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Create an updated Event object
        Event updatedEvent = new Event(eventId, updatedName, updatedDescription, updatedLocation, updatedDate, updatedCategory, updatedCapacity, updatedPrice, updatedFeeType, userId, updatedTime,updatedStatus);

        // Save the updated event to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId).set(updatedEvent)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event updated successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to the previous screen after success
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update event", Toast.LENGTH_SHORT).show());
    }
}