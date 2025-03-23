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
import com.example.workshop2.model.StatusEventAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class StatusEventsFragment extends Fragment {

    private RecyclerView statusRecyclerView;
    private StatusEventAdapter statusAdapter;
    private ArrayList<Event> statusEvents;
    private ImageView noEventsImageView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        statusRecyclerView = view.findViewById(R.id.recyclerView);
        noEventsImageView = view.findViewById(R.id.noEventsImageView);
        statusEvents = new ArrayList<>();
        statusAdapter = new StatusEventAdapter(getContext(), statusEvents);

        statusRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        statusRecyclerView.setAdapter(statusAdapter);

        loadPendingEvents();

        return view;
    }

    private void loadPendingEvents() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "Pending") // Filter events with status "Pending"
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    statusEvents.clear();
                    queryDocumentSnapshots.getDocuments().forEach(document -> {
                        Event event = document.toObject(Event.class);
                        if (event != null) {
                            statusEvents.add(event);
                        }
                    });
                    statusAdapter.notifyDataSetChanged();
                    updateUI();
                })
                .addOnFailureListener(e -> updateUI());
    }

    private void updateUI() {
        if (statusEvents.isEmpty()) {
            noEventsImageView.setVisibility(View.VISIBLE);
            statusRecyclerView.setVisibility(View.GONE);
        } else {
            noEventsImageView.setVisibility(View.GONE);
            statusRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void refreshEvents() {
        loadPendingEvents();
    }
}
