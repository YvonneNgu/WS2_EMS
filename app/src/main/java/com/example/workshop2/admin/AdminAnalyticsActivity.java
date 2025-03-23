package com.example.workshop2.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop2.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminAnalyticsActivity extends AppCompatActivity {
    private BarChart barChart;
    private PieChart pieChart;
    private TextView barChartTitle; // Custom TextView for Bar Chart title

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_analytics);

        // Initialize UI components
        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);
        barChartTitle = findViewById(R.id.barChartTitle); // Title for Bar Chart

        // Set up the charts
        setupBarChart();
        setupPieChart();
    }

    private void setupBarChart() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query Firestore to fetch events
        db.collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        // Map to hold the count of events per category
                        Map<String, Integer> categoryCountMap = new HashMap<>();

                        // Loop through all the documents in the 'events' collection
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String category = document.getString("category");

                            if (category != null) {
                                // Increment the count for the current category
                                categoryCountMap.put(category, categoryCountMap.getOrDefault(category, 0) + 1);
                            }
                        }

                        // After collecting all data, update the Bar Chart
                        updateBarChart(categoryCountMap);
                    } else {
                        Toast.makeText(AdminAnalyticsActivity.this, "No events found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching events", e);
                    Toast.makeText(AdminAnalyticsActivity.this, "Error fetching events.", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateBarChart(Map<String, Integer> categoryCountMap) {
        // Prepare data for the Bar Chart
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int index = 0;

        // Loop through the category count map to create entries for the Bar Chart
        for (Map.Entry<String, Integer> entry : categoryCountMap.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue())); // X-axis index, Y-axis value
            labels.add(entry.getKey()); // Add the category label
            index++;
        }

        // Create Bar DataSet
        BarDataSet barDataSet = new BarDataSet(entries, "Total Events by Category");
        barDataSet.setColor(Color.parseColor("#B19CD7")); // Set bar color to the specified hex code
        barDataSet.setValueTextColor(Color.BLACK); // Value text color
        barDataSet.setValueTextSize(12f); // Value text size

        // Create BarData and set it to the Bar Chart
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.8f); // Adjust bar width for spacing
        barChart.setData(barData);

        // Configure the X-axis to show category labels
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels)); // Set category labels
        xAxis.setGranularity(1f); // Ensure 1-to-1 mapping
        xAxis.setGranularityEnabled(true); // Enable granularity
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Place labels at the bottom
        xAxis.setTextSize(10f); // Adjust text size for X-axis labels
        xAxis.setDrawGridLines(false); // Disable grid lines

        // Customizing the Bar Chart appearance
        barChart.getDescription().setEnabled(false); // Disable built-in description
        barChart.animateY(1000); // Vertical animation
        barChart.getAxisRight().setEnabled(false); // Disable right Y-axis
        barChart.invalidate(); // Refresh the chart
    }

    private void setupPieChart() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query Firestore for event status distribution
        db.collection("events")
                .whereEqualTo("status", "Approved")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int approvedCount = queryDocumentSnapshots.size();

                    db.collection("events")
                            .whereEqualTo("status", "Pending")
                            .get()
                            .addOnSuccessListener(pendingSnapshot -> {
                                int pendingCount = pendingSnapshot.size();

                                db.collection("events")
                                        .whereEqualTo("status", "Rejected")
                                        .get()
                                        .addOnSuccessListener(rejectedSnapshot -> {
                                            int rejectedCount = rejectedSnapshot.size();

                                            // Create Pie Chart Data
                                            ArrayList<PieEntry> entries = new ArrayList<>();
                                            entries.add(new PieEntry(approvedCount, "Approved"));
                                            entries.add(new PieEntry(pendingCount, "Pending"));
                                            entries.add(new PieEntry(rejectedCount, "Rejected"));

                                            PieDataSet dataSet = new PieDataSet(entries, "Event Status");

                                            // Set specific colors for each status
                                            ArrayList<Integer> colors = new ArrayList<>();
                                            colors.add(Color.GREEN);   // Approved in Green
                                            colors.add(Color.parseColor("#FFA500"));  // Pending in Orange
                                            colors.add(Color.RED);     // Rejected in Red

                                            dataSet.setColors(colors); // Apply custom colors
                                            dataSet.setSliceSpace(2f);
                                            dataSet.setValueTextSize(12f);

                                            PieData pieData = new PieData(dataSet);
                                            pieChart.setData(pieData);

                                            // Customize Pie Chart Appearance
                                            pieChart.getDescription().setText("Event Status Distribution");
                                            pieChart.getDescription().setTextSize(10f);
                                            pieChart.setDrawHoleEnabled(true);
                                            pieChart.setHoleRadius(40f); // Set hollow center size
                                            pieChart.setUsePercentValues(true); // Display values as percentages
                                            pieChart.animateY(1000);

                                            pieChart.invalidate(); // Refresh the chart
                                        });
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show();
                });
    }
}
