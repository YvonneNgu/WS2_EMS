package com.example.workshop2.model;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop2.R;
import com.example.workshop2.participant.DisplayQRActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class EventQRAdapter extends RecyclerView.Adapter<EventQRAdapter.ViewHolder> {

    private List<EventQR> eventQRList;
    private Context context;

    public EventQRAdapter(List<EventQR> eventQRList, Context context) {
        this.eventQRList = eventQRList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.qr_code_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EventQR event = eventQRList.get(position);

        // Set event name
        holder.eventNameTextView.setText("Event: " + event.getEventName());

        // Avoid repeated Firestore calls; preload data if possible
        DocumentReference eventRef = FirebaseFirestore.getInstance().collection("events").document(event.getEventId());
        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                final String eventDate = documentSnapshot.getString("date");
                holder.eventDateTextView.setText("Date: " + (eventDate != null ? eventDate : "N/A"));

                holder.displayQRButton.setOnClickListener(v -> {
                    Intent intent = new Intent(context, DisplayQRActivity.class);
                    intent.putExtra("eventId", event.getEventId());
                    intent.putExtra("eventName", event.getEventName());
                    intent.putExtra("eventDate", eventDate);
                    intent.putExtra("userName", event.getUserName());
                    intent.putExtra("userDOB", event.getUserDOB());
                    intent.putExtra("userEmail", event.getUserEmail());
                    intent.putExtra("userPhoneNumber", event.getUserPhoneNumber());
                    intent.putExtra("userIdCard", event.getUserIdCard());
                    intent.putExtra("status", event.getStatus());
                    context.startActivity(intent);
                });
            } else {
                holder.eventDateTextView.setText("Date: N/A");
            }
        }).addOnFailureListener(e -> {
            holder.eventDateTextView.setText("Date: Error");
        });
    }

    @Override
    public int getItemCount() {
        return eventQRList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView eventNameTextView;
        private TextView eventDateTextView;
        private Button displayQRButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            eventDateTextView = itemView.findViewById(R.id.eventDateTextView); // Initialize event date TextView
            displayQRButton = itemView.findViewById(R.id.displayQRButton);
        }
    }
}