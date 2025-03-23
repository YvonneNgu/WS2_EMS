package com.example.workshop2.participant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop2.R;
import com.example.workshop2.model.Event;
import com.example.workshop2.model.EventAdapterParticipant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class EventParticipantFragment extends Fragment {

    private RecyclerView eventsRecyclerView;
    private EventAdapterParticipant eventAdapterParticipant;
    private ArrayList<Event> eventList;
    private ArrayList<Event> allEventsList; // Store all events for later searching
    private Spinner categorySpinner;
    private EditText searchEventNameEditText;
    private Button searchButton;
    private ImageView noEventsIcon;

    private String selectedCategory = ""; // To hold the selected category
    private String userId; // Current logged-in user's ID

    public EventParticipantFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_participant, container, false);

        // Initialize FirebaseAuth to get current user ID
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize UI components
        eventsRecyclerView = view.findViewById(R.id.eventsRecyclerView);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        searchEventNameEditText = view.findViewById(R.id.searchEventNameEditText);
        searchButton = view.findViewById(R.id.searchButton);
        noEventsIcon = view.findViewById(R.id.noEventsIcon);

        // Initialize event list and adapter
        eventList = new ArrayList<>();
        allEventsList = new ArrayList<>();
        eventAdapterParticipant = new EventAdapterParticipant(getContext(), eventList, userId); // Pass userId to adapter
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventsRecyclerView.setAdapter(eventAdapterParticipant);

        // Populate category spinner with values from strings.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(), R.array.event_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Set listener for category spinner selection
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedCategory = parentView.getItemAtPosition(position).toString();
                filterEvents();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                selectedCategory = "";
                filterEvents();
            }
        });

        // Set up the search button listener to filter events by name
        searchButton.setOnClickListener(v -> filterEventsByName());

        // Fetch all events from Firestore when the fragment is created
        fetchAllEvents();

        return view;
    }

    private void fetchAllEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events")
                .whereEqualTo("status", "Approved") // Only fetch events with "Approved" status
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            allEventsList.clear();
                            querySnapshot.getDocuments().forEach(doc -> {
                                Event event = doc.toObject(Event.class);
                                if (event != null && isUpcomingEvent(event.getDate())) {
                                    allEventsList.add(event);
                                }
                            });
                            filterEvents(); // Filter events based on selected category after fetching all events
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to fetch events.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isUpcomingEvent(String eventDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date eventDateObj = format.parse(eventDate);
            Date currentDate = Calendar.getInstance().getTime();
            return eventDateObj != null && eventDateObj.after(currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void filterEvents() {
        // Filter events based on selected category
        List<Event> filteredEvents = new ArrayList<>(allEventsList);

        if (!selectedCategory.isEmpty() && !selectedCategory.equals("All Categories")) {
            filteredEvents = filteredEvents.stream()
                    .filter(event -> event.getCategory().equals(selectedCategory))
                    .collect(Collectors.toList());
        }

        eventList.clear();
        eventList.addAll(filteredEvents);
        eventAdapterParticipant.notifyDataSetChanged();

    }

    private void filterEventsByName() {
        String searchEventName = searchEventNameEditText.getText().toString().trim();

        if (searchEventName.isEmpty()) {
            // If search box is empty, show all events (after applying the category filter)
            filterEvents();
        } else {
            // Filter events based on name
            List<Event> filteredEventsByName = new ArrayList<>(eventList);

            filteredEventsByName = filteredEventsByName.stream()
                    .filter(event -> event.getName().toLowerCase().contains(searchEventName.toLowerCase()))  // Use getName() method
                    .collect(Collectors.toList());

            // Update the event list with filtered events by name
            eventList.clear();
            eventList.addAll(filteredEventsByName);
            eventAdapterParticipant.notifyDataSetChanged();

            // Show or hide the no events icon
            if (eventList.isEmpty()) {
                noEventsIcon.setVisibility(View.VISIBLE);
            } else {
                noEventsIcon.setVisibility(View.GONE);
            }
        }
    }
}