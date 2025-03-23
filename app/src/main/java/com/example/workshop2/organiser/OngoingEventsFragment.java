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

public class OngoingEventsFragment extends Fragment {

    private RecyclerView ongoingRecyclerView;
    private EventAdapter ongoingAdapter;
    private ArrayList<Event> ongoingEvents;
    private Set<String> eventIds;
    private ListenerRegistration listenerRegistration;
    private ImageView noEventsImageView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        ongoingRecyclerView = view.findViewById(R.id.recyclerView);
        noEventsImageView = view.findViewById(R.id.noEventsImageView);
        ongoingEvents = new ArrayList<>();
        eventIds = new HashSet<>();
        ongoingAdapter = new EventAdapter(getContext(), ongoingEvents);
        ongoingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ongoingRecyclerView.setAdapter(ongoingAdapter);

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
                        ongoingEvents.clear();
                        eventIds.clear();
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            Event event = document.toObject(Event.class);
                            if (event != null && !eventIds.contains(event.getEventId())) {
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                    Date eventDateTime = sdf.parse(event.getDate() + " " + event.getTime());
                                    Date currentDateTime = new Date();
                                    SimpleDateFormat dateOnlySdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                                    if (dateOnlySdf.format(eventDateTime).equals(dateOnlySdf.format(currentDateTime)) &&
                                            (currentDateTime.equals(eventDateTime) || currentDateTime.after(eventDateTime))) {
                                        ongoingEvents.add(event);
                                        eventIds.add(event.getEventId());
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                        ongoingAdapter.notifyDataSetChanged();
                        updateUI();
                    }
                });
    }

    private void updateUI() {
        if (ongoingEvents.isEmpty()) {
            noEventsImageView.setVisibility(View.VISIBLE);
            ongoingRecyclerView.setVisibility(View.GONE);
        } else {
            noEventsImageView.setVisibility(View.GONE);
            ongoingRecyclerView.setVisibility(View.VISIBLE);
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