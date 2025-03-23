package com.example.workshop2.organiser;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.workshop2.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventTypesFragment extends Fragment {

    private PieChart freePaidPieChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_types, container, false);
        freePaidPieChart = view.findViewById(R.id.free_paid_pie_chart);
        fetchEventTypesData();
        return view;
    }

    private void fetchEventTypesData() {
        String userId = AnalyticsHelper.getCurrentUserId();

        // Firestore query to fetch events based on user ID
        FirebaseFirestore.getInstance()
                .collection("events")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        // No events to process
                        Toast.makeText(getContext(), "No events available.", Toast.LENGTH_SHORT).show();
                        freePaidPieChart.clear();
                        freePaidPieChart.invalidate();
                        return;
                    }

                    int freeEvents = 0;
                    int paidEvents = 0;

                    for (var document : querySnapshot) {
                        Map<String, Object> event = document.getData();

                        // Check fee type to determine event type
                        String feeType = event.get("feeType") != null ? (String) event.get("feeType") : "";
                        if ("Free".equalsIgnoreCase(feeType)) {
                            freeEvents++;
                        } else if ("Paid".equalsIgnoreCase(feeType)) {
                            paidEvents++;
                        }
                    }

                    // Display the chart with the counts
                    displayEventTypesChart(freeEvents, paidEvents);
                })
                .addOnFailureListener(e -> {
                    // Handle Firestore query failure
                    Toast.makeText(getContext(), "Failed to fetch event types data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void displayEventTypesChart(int freeEvents, int paidEvents) {
        if (freeEvents == 0 && paidEvents == 0) {
            Toast.makeText(getContext(), "No event type data available.", Toast.LENGTH_SHORT).show();
            freePaidPieChart.clear();
            freePaidPieChart.invalidate();
            return;
        }

        // Prepare entries for the PieChart
        List<PieEntry> entries = new ArrayList<>();
        if (freeEvents > 0) {
            entries.add(new PieEntry(freeEvents, "Free Events"));
        }
        if (paidEvents > 0) {
            entries.add(new PieEntry(paidEvents, "Paid Events"));
        }

        // Create PieDataSet
        PieDataSet dataSet = new PieDataSet(entries, "Event Types");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Use predefined material colors
        dataSet.setValueTextSize(14f); // Set value text size
        dataSet.setSliceSpace(2f); // Add spacing between slices

        // Create PieData
        PieData pieData = new PieData(dataSet);

        // Configure the PieChart
        freePaidPieChart.setData(pieData);
        freePaidPieChart.setUsePercentValues(true); // Display values as percentages
        freePaidPieChart.getDescription().setEnabled(true);
        freePaidPieChart.getDescription().setText("Event Types Distribution");
        freePaidPieChart.getDescription().setTextSize(16f); // Adjust text size for better visibility
        freePaidPieChart.getDescription().setTextAlign(Paint.Align.CENTER); // Align the title text
        freePaidPieChart.getDescription().setPosition(
                freePaidPieChart.getWidth() / 2f, // Center horizontally
                40f // Adjust the vertical position (top padding)
        );

        // Configure legend to place it at the bottom-left corner
        Legend legend = freePaidPieChart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER); // Align to the bottom-left
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false); // Ensure legend is outside the chart
        legend.setTextSize(12f);
        legend.setYOffset(90f); // Move legend closer to the chart
        legend.setXOffset(-20f);
        legend.setXEntrySpace(20f); // Add spacing between legend entries
        legend.setYEntrySpace(10f);  // Add vertical spacing for clarity
        legend.setWordWrapEnabled(true); // Enable word wrapping to avoid cramped text

        // Animate and refresh the chart
        freePaidPieChart.animateY(1000); // Animation for appearance
        freePaidPieChart.invalidate(); // Refresh the chart
    }
}
