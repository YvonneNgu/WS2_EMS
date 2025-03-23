package com.example.workshop2.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.workshop2.R;

import java.util.ArrayList;

public class StatusEventAdapter extends RecyclerView.Adapter<StatusEventAdapter.StatusEventViewHolder> {

    private Context context;
    private ArrayList<Event> eventList;

    public StatusEventAdapter(Context context, ArrayList<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public StatusEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.event_item_status, parent, false);
        return new StatusEventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusEventViewHolder holder, int position) {
        Event event = eventList.get(position);

        // Bind event details
        holder.eventNameTextView.setText(event.getName());
        holder.eventDescriptionTextView.setText(event.getDescription());
        holder.eventLocationTextView.setText(event.getLocation());
        holder.eventDateTextView.setText(event.getDate());
        holder.eventPriceTextView.setText("Price: " + event.getEntryPrice());
        holder.eventCategoryTextView.setText("Category: " + event.getCategory());
        holder.eventCapacityTextView.setText("Capacity: " + event.getCapacity());

        // Start animation only for pending events
        holder.clockAnimationView.setVisibility(View.VISIBLE);
        holder.clockAnimationView.playAnimation();
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class StatusEventViewHolder extends RecyclerView.ViewHolder {
        public TextView eventNameTextView, eventDescriptionTextView, eventLocationTextView, eventDateTextView, eventPriceTextView,eventCapacityTextView,eventCategoryTextView;
        public LottieAnimationView clockAnimationView;

        public StatusEventViewHolder(View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            eventDescriptionTextView = itemView.findViewById(R.id.eventDescriptionTextView);
            eventLocationTextView = itemView.findViewById(R.id.eventLocationTextView);
            eventDateTextView = itemView.findViewById(R.id.eventDateTextView);
            eventPriceTextView = itemView.findViewById(R.id. eventPriceTextView);
            eventCapacityTextView = itemView.findViewById(R.id. eventCapacityTextView);
            eventCategoryTextView = itemView.findViewById(R.id. eventCategoryTextView);
            clockAnimationView = itemView.findViewById(R.id.clockAnimationView);
        }
    }
}
