package com.example.workshop2.participant;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.workshop2.R;
import com.example.workshop2.cert.ViewCertListActivity;
import com.example.workshop2.model.Event;
import com.example.workshop2.model.EventAdapterParticipant;
import com.example.workshop2.payment.PaymentActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeParticipantFragment extends Fragment {

    private ImageView profilePicImageView, iv_view_cert;
    private TextView greetingText;
    private RecyclerView popularEventsRecyclerView;
    private EventAdapterParticipant eventAdapterParticipant;
    private ArrayList<Event> eventList;
    private FloatingActionButton viewWalletButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private String userId;

    public HomeParticipantFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home_participant, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Get the current user from Firebase Authentication
        userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        // Initialize UI components
        profilePicImageView = rootView.findViewById(R.id.ivProfile);
        greetingText = rootView.findViewById(R.id.tvGreeting);
        popularEventsRecyclerView = rootView.findViewById(R.id.rvEvents);
        viewWalletButton = rootView.findViewById(R.id.viewWalletButton);
        iv_view_cert = rootView.findViewById(R.id.iv_view_cert);


        // Set up RecyclerView for events slider
        eventList = new ArrayList<>();
        eventAdapterParticipant = new EventAdapterParticipant(getContext(), eventList, userId);
        popularEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        popularEventsRecyclerView.setAdapter(eventAdapterParticipant);

        // Fetch user data and popular events
        fetchUserData();
        fetchPopularEvents();

        // Set up Wallet button click
        viewWalletButton.setOnClickListener(v -> {
            if (userId != null) {
                Intent intent = new Intent(requireContext(), PaymentActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "Please log in to access Wallet.", Toast.LENGTH_SHORT).show();
            }
        });

        // for participant: view certificate list
        iv_view_cert.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ViewCertListActivity.class);
            startActivity(intent);
        });

        return rootView;
    }

    private void fetchUserData() {
        if (userId == null) {
            Toast.makeText(getActivity(), "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference userDocRef = firestore.collection("users").document(userId);
        userDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    String fullName = documentSnapshot.getString("fullName");
                    String profilePicUrl = documentSnapshot.getString("profileImageUrl");

                    greetingText.setText("Hi, " + fullName);

                    // Load profile picture if available
                    if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                        Glide.with(this)
                                .load(profilePicUrl)
                                .circleCrop() // Makes the image circular
                                .into(profilePicImageView);
                    } else {
                        // Set a default profile picture if none exists
                        Glide.with(this)
                                .load(R.drawable.profile_user)
                                .circleCrop()
                                .into(profilePicImageView);
                    }
                } else {
                    Toast.makeText(getActivity(), "User data not found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPopularEvents() {
        if (userId == null) {
            Toast.makeText(getActivity(), "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current date and time
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDateString = dateFormat.format(currentDate);

        // Fetch all upcoming events (future events only)
        firestore.collection("events")
                .whereGreaterThan("date", currentDateString) // Strictly greater to exclude ongoing and past events
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot querySnapshot = task.getResult();
                        eventList.clear();

                        if (querySnapshot.isEmpty()) {
                            Toast.makeText(getActivity(), "No upcoming events found.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Prepare a map to store popularity (number of participants) for each event
                        Map<Event, Integer> eventPopularity = new HashMap<>();
                        List<String> eventIds = new ArrayList<>();

                        // Populate the map and list of event IDs
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Event event = doc.toObject(Event.class);
                            if (event != null) {
                                eventIds.add(doc.getId());
                                eventPopularity.put(event, 0); // Initialize popularity count as 0
                            }
                        }

                        // Query eventQR to count participants for each event
                        firestore.collection("eventQR")
                                .whereIn("eventId", eventIds) // Retrieve all eventQR documents for the relevant events
                                .get()
                                .addOnCompleteListener(eventQRTask -> {
                                    if (eventQRTask.isSuccessful() && eventQRTask.getResult() != null) {
                                        QuerySnapshot qrSnapshot = eventQRTask.getResult();

                                        // Count participants for each eventId
                                        for (DocumentSnapshot doc : qrSnapshot.getDocuments()) {
                                            String eventId = doc.getString("eventId");
                                            for (Event event : eventPopularity.keySet()) {
                                                if (eventId != null && eventId.equals(event.getEventId())) {
                                                    eventPopularity.put(event, eventPopularity.get(event) + 1);
                                                }
                                            }
                                        }

                                        // Sort events by the number of participants (popularity) in descending order
                                        List<Map.Entry<Event, Integer>> sortedEvents = new ArrayList<>(eventPopularity.entrySet());
                                        sortedEvents.sort((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()));

                                        // Clear and add the top 5 events to the eventList
                                        eventList.clear();
                                        for (int i = 0; i < Math.min(5, sortedEvents.size()); i++) {
                                            eventList.add(sortedEvents.get(i).getKey());
                                        }

                                        // Notify the adapter about changes
                                        eventAdapterParticipant.notifyDataSetChanged();
                                    } else {
                                        Toast.makeText(getActivity(), "Failed to fetch event popularity.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(getActivity(), "Failed to fetch events.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
