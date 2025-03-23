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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParticipantsFragment extends Fragment {

    private BarChart participantsBarChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_participants, container, false);
        participantsBarChart = view.findViewById(R.id.participants_bar_chart);
        fetchParticipantsData();
        return view;
    }

    private void fetchParticipantsData() {
        String userId;
        try {
            userId = AnalyticsHelper.getCurrentUserId();
        } catch (IllegalStateException e) {
            Toast.makeText(getContext(), "User is not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("events")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<BarEntry> entries = new ArrayList<>();
                    List<String> eventNames = new ArrayList<>();
                    int totalEvents = querySnapshot.size();

                    if (totalEvents == 0) {
                        Toast.makeText(getContext(), "No events available.", Toast.LENGTH_SHORT).show();
                        participantsBarChart.clear();
                        participantsBarChart.invalidate();
                        return;
                    }

                    final int[] processedEvents = {0};
                    int maxDisplayEvents = Math.min(totalEvents, 5); // Limit to 5 events

                    for (var document : querySnapshot) {
                        Map<String, Object> event = document.getData();

                        String eventName = event.get("name") != null ? (String) event.get("name") : "Unnamed Event";
                        String eventId = event.get("eventId") != null ? (String) event.get("eventId") : "";
                        int capacity = event.get("capacity") != null ? ((Long) event.get("capacity")).intValue() : 0;

                        if (!eventId.isEmpty()) {
                            FirebaseFirestore.getInstance()
                                    .collection("eventQR")
                                    .whereEqualTo("eventId", eventId)
                                    .get()
                                    .addOnSuccessListener(participantsSnapshot -> {
                                        int participantsCount = participantsSnapshot.size();
                                        int remainingCapacity = Math.max(capacity - participantsCount, 0);

                                        // Add entry for stacked bar chart (joined + remaining)
                                        if (entries.size() < maxDisplayEvents) {
                                            entries.add(new BarEntry(entries.size(), new float[]{participantsCount, remainingCapacity}));
                                            eventNames.add(eventName);
                                        }

                                        processedEvents[0]++;
                                        if (processedEvents[0] == totalEvents) {
                                            displayParticipantsChart(entries, eventNames);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Failed to fetch participants for event: " + eventName, Toast.LENGTH_SHORT).show();
                                        processedEvents[0]++;
                                        if (processedEvents[0] == totalEvents) {
                                            displayParticipantsChart(entries, eventNames);
                                        }
                                    });
                        } else {
                            processedEvents[0]++;
                            if (processedEvents[0] == totalEvents) {
                                displayParticipantsChart(entries, eventNames);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void displayParticipantsChart(List<BarEntry> entries, List<String> eventNames) {
        if (entries.isEmpty()) {
            Toast.makeText(getContext(), "No participants data available.", Toast.LENGTH_SHORT).show();
            participantsBarChart.clear();
            participantsBarChart.invalidate();
            return;
        }

        // Create BarDataSet
        BarDataSet dataSet = new BarDataSet(entries, "Participants");
        dataSet.setColors(
                ColorTemplate.MATERIAL_COLORS[0], // Joined
                ColorTemplate.MATERIAL_COLORS[1]  // Remaining
        );
        dataSet.setStackLabels(new String[]{"Joined", "Remaining"}); // Set labels
        dataSet.setValueTextSize(12f); // Slightly reduce text size for clarity

        // Use ValueFormatter to display integers
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value); // Convert float to integer
            }
        });

        // Create BarData
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f); // Adjust bar width for better spacing

        // Configure XAxis
        XAxis xAxis = participantsBarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(eventNames));
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(0); // Straight labels for better readability
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        // Enable scrolling for more than 5 events
        participantsBarChart.setScaleEnabled(entries.size() > 5);
        participantsBarChart.setVisibleXRangeMaximum(5f); // Show 5 bars at a time
        participantsBarChart.moveViewToX(0); // Start from the first bar

        // Configure Y-axis
        participantsBarChart.getAxisLeft().setGranularity(1f);
        participantsBarChart.getAxisLeft().setAxisMinimum(0); // Ensure Y-axis starts at 0
        participantsBarChart.getAxisRight().setEnabled(false); // Disable the right axis

        // Configure chart description
        participantsBarChart.setData(barData);
        participantsBarChart.setFitBars(true);
        participantsBarChart.getDescription().setEnabled(true);
        participantsBarChart.getDescription().setText("Participant Analysis");
        participantsBarChart.getDescription().setTextSize(16f); // Adjust text size for better visibility
        participantsBarChart.getDescription().setTextAlign(Paint.Align.CENTER); // Align the title text
        participantsBarChart.getDescription().setPosition(
                participantsBarChart.getWidth() / 2f, // Center horizontally
                40f // Adjust the vertical position (top padding)
        );

        // Configure legend for vertical alignment at the bottom-left
        Legend legend = participantsBarChart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM); // Place legend at the bottom
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT); // Align to the left
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL); // Keep items in a horizontal row
        legend.setDrawInside(false); // Place legend outside the chart
        legend.setYOffset(10f); // Add some space from the bottom of the graph
        legend.setTextSize(12f); // Set text size
        legend.setXEntrySpace(20f); // Add spacing between legend entries horizontally
        legend.setYEntrySpace(10f); // Add vertical spacing if needed
        legend.setFormSize(12f); // Adjust the size of the legend form (color box)


        // Add animation
        participantsBarChart.animateY(1000);

        // Refresh the chart
        participantsBarChart.invalidate();
    }
}