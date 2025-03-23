package com.example.workshop2.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop2.R;
import com.example.workshop2.model.Event;

import java.util.List;

public class EventApprovalAdapter extends RecyclerView.Adapter<EventApprovalAdapter.ViewHolder> {

    private List<Event> eventList;
    private OnReviewClickListener reviewClickListener;

    public EventApprovalAdapter(List<Event> eventList, OnReviewClickListener reviewClickListener) {
        this.eventList = eventList;
        this.reviewClickListener = reviewClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_approve_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView eventName, eventDescription;
        private Button reviewButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.eventName);
            eventDescription = itemView.findViewById(R.id.eventDescription);
            reviewButton = itemView.findViewById(R.id.reviewButton);
        }

        public void bind(Event event) {
            eventName.setText(event.getName());
            eventDescription.setText(event.getDescription());

            reviewButton.setOnClickListener(v -> reviewClickListener.onReviewClick(event));
        }
    }

    public interface OnReviewClickListener {
        void onReviewClick(Event event);
    }
}
