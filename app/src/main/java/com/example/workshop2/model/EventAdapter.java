package com.example.workshop2.model;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop2.R;
import com.example.workshop2.cert.CertMgmtActivity;
import com.example.workshop2.organiser.EditEventActivity;
import com.example.workshop2.organiser.ViewAttendanceActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private Context context;
    private ArrayList<Event> eventList;

    public EventAdapter(Context context, ArrayList<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.eventNameTextView.setText(event.getName());
        holder.eventDescriptionTextView.setText(event.getDescription());
        holder.eventLocationTextView.setText(event.getLocation());
        holder.eventDateTextView.setText(event.getDate());
        holder.eventTimeTextView.setText(event.getTime());
        holder.eventPriceTextView.setText("Price: " + event.getEntryPrice());
        holder.eventCategoryTextView.setText("Category: " + event.getCategory());
        holder.eventCapacityTextView.setText("Capacity: " + event.getCapacity());

        // Set up edit button click listener
        holder.editButton.setOnClickListener(v -> {
            // Ensure eventId is passed correctly
            String eventId = event.getEventId();
            if (eventId != null && !eventId.isEmpty()) {
                Intent intent = new Intent(context, EditEventActivity.class);
                intent.putExtra("eventId", eventId); // Pass the event ID to the edit activity
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Event ID is missing", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up delete button click listener
        holder.deleteButton.setOnClickListener(v -> {
            deleteEvent(event.getEventId(), position);
        });

        // Set up generate cert button click listener
        holder.generateCertButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, CertMgmtActivity.class);
            intent.putExtra("eventId", event.getEventId());
            intent.putExtra("eventName", event.getName());
            context.startActivity(intent);
        });

        // button view
        holder.viewAttendanceButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewAttendanceActivity.class);
            intent.putExtra("eventId", event.getEventId());
            intent.putExtra("eventName", event.getName());
            context.startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    // Method to delete an event from Firestore and update the RecyclerView
    private void deleteEvent(String eventId, int position) {
        // Check if the position is valid before proceeding
        if (position < 0 || position >= eventList.size()) {
            Log.e("EventAdapter", "Invalid position: " + position + ", eventList size: " + eventList.size());
            Toast.makeText(context, "Unable to delete event. Invalid position.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create an AlertDialog for confirmation
        new android.app.AlertDialog.Builder(context)
                .setMessage("Are you sure you want to delete this event?")
                .setCancelable(false) // Prevent dialog from being dismissed by tapping outside
                .setPositiveButton("Yes", (dialog, id) -> {
                    // User confirmed, proceed with deleting the event
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("events").document(eventId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                // Ensure the event is removed from the list
                                if (position >= 0 && position < eventList.size()) {
                                    eventList.remove(position); // Remove the item from the list
                                    notifyItemRemoved(position); // Notify RecyclerView that the item is removed
                                    notifyItemRangeChanged(position, eventList.size()); // Refresh the remaining items
                                    Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e("EventAdapter", "Position out of bounds after deletion: " + position);
                                }
                            })
                            .addOnFailureListener(e -> {
                                // Handle failure and log the error
                                Toast.makeText(context, "Failed to delete event", Toast.LENGTH_SHORT).show();
                                Log.e("EventAdapter", "Error deleting event: ", e); // Log the error for debugging
                            });
                })
                .setNegativeButton("No", (dialog, id) -> {
                    // User canceled, close the dialog
                    dialog.dismiss();
                })
                .create()
                .show(); // Show the dialog
    }



    public static class EventViewHolder extends RecyclerView.ViewHolder {
        public TextView eventNameTextView, eventDescriptionTextView, eventLocationTextView, eventDateTextView, eventPriceTextView, eventCategoryTextView, eventCapacityTextView,eventTimeTextView;
        public Button editButton, deleteButton, generateCertButton, viewAttendanceButton;

        public EventViewHolder(View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            eventDescriptionTextView = itemView.findViewById(R.id.eventDescriptionTextView);
            eventLocationTextView = itemView.findViewById(R.id.eventLocationTextView);
            eventDateTextView = itemView.findViewById(R.id.eventDateTextView);
            eventPriceTextView = itemView.findViewById(R.id.eventPriceTextView);
            eventTimeTextView = itemView.findViewById(R.id.eventTimeTextView);
            eventCategoryTextView = itemView.findViewById(R.id.eventCategoryTextView);
            eventCapacityTextView = itemView.findViewById(R.id.eventCapacityTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            generateCertButton = itemView.findViewById(R.id.btnGenerateCert);
            viewAttendanceButton = itemView.findViewById(R.id.btnViewAttendance);
        }
    }
}
