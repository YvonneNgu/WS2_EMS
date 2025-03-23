package com.example.workshop2.participant;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardFragment extends Fragment {

    private RecyclerView leaderboardRecyclerView;
    private LeaderboardAdapter leaderboardAdapter;
    private List<Participant> participantList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        leaderboardRecyclerView = view.findViewById(R.id.leaderboardRecyclerView);
        participantList = new ArrayList<>();

        String currentUserId = FirebaseAuth.getInstance().getUid(); // Get the current user ID
        leaderboardAdapter = new LeaderboardAdapter(participantList, currentUserId);

        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        leaderboardRecyclerView.setAdapter(leaderboardAdapter);

        fetchEventQRData();

        return view;
    }

    private void fetchEventQRData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("eventQR")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, Integer> eventCounter = new HashMap<>();
                    Map<String, Integer> certCounter = new HashMap<>();
                    Map<String, String> userIdToName = new HashMap<>();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String userId = document.getString("userId");
                        String userName = document.getString("userName");
                        String status = document.getString("status");

                        // Count joined events
                        eventCounter.put(userId, eventCounter.getOrDefault(userId, 0) + 1);

                        // Count certificates for "present" status
                        if ("present".equalsIgnoreCase(status)) {
                            certCounter.put(userId, certCounter.getOrDefault(userId, 0) + 1);
                        }

                        // Map userId to userName
                        userIdToName.putIfAbsent(userId, userName);
                    }

                    // Fetch profile images from the "users" collection
                    fetchProfileImages(db, eventCounter, certCounter, userIdToName);
                })
                .addOnFailureListener(e -> {
                    Log.e("Leaderboard", "Failed to fetch event data: " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to fetch leaderboard data.", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchProfileImages(FirebaseFirestore db, Map<String, Integer> eventCounter, Map<String, Integer> certCounter, Map<String, String> userIdToName) {
        db.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, String> userIdToProfilePicUrl = new HashMap<>();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String userId = document.getString("userId");
                        String profilePicUrl = document.getString("profileImageUrl");

                        if (userId != null && profilePicUrl != null && !profilePicUrl.isEmpty()) {
                            userIdToProfilePicUrl.put(userId, profilePicUrl);
                        }
                    }

                    // Calculate points and display the leaderboard
                    calculatePointsAndDisplayLeaderboard(eventCounter, certCounter, userIdToName, userIdToProfilePicUrl);
                })
                .addOnFailureListener(e -> {
                    Log.e("Leaderboard", "Failed to fetch user profile images: " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to fetch user profile images.", Toast.LENGTH_SHORT).show();
                });
    }

    private void calculatePointsAndDisplayLeaderboard(Map<String, Integer> eventCounter, Map<String, Integer> certCounter, Map<String, String> userIdToName, Map<String, String> userIdToProfilePicUrl) {
        List<Participant> participantList = new ArrayList<>();

        for (String userId : eventCounter.keySet()) {
            int attendedEvents = eventCounter.getOrDefault(userId, 0);
            int certs = certCounter.getOrDefault(userId, 0);
            String userName = userIdToName.get(userId);
            String profilePicUrl = userIdToProfilePicUrl.getOrDefault(userId, null);

            // Calculate points (e.g., 2 points per attended event, 5 points per certificate)
            int points = (attendedEvents * 2) + (certs * 5);

            participantList.add(new Participant(userId, userName, attendedEvents, certs, points, profilePicUrl));
        }

        // Sort participants by points in descending order
        Collections.sort(participantList, (p1, p2) -> Integer.compare(p2.getPoints(), p1.getPoints()));

        leaderboardAdapter.updateData(participantList);
    }

    public static class Participant {
        private final String userId;
        private final String name;
        private final int eventsJoined;
        private final int certs;
        private final int points;
        private final String profilePicUrl;

        public Participant(String userId, String name, int eventsJoined, int certs, int points, String profilePicUrl) {
            this.userId = userId;
            this.name = name;
            this.eventsJoined = eventsJoined;
            this.certs = certs;
            this.points = points;
            this.profilePicUrl = profilePicUrl;
        }

        public String getUserId() {
            return userId;
        }

        public String getName() {
            return name;
        }

        public int getEventsJoined() {
            return eventsJoined;
        }

        public int getCerts() {
            return certs;
        }

        public int getPoints() {
            return points;
        }

        public String getProfilePicUrl() {
            return profilePicUrl;
        }
    }
}
