package com.example.workshop2.cert;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.workshop2.R;
import com.example.workshop2.cert.model.GeneratedCert;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CertMgmtActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String eventId;
    private String eventName;

    private TextView tvEventName;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private Button btnManageDesign;
    private TextView tvNoCertificates;
    private Button btnGenerate;

    private Map<String, List<GeneratedCert>> certificatesByType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cert_mgmt);

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");

        initViews();
        loadGeneratedCerts();
    }

    private void initViews() {
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvEventName = findViewById(R.id.tvEventName);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        btnManageDesign = findViewById(R.id.btnManageDesign);
        tvNoCertificates = findViewById(R.id.tvNoCertificates);
        btnGenerate = findViewById(R.id.btnSaveDesign);

        tvTitle.setText("Certificates");
        tvEventName.setText(eventName);

        btnManageDesign.setOnClickListener(v -> {
            Intent intent = new Intent(CertMgmtActivity.this, DesignMgmtActivity.class);
            intent.putExtra("eventId", eventId);
            intent.putExtra("eventName", eventName);
            startActivity(intent);
            finish();
        });

        btnGenerate.setOnClickListener(v -> showCertificateTypeDialog());
    }

    private void loadGeneratedCerts() {
        certificatesByType = new HashMap<>();

        db.collection("certificates")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        GeneratedCert certificate = document.toObject(GeneratedCert.class);
                        certificate.setId(document.getId());

                        String certificateType = certificate.getCertificateType();
                        if (!certificatesByType.containsKey(certificateType)) {
                            certificatesByType.put(certificateType, new ArrayList<>());
                        }
                        certificatesByType.get(certificateType).add(certificate);
                    }

                    updateUI();
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                });
    }

    private void updateUI() {
        if (certificatesByType.isEmpty()) {
            viewPager.setVisibility(View.GONE);
            tabLayout.setVisibility(View.GONE);
            tvNoCertificates.setVisibility(View.VISIBLE);
            btnGenerate.setVisibility(View.VISIBLE);
        } else {
            viewPager.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.VISIBLE);
            tvNoCertificates.setVisibility(View.GONE);
            btnGenerate.setVisibility(View.GONE);

            setupViewPager();
        }
    }

    private void setupViewPager() {
        List<String> tabTitles = new ArrayList<>(certificatesByType.keySet());
        tabTitles.add("+"); // Add the "+" tab
        CertPagerAdapter pagerAdapter = new CertPagerAdapter(this, certificatesByType, tabTitles);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(pagerAdapter.getTabTitle(position))
        ).attach();
    }

    void showCertificateTypeDialog() {
        // Fetch the certificate types from the resources
        String[] allCertificateTypes = getResources().getStringArray(R.array.certificate_types);
        // Create a new array excluding the last item
        String[] certificateTypes = Arrays.copyOf(allCertificateTypes, allCertificateTypes.length - 1);

        new AlertDialog.Builder(this)
                .setTitle("Select Certificate Type")
                .setItems(certificateTypes, (dialog, which) -> {
                    String certificateType = certificateTypes[which];
                    Intent intent = new Intent(CertMgmtActivity.this, SelectParticipantActivity.class);
                    intent.putExtra("eventId", eventId);
                    intent.putExtra("eventName", eventName);
                    intent.putExtra("certificateType", certificateType);
                    startActivity(intent);
                    finish();
                })
                .show();
    }
}