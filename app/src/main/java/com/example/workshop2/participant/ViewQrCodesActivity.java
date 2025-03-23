package com.example.workshop2.participant;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop2.R;
import com.example.workshop2.model.EventQR;
import com.example.workshop2.model.EventQRAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ViewQrCodesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventQRAdapter eventQRAdapter;

    private ImageView  noEventsImageView;


    private List<EventQR> qrEventList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_qr);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        eventQRAdapter = new EventQRAdapter(qrEventList, this);
        recyclerView.setAdapter(eventQRAdapter);

        noEventsImageView = findViewById(R.id.noEventsImageView); // Initialize here

        fetchEventQRs();
    }
    /*private void fetchEventQRs() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get today's date in "YYYY-MM-DD" format
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        db.collection("eventQR")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        qrEventList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            EventQR eventQR = document.toObject(EventQR.class);
                            if (eventQR != null) {
                                String eventId = eventQR.getEventId();

                                // Fetch the event details from the "events" collection
                                db.collection("events")
                                        .document(eventId)
                                        .get()
                                        .addOnCompleteListener(eventTask -> {
                                            if (eventTask.isSuccessful() && eventTask.getResult() != null) {
                                                DocumentSnapshot eventDoc = eventTask.getResult();
                                                String eventDate = eventDoc.getString("date");

                                                // Compare the event date with today's date
                                                if (eventDate != null && isDateOnOrAfter(eventDate, todayDate)) {
                                                    eventQR.setEventDate(eventDate); // Save event date for sorting
                                                    qrEventList.add(eventQR);

                                                    // Sort the list by event date
                                                    sortEventQRList();

                                                    eventQRAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        });
                            }
                        }
                    } else {
                        Toast.makeText(this, "Failed to fetch events", Toast.LENGTH_SHORT).show();
                    }
                });
    }*/

    private void fetchEventQRs() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get today's date in "YYYY-MM-DD" format
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        db.collection("eventQR")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        qrEventList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            EventQR eventQR = document.toObject(EventQR.class);
                            if (eventQR != null) {
                                String eventId = eventQR.getEventId();

                                // Fetch the event details from the "events" collection
                                db.collection("events")
                                        .document(eventId)
                                        .get()
                                        .addOnCompleteListener(eventTask -> {
                                            if (eventTask.isSuccessful() && eventTask.getResult() != null) {
                                                DocumentSnapshot eventDoc = eventTask.getResult();
                                                String eventDate = eventDoc.getString("date");

                                                // Compare the event date with today's date
                                                if (eventDate != null && isDateOnOrAfter(eventDate, todayDate)) {
                                                    eventQR.setEventDate(eventDate); // Save event date for sorting
                                                    qrEventList.add(eventQR);

                                                    // Sort the list by event date
                                                    sortEventQRList();

                                                    eventQRAdapter.notifyDataSetChanged();
                                                }
                                            }

                                            toggleNoEventsImage(); // Check after each event is fetched
                                        });
                            }
                        }

                        toggleNoEventsImage(); // Check if no events at all
                    } else {
                        Toast.makeText(this, "Failed to fetch events", Toast.LENGTH_SHORT).show();
                        toggleNoEventsImage(); // Show "no events" image in case of failure
                    }
                });


    }

    private void toggleNoEventsImage() {
        if (qrEventList.isEmpty()) {
            noEventsImageView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noEventsImageView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    // Helper method to sort the list by date
    private void sortEventQRList() {
        Collections.sort(qrEventList, (event1, event2) -> {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date1 = dateFormat.parse(event1.getEventDate());
                Date date2 = dateFormat.parse(event2.getEventDate());
                return date1.compareTo(date2); // Ascending order
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        });
    }

    // Helper method to check if the event date is on or after today's date
    private boolean isDateOnOrAfter(String eventDate, String todayDate) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date event = dateFormat.parse(eventDate);
            Date today = dateFormat.parse(todayDate);

            // Ensure the event date is on or after today's date
            return event != null && today != null && !event.before(today);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*private void fetchEventQRs() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get today's date in "YYYY-MM-DD" format
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        db.collection("eventQR")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        qrEventList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            EventQR eventQR = document.toObject(EventQR.class);
                            if (eventQR != null) {
                                String eventId = eventQR.getEventId();

                                // Fetch the event details from the "events" collection
                                db.collection("events")
                                        .document(eventId)
                                        .get()
                                        .addOnCompleteListener(eventTask -> {
                                            if (eventTask.isSuccessful() && eventTask.getResult() != null) {
                                                DocumentSnapshot eventDoc = eventTask.getResult();
                                                String eventDate = eventDoc.getString("date");

                                                // Compare the event date with today's date
                                                if (eventDate != null && isDateOnOrAfter(eventDate, todayDate)) {
                                                    qrEventList.add(eventQR);
                                                    eventQRAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        });
                            }
                        }
                    } else {
                        Toast.makeText(this, "Failed to fetch events", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Helper method to check if the event date is on or after today's date
    private boolean isDateOnOrAfter(String eventDate, String todayDate) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date event = dateFormat.parse(eventDate);
            Date today = dateFormat.parse(todayDate);

            // Ensure the event date is on or after today's date
            return event != null && today != null && !event.before(today);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }*/
}
