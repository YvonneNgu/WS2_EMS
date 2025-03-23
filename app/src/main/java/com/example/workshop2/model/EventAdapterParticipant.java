
package com.example.workshop2.model;

import android.app.AlertDialog;
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
import com.example.workshop2.participant.GenerateQR;
import com.example.workshop2.payment.PaymentActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EventAdapterParticipant extends RecyclerView.Adapter<EventAdapterParticipant.EventViewHolderParticipant> {

    private final Context context;
    private final ArrayList<Event> eventList;
    private final String userId;

    public EventAdapterParticipant(Context context, ArrayList<Event> eventList, String userId) {
        this.context = context;
        this.eventList = eventList;
        this.userId = userId;
    }

    @NonNull
    @Override
    public EventViewHolderParticipant onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.card_event, parent, false);
        return new EventViewHolderParticipant(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolderParticipant holder, int position) {
        Event event = eventList.get(position);

        holder.eventNameTextView.setText(event.getName());
        holder.eventDescriptionTextView.setText(event.getDescription());
        holder.eventLocationTextView.setText(event.getLocation());
        holder.eventDateTextView.setText(event.getDate());
        holder.eventPriceTextView.setText("Price: " + (event.getEntryPrice() == 0 ? "Free" : "RM " + event.getEntryPrice()));
        holder.eventCategoryTextView.setText("Category: " + event.getCategory());
        holder.eventCapacityTextView.setText("Capacity: " + event.getCapacity());
        holder.eventTimeTextView.setText(event.getTime());

        holder.joinButton.setOnClickListener(v -> checkIfUserJoinedEvent(event));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolderParticipant extends RecyclerView.ViewHolder {
        public TextView eventNameTextView, eventDescriptionTextView, eventLocationTextView,
                eventDateTextView, eventPriceTextView, eventCategoryTextView, eventCapacityTextView, eventTimeTextView;
        public Button joinButton;

        public EventViewHolderParticipant(View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            eventDescriptionTextView = itemView.findViewById(R.id.eventDescriptionTextView);
            eventLocationTextView = itemView.findViewById(R.id.eventLocationTextView);
            eventDateTextView = itemView.findViewById(R.id.eventDateTextView);
            eventPriceTextView = itemView.findViewById(R.id.eventPriceTextView);
            eventCategoryTextView = itemView.findViewById(R.id.eventCategoryTextView);
            eventCapacityTextView = itemView.findViewById(R.id.eventCapacityTextView);
            joinButton = itemView.findViewById(R.id.joinButton);
            eventTimeTextView = itemView.findViewById(R.id.eventTimeTextView);
        }
    }

    private void checkIfUserJoinedEvent(Event event) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("eventQR")
                .whereEqualTo("eventId", event.getEventId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            int currentParticipants = task.getResult().size();

                            // Check if capacity is exceeded
                            if (currentParticipants >= event.getCapacity()) {
                                Toast.makeText(context, "Event is full. You cannot join this event.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Check if user has already joined the event
                            boolean userAlreadyJoined = false;
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                if (doc.getString("userId").equals(userId)) {
                                    userAlreadyJoined = true;
                                    break;
                                }
                            }

                            if (userAlreadyJoined) {
                                Toast.makeText(context, "You have already joined this event.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Check if the event is free or paid
                                if (event.getEntryPrice() == 0) {
                                    navigateToGenerateQR(event);
                                } else {
                                    handlePaidEvent(event);
                                }
                            }
                        }
                    } else {
                        Toast.makeText(context, "Error checking event participation.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void handlePaidEvent(Event event) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("card_details")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String cardId = querySnapshot.getDocuments().get(0).getId(); // Get the card ID
                        double walletBalance = querySnapshot.getDocuments().get(0).getDouble("wallet"); // Use wallet from card_details

                        if (walletBalance >= event.getEntryPrice()) {
                            showPaymentConfirmationDialog(event, walletBalance, cardId);
                        } else {
                            showTopUpDialog(event.getEntryPrice());
                        }
                    } else {
                        Toast.makeText(context, "No wallet data found for the user.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to fetch wallet data.", Toast.LENGTH_SHORT).show();
                });
    }

    private void showPaymentConfirmationDialog(Event event, double walletBalance, String cardId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Payment")
                .setMessage("Event Price: RM " + event.getEntryPrice() + "\nCurrent Wallet Balance: RM " + walletBalance)
                .setPositiveButton("Confirm", (dialog, which) -> deductWalletBalance(event, cardId, walletBalance))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deductWalletBalance(Event event, String cardId, double walletBalance) {
        double newWalletBalance = walletBalance - event.getEntryPrice();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("card_details")
                .document(cardId)
                .update("wallet", newWalletBalance) // Deduct from wallet in card_details
                .addOnSuccessListener(aVoid -> {
                    savePaymentDetails(event, cardId); // Save payment details directly
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update wallet balance. Try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void savePaymentDetails(Event event, String cardId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> paymentDetails = new HashMap<>();
        paymentDetails.put("amount", event.getEntryPrice());
        paymentDetails.put("PaymentStatus", "Completed");
        paymentDetails.put("userId", userId);
        paymentDetails.put("cardId", cardId);
        paymentDetails.put("eventId", event.getEventId());
        paymentDetails.put("eventName", event.getName());

        db.collection("Payment")
                .add(paymentDetails)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, "Payment successful. Proceeding to generate QR.", Toast.LENGTH_SHORT).show();
                    navigateToGenerateQR(event); // Navigate to QR generation after saving payment details
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to save payment. Try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToGenerateQR(Event event) {
        Intent intent = new Intent(context, GenerateQR.class);
        intent.putExtra("eventName", event.getName());
        intent.putExtra("eventId", event.getEventId());
        intent.putExtra("userId", userId);
        context.startActivity(intent);
    }

    private void showTopUpDialog(double entryPrice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Insufficient Wallet Balance")
                .setMessage("Your wallet balance is insufficient to pay RM " + entryPrice + ". Would you like to top up?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(context, PaymentActivity.class);
                    intent.putExtra("userId", userId);
                    context.startActivity(intent);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}

