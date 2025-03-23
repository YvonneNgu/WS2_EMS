package com.example.workshop2.participant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.workshop2.R;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final List<LeaderboardFragment.Participant> participants;
    private final String currentUserId;

    public LeaderboardAdapter(List<LeaderboardFragment.Participant> participants, String currentUserId) {
        this.participants = participants;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardFragment.Participant participant = participants.get(position);

        holder.rankTextView.setText(String.valueOf(position + 1));
        holder.nameTextView.setText(participant.getName());
        holder.pointsTextView.setText(participant.getPoints() + " pts");
        holder.eventCountTextView.setText("Events: " + participant.getEventsJoined());
        holder.certCountTextView.setText("Certs: " + participant.getCerts());

        // Load profile picture if available
        if (participant.getProfilePicUrl() != null && !participant.getProfilePicUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(participant.getProfilePicUrl())
                    .circleCrop() // Makes the image circular
                    .into(holder.profilePicImageView);
        } else {
            // Set a default profile picture if none exists
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.profile_user)
                    .circleCrop()
                    .into(holder.profilePicImageView);
        }

        // Highlight top 3 ranks
        if (position == 0) {
            // Gold for 1st place
            holder.rankTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.gold));
            holder.nameTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.gold));
        } else if (position == 1) {
            // Silver for 2nd place
            holder.rankTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.silver));
            holder.nameTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.silver));
        } else if (position == 2) {
            // Bronze for 3rd place
            holder.rankTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.bronze));
            holder.nameTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.bronze));
        } else {
            // Default color for other ranks
            holder.rankTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
            holder.nameTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
        }

        // Highlight current user with light gray border
        if (participant.getUserId().equals(currentUserId)) {
            holder.itemView.setBackgroundResource(R.drawable.card_background_highlight);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.card_background_default);
        }
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePicImageView;
        TextView rankTextView, nameTextView, pointsTextView, eventCountTextView, certCountTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicImageView = itemView.findViewById(R.id.profilePicImageView);
            rankTextView = itemView.findViewById(R.id.rankTextView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            pointsTextView = itemView.findViewById(R.id.pointsTextView);
            eventCountTextView = itemView.findViewById(R.id.eventCountTextView);
            certCountTextView = itemView.findViewById(R.id.certCountTextView);
        }
    }

    public void updateData(List<LeaderboardFragment.Participant> newParticipants) {
        this.participants.clear(); // Clear the old data
        this.participants.addAll(newParticipants); // Add the new data
        notifyDataSetChanged(); // Notify the adapter to refresh the RecyclerView
    }
}