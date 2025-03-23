package com.example.workshop2.admin;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop2.R;
import com.example.workshop2.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ReactivateUserAdapter extends RecyclerView.Adapter<ReactivateUserAdapter.UserViewHolder> {

    private ArrayList<User> deactivatedUsers;
    private Runnable refreshCallback;

    public ReactivateUserAdapter(ArrayList<User> deactivatedUsers, Runnable refreshCallback) {
        this.deactivatedUsers = deactivatedUsers;
        this.refreshCallback = refreshCallback;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deactivate_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = deactivatedUsers.get(position);

        holder.fullNameTextView.setText(user.getFullName());
        holder.emailTextView.setText("Email: " + user.getEmail());
        holder.phoneNumberTextView.setText("Phone: " + user.getPhoneNumber());

        holder.reactivateButton.setOnClickListener(v -> {
            showApprovalDialog(v.getContext(), user);
        });

        FirebaseFirestore.getInstance()
                .collection("reactivationRequests")
                .document(user.getUserId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        holder.reactivateButton.setText("Approve Request");
                        holder.reactivateButton.setEnabled(true);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return deactivatedUsers.size();
    }

    private void showApprovalDialog(Context context, User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Approve Reactivation Request");
        builder.setMessage("Are you sure you want to approve " + user.getFullName() + "'s reactivation request?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUserId())
                    .update("userStatus", "active")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Reactivation request approved!", Toast.LENGTH_SHORT).show();

                        // Trigger refresh callback after approval
                        if (refreshCallback != null) {
                            refreshCallback.run();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error approving request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView fullNameTextView, emailTextView, phoneNumberTextView;
        Button reactivateButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            fullNameTextView = itemView.findViewById(R.id.textViewFullName);
            emailTextView = itemView.findViewById(R.id.textViewEmail);
            phoneNumberTextView = itemView.findViewById(R.id.textViewPhoneNumber);
            reactivateButton = itemView.findViewById(R.id.buttonReactivate);
        }
    }
}
