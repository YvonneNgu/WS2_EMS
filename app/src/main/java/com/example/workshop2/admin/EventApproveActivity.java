package com.example.workshop2.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop2.R;
import com.example.workshop2.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EventApproveActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView noEventsTextView; // TextView to display when no events are available
    private EventApprovalAdapter adapter;
    private List<Event> eventList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_approve);

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerView);
        noEventsTextView = findViewById(R.id.noEventsTextView); // Reference to the "no events" TextView

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventApprovalAdapter(eventList, event -> {
            Intent intent = new Intent(EventApproveActivity.this, EventReviewActivity.class);
            intent.putExtra("eventId", event.getEventId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // Load events for the first time
        loadPendingEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list when returning to this activity
        refreshPendingEvents();
    }

    private void loadPendingEvents() {
        db.collection("events")
                .whereEqualTo("status", "Pending") // Only load events with "Pending" status
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear(); // Clear the existing list to avoid duplicates
                    for (var document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        event.setEventId(document.getId()); // Set the event ID for reference
                        eventList.add(event);
                    }
                    // Update UI based on event list size
                    if (eventList.isEmpty()) {
                        noEventsTextView.setVisibility(View.VISIBLE); // Show "no events" message
                        recyclerView.setVisibility(View.GONE); // Hide RecyclerView
                    } else {
                        noEventsTextView.setVisibility(View.GONE); // Hide "no events" message
                        recyclerView.setVisibility(View.VISIBLE); // Show RecyclerView
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show();
                });
    }

    private void refreshPendingEvents() {
        // Clear the list and reload pending events
        eventList.clear();
        loadPendingEvents();
    }
}
