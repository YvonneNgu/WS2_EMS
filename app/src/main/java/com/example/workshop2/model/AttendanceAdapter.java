package com.example.workshop2.model;

import android.app.AlertDialog;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private ArrayList<Attendance> attendanceList;
    private FirebaseFirestore db;
    private String eventId; // Added eventId to the adapter

    public AttendanceAdapter(ArrayList<Attendance> attendanceList, FirebaseFirestore db, String eventId) {
        this.attendanceList = attendanceList;
        this.db = db;
        this.eventId = eventId; // Initialize eventId here
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        Attendance attendance = attendanceList.get(position);

        holder.tvNumber.setText(String.valueOf(position + 1));
        holder.tvUserName.setText(attendance.getUserName());
        holder.tvUserEmail.setText(attendance.getUserEmail());
        holder.tvUserPhone.setText(attendance.getUserPhoneNumber());
        holder.tvUserIdCard.setText(attendance.getUserIdCard());
        holder.tvUserStatus.setText(attendance.getStatus());

        // Set color based on status
        if ("Absent".equalsIgnoreCase(attendance.getStatus())) {
            holder.tvUserStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
        } else {
            holder.tvUserStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
        }

        // Button click to show confirmation dialog for attendance status change
        holder.btnMarkAttendance.setOnClickListener(v -> showConfirmationDialog(attendance, holder));
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    private void showConfirmationDialog(Attendance attendance, AttendanceViewHolder holder) {
        // Create the dialog
        new AlertDialog.Builder(holder.itemView.getContext())
                .setTitle("Change Status")
                .setMessage("Are you sure you want to change the status for " + attendance.getUserName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Update the attendance status
                    updateAttendanceStatus(attendance, holder);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void updateAttendanceStatus(Attendance attendance, AttendanceViewHolder holder) {
        // Use the passed eventId here
        String userId = attendance.getUserEmail(); // Assuming userEmail is the unique identifier for userId

        db.collection("eventQR")
                .whereEqualTo("eventId", eventId)  // Query by eventId passed from the previous page
                .whereEqualTo("userEmail", userId) // Query by userEmail (as userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Get the current status from the document, or set to "Absent" if it doesn't exist
                            String currentStatus = document.contains("status") ? document.getString("status") : "Absent";

                            // Log the current status for debugging purposes
                            Log.d("AttendanceAdapter", "Current status: " + currentStatus);

                            // Toggle the status: if "Present", change to "Absent" and vice versa
                            String newStatus = "Present".equalsIgnoreCase(currentStatus) ? "Absent" : "Present";

                            // Get the document reference for updating
                            DocumentReference docRef = document.getReference();

                            // Update the status in Firestore
                            if (docRef != null) {
                                Log.d("AttendanceAdapter", "Updating status for user: " + attendance.getUserName() + " to " + newStatus);
                                docRef.update("status", newStatus)
                                        .addOnSuccessListener(aVoid -> {
                                            // Update the status in the local list
                                            attendance.setStatus(newStatus); // Update the local attendance object
                                            attendanceList.set(attendanceList.indexOf(attendance), attendance); // Update the list at the current index
                                            // Notify the adapter about the status change for this item
                                            notifyItemChanged(attendanceList.indexOf(attendance)); // Update the specific item in RecyclerView
                                            // Toast for success
                                            Toast.makeText(holder.itemView.getContext(), "Status updated successfully!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            // Handle failure
                                            Log.e("AttendanceAdapter", "Error updating status: ", e);
                                            Toast.makeText(holder.itemView.getContext(), "Failed to update status.", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }
                    } else {
                        Log.e("AttendanceAdapter", "Document not found for eventId: " + eventId + " and userEmail: " + userId);
                        Toast.makeText(holder.itemView.getContext(), "Document not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AttendanceAdapter", "Error fetching document: ", e);
                    Toast.makeText(holder.itemView.getContext(), "Error fetching status.", Toast.LENGTH_SHORT).show();
                });
    }

    public static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber, tvUserName, tvUserEmail, tvUserPhone, tvUserIdCard, tvUserStatus;
        Button btnMarkAttendance;

        public AttendanceViewHolder(View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tvNumber);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserPhone = itemView.findViewById(R.id.tvUserPhone);
            tvUserIdCard = itemView.findViewById(R.id.tvUserIdCard);
            tvUserStatus = itemView.findViewById(R.id.tvUserStatus);
            btnMarkAttendance = itemView.findViewById(R.id.btnMarkAttendance);
        }
    }
}
