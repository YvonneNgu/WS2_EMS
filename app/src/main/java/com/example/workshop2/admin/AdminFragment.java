package com.example.workshop2.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.workshop2.MainActivity;
import com.example.workshop2.R;
import com.example.workshop2.admin.ReactivatedUserActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AdminFragment extends Fragment {

    private TextView adminGreetingText;
    private Button approveEventButton, viewUsersButton, viewAnalyticsButton, viewReactivatedButton;
    private FloatingActionButton btnLogout;
    private FirebaseAuth mAuth;

    // A list to hold event data
    private ArrayList<String> eventDetailsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        adminGreetingText = view.findViewById(R.id.tvGreetingAdmin);
        approveEventButton = view.findViewById(R.id.approveEventButton);
        btnLogout = view.findViewById(R.id.btnLogout);
        viewUsersButton = view.findViewById(R.id.viewUsersButton);
        viewAnalyticsButton = view.findViewById(R.id.viewAnalyticsButton);
        viewReactivatedButton = view.findViewById(R.id.viewReactivatedButton); // New Button

        // Set greeting message
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String adminName = (currentUser != null && currentUser.getDisplayName() != null)
                ? currentUser.getDisplayName()
                : "Admin";
        adminGreetingText.setText("Welcome, " + adminName);

        // Fetch event data from Firestore and populate the eventDetailsList
        fetchEventData();

        // Set click listener for View Analytics button
        viewAnalyticsButton.setOnClickListener(v -> {
            // Pass event data to AdminAnalyticsActivity
            Intent intent = new Intent(requireContext(), AdminAnalyticsActivity.class);
            intent.putStringArrayListExtra("eventDetails", eventDetailsList); // Pass all event data
            startActivity(intent);
        });

        // Set click listener for Approve Event button
        approveEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EventApproveActivity.class);
            startActivity(intent);
        });

        // Set click listener for View Users button
        viewUsersButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ViewUserActivity.class);
            startActivity(intent);
        });

        // Set click listener for Reactivate User button
        viewReactivatedButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ReactivatedUserActivity.class);
            startActivity(intent);
        });

        // Set click listener for Logout button
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();
        });

        return view;
    }

    private void fetchEventData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch all events data
        db.collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        // Loop through each event and add relevant details to the list
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String eventDetails = "Event Name: " + document.getString("name") +
                                    "\nCapacity: " + document.getLong("capacity") +
                                    "\nStatus: " + document.getString("status") +
                                    "\nDate: " + document.getString("date");

                            // Add the event details to the list
                            eventDetailsList.add(eventDetails);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error fetching event data", Toast.LENGTH_SHORT).show());
    }
}
