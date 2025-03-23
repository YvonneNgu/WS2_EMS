package com.example.workshop2.organiser;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.workshop2.R;
import com.example.workshop2.model.Event;
import com.example.workshop2.model.EventAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class FutureEventsFragment extends Fragment {

    private RecyclerView futureRecyclerView;
    private EventAdapter futureAdapter;
    private ArrayList<Event> futureEvents;
    private Set<String> eventIds;
    private ListenerRegistration listenerRegistration;
    private ImageView noEventsImageView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        futureRecyclerView = view.findViewById(R.id.recyclerView);
        noEventsImageView = view.findViewById(R.id.noEventsImageView);
        futureEvents = new ArrayList<>();
        eventIds = new HashSet<>();
        futureAdapter = new EventAdapter(getContext(), futureEvents);
        futureRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        futureRecyclerView.setAdapter(futureAdapter);

        setUpRealtimeUpdates();

        return view;
    }

    private void setUpRealtimeUpdates() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        listenerRegistration = db.collection("events")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "Approved") // Filter for approved events
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        return;
                    }
                    if (querySnapshot != null) {
                        futureEvents.clear();
                        eventIds.clear();
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            Event event = document.toObject(Event.class);
                            if (event != null && !eventIds.contains(event.getEventId())) {
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                    Date eventDateTime = sdf.parse(event.getDate() + " " + event.getTime());
                                    Date currentDateTime = new Date();
                                    if (eventDateTime != null && eventDateTime.after(currentDateTime)) {
                                        futureEvents.add(event);
                                        eventIds.add(event.getEventId());
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                        futureAdapter.notifyDataSetChanged();
                        updateUI();
                    }
                });
    }

    private void updateUI() {
        if (futureEvents.isEmpty()) {
            noEventsImageView.setVisibility(View.VISIBLE);
            futureRecyclerView.setVisibility(View.GONE);
        } else {
            noEventsImageView.setVisibility(View.GONE);
            futureRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void refreshEvents() {
        setUpRealtimeUpdates();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}