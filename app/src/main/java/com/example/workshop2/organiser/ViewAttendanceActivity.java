package com.example.workshop2.organiser;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop2.R;
import com.example.workshop2.model.Attendance;
import com.example.workshop2.model.AttendanceAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ViewAttendanceActivity extends AppCompatActivity {

    private RecyclerView rvAttendanceList;
    private AttendanceAdapter attendanceAdapter;
    private ArrayList<Attendance> attendanceList;
    private FirebaseFirestore db;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        TextView tvEventName = findViewById(R.id.tvEventName);
        EditText etSearch = findViewById(R.id.etSearch);
        Button btnSearch = findViewById(R.id.btnSearch);
        rvAttendanceList = findViewById(R.id.rvAttendanceList);

        // Get event details from intent
        eventId = getIntent().getStringExtra("eventId");
        String eventName = getIntent().getStringExtra("eventName");

        if (eventName != null) {
            tvEventName.setText("Attendance for: " + eventName);
        } else {
            tvEventName.setText("Attendance for: Unknown Event");
        }

        // Initialize RecyclerView
        attendanceList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        attendanceAdapter = new AttendanceAdapter(attendanceList, db, eventId);
        rvAttendanceList.setLayoutManager(new LinearLayoutManager(this));
        rvAttendanceList.setAdapter(attendanceAdapter);

        // Fetch attendance data
        fetchAttendanceData(eventId);

        // Set up search button click listener
        btnSearch.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                searchAttendanceByIdCard(query);
            } else {
                Toast.makeText(this, "Please enter a User ID Card to search.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAttendanceData(String eventId) {
        if (eventId == null || eventId.isEmpty()) {
            Log.e("ViewAttendanceActivity", "Event ID is null or empty.");
            return;
        }

        db.collection("eventQR")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot documents = task.getResult();
                        attendanceList.clear();
                        if (documents.isEmpty()) {
                            Log.d("ViewAttendanceActivity", "No attendance data found for eventID: " + eventId);
                        } else {
                            for (QueryDocumentSnapshot doc : documents) {
                                Attendance attendance = new Attendance(
                                        doc.getString("userName"),
                                        doc.getString("userEmail"),
                                        doc.getString("userPhoneNumber"),
                                        doc.getString("userIdCard"),
                                        doc.getString("status")
                                );
                                attendanceList.add(attendance);
                            }
                            attendanceAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("ViewAttendanceActivity", "Error fetching attendance data: ", task.getException());
                    }
                });
    }

    private void searchAttendanceByIdCard(String userIdCard) {
        if (eventId == null || eventId.isEmpty()) {
            Log.e("ViewAttendanceActivity", "Event ID is null or empty.");
            return;
        }

        db.collection("eventQR")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("userIdCard", userIdCard)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot documents = task.getResult();
                        attendanceList.clear();
                        if (documents.isEmpty()) {
                            Toast.makeText(this, "No participant found.", Toast.LENGTH_SHORT).show();
                        } else {
                            for (QueryDocumentSnapshot doc : documents) {
                                Attendance attendance = new Attendance(
                                        doc.getString("userName"),
                                        doc.getString("userEmail"),
                                        doc.getString("userPhoneNumber"),
                                        doc.getString("userIdCard"),
                                        doc.getString("status")
                                );
                                attendanceList.add(attendance);
                            }
                            attendanceAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("ViewAttendanceActivity", "Error fetching search results: ", task.getException());
                        Toast.makeText(this, "Error fetching data. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
