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

public class RevenueFragment extends Fragment {

    private PieChart revenuePieChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_revenue, container, false);
        revenuePieChart = view.findViewById(R.id.revenue_pie_chart);
        fetchRevenueData();
        return view;
    }

    private void fetchRevenueData() {
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
                    List<PieEntry> entries = new ArrayList<>();
                    int totalEvents = querySnapshot.size();
                    int[] processedCount = {0}; // Tracks how many events have been processed

                    if (totalEvents == 0) {
                        Toast.makeText(getContext(), "No events available.", Toast.LENGTH_SHORT).show();
                        revenuePieChart.clear();
                        revenuePieChart.invalidate();
                        return;
                    }

                    for (var document : querySnapshot) {
                        Map<String, Object> event = document.getData();
                        String eventName = event.get("name") != null ? (String) event.get("name") : "Unnamed Event";
                        double entryPrice = event.get("entryPrice") instanceof Number
                                ? ((Number) event.get("entryPrice")).doubleValue()
                                : 0;
                        String eventId = event.get("eventId") != null ? (String) event.get("eventId") : "";

                        if (!eventId.isEmpty() && entryPrice > 0) {
                            FirebaseFirestore.getInstance()
                                    .collection("eventQR")
                                    .whereEqualTo("eventId", eventId)
                                    .get()
                                    .addOnSuccessListener(participantsSnapshot -> {
                                        int participantsCount = participantsSnapshot.size();
                                        double totalRevenue = participantsCount * entryPrice;

                                        if (totalRevenue > 0) {
                                            entries.add(new PieEntry((float) totalRevenue, eventName));
                                        }

                                        processedCount[0]++;
                                        if (processedCount[0] == totalEvents) {
                                            displayRevenueChart(entries);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Failed to fetch participants for event: " + eventName, Toast.LENGTH_SHORT).show();
                                        processedCount[0]++;
                                        if (processedCount[0] == totalEvents) {
                                            displayRevenueChart(entries);
                                        }
                                    });
                        } else {
                            processedCount[0]++;
                            if (processedCount[0] == totalEvents) {
                                displayRevenueChart(entries);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch revenue data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void displayRevenueChart(List<PieEntry> entries) {
        if (entries.isEmpty()) {
            Toast.makeText(getContext(), "No revenue data available to display.", Toast.LENGTH_SHORT).show();
            revenuePieChart.clear();
            revenuePieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "Revenue");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS); // Use a predefined color set
        dataSet.setValueTextSize(14f);
        dataSet.setSliceSpace(2f); // Add spacing between slices

        PieData pieData = new PieData(dataSet);

        revenuePieChart.setData(pieData);
        revenuePieChart.setUsePercentValues(true);
        revenuePieChart.getDescription().setEnabled(true);
        revenuePieChart.getDescription().setText("Revenue Distribution");
        revenuePieChart.getDescription().setTextSize(16f); // Adjust text size for better visibility
        revenuePieChart.getDescription().setTextAlign(Paint.Align.CENTER); // Align the title text
        revenuePieChart.getDescription().setPosition(
                revenuePieChart.getWidth() / 2f, // Center horizontally
                40f // Adjust the vertical position (top padding)
        );

        // Configure the legend for better readability
        Legend legend = revenuePieChart.getLegend();
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
        revenuePieChart.animateY(1000);
        revenuePieChart.invalidate();
    }
}