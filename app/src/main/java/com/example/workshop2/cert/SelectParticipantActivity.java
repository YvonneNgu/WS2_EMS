package com.example.workshop2.cert;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop2.R;
import com.example.workshop2.cert.model.Participant;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SelectParticipantActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String eventId;
    private String eventName;
    private String certificateType;

    private RecyclerView rvParticipants;
    private ParticipantAdapter participantAdapter;
    private List<Participant> participants;

    private CheckBox cbAllRegistered, cbOnlyAttended, cbNoParticipation, cbNoAchievement, cbSelectAll;
    private Button btnNext, btnBack;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_participant);

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");
        certificateType = getIntent().getStringExtra("certificateType");

        initViews();
        cbOnlyAttended.setChecked(true);
        setupRecyclerView();
        loadAttendedParticipants();
        setupListeners();
    }

    private void initViews() {
        rvParticipants = findViewById(R.id.rvParticipants);
        cbAllRegistered = findViewById(R.id.cbAllRegistered);
        cbOnlyAttended = findViewById(R.id.cbOnlyAttended);
        cbNoParticipation = findViewById(R.id.cbNoParticipation);
        cbNoAchievement = findViewById(R.id.cbNoAchievement);
        cbSelectAll = findViewById(R.id.cbSelectAll);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBackToCertMgmt);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        participants = new ArrayList<>();
        participantAdapter = new ParticipantAdapter(participants);
        rvParticipants.setLayoutManager(new LinearLayoutManager(this));
        rvParticipants.setAdapter(participantAdapter);
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    private void loadAttendedParticipants() {
        showProgressBar();
        participants.clear();
        db.collection("eventQR")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", "Present")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String participantId = document.getString("userId");
                        loadParticipantDetails(participantId, "Present");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading participants", Toast.LENGTH_SHORT).show();
                    hideProgressBar();
                });
    }

    private void loadAllParticipants() {
        showProgressBar();
        participants.clear();
        db.collection("eventQR")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String participantId = document.getString("userId");
                        String attendanceStatus = document.getString("status");
                        loadParticipantDetails(participantId, attendanceStatus);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading participants", Toast.LENGTH_SHORT).show();
                    hideProgressBar();
                });
    }

    private void loadParticipantDetails(String participantId, String attendanceStatus) {
        db.collection("users").document(participantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String fullName = documentSnapshot.getString("fullName");
                    Participant participant = new Participant(participantId, fullName, attendanceStatus);
                    loadCertificateStatus(participant);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading participant details", Toast.LENGTH_SHORT).show();
                    hideProgressBar();
                });
    }

    private void loadCertificateStatus(Participant participant) {
        db.collection("certificates")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("participantId", participant.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String certType = document.getString("certificateType");
                        if ("Participation".equals(certType)) {
                            participant.setHasParticipationCert(true);
                        } else if ("Achievement".equals(certType)) {
                            participant.setHasAchievementCert(true);
                        }
                    }
                    participants.add(participant);
                    participantAdapter.notifyDataSetChanged();
                    applyFilters();
                    if (participants.size() == participantAdapter.getItemCount()) {
                        hideProgressBar();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading certificate status", Toast.LENGTH_SHORT).show();
                    hideProgressBar();
                });
    }

    private void setupListeners() {
        cbAllRegistered.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbOnlyAttended.setChecked(false);
                loadAllParticipants();
            } else if (!cbOnlyAttended.isChecked()) {
                cbOnlyAttended.setChecked(true);
            }
            applyFilters();
        });

        cbOnlyAttended.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbAllRegistered.setChecked(false);
                loadAttendedParticipants();
            } else if (!cbAllRegistered.isChecked()) {
                cbAllRegistered.setChecked(true);
            }
            applyFilters();
        });

        cbNoParticipation.setOnCheckedChangeListener((buttonView, isChecked) -> applyFilters());
        cbNoAchievement.setOnCheckedChangeListener((buttonView, isChecked) -> applyFilters());

        cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (Participant participant : participants) {
                participant.setSelected(isChecked);
            }
            participantAdapter.notifyDataSetChanged();
        });

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, CertMgmtActivity.class);
            intent.putExtra("eventId", eventId);
            intent.putExtra("eventName", eventName);
            startActivity(intent);
            finish();
        });

        btnNext.setOnClickListener(v -> {
            Map<String,String> participantIdName = new HashMap<>();
            for (Participant participant : participants) {
                if (participant.isSelected()) {
                    participantIdName.put(participant.getId(), participant.getFullName());
                }
            }
            if (participantIdName.isEmpty()) {
                Toast.makeText(this, "Please select at least one participant", Toast.LENGTH_SHORT).show();
            } else {

                if(Objects.equals(certificateType, "Participation")) { // skip assign achievement
                    CertGenerator certGenerator = new CertGenerator(this, eventId, eventName, certificateType, progressBar);
                    certGenerator.showDesignSelectionDialog(participantIdName);
                }
                else { //Achievement
                    Intent intent;
                    intent = new Intent(SelectParticipantActivity.this, AssignAchievementActivity.class);
                    intent.putExtra("eventId", eventId);
                    intent.putExtra("eventName", eventName);
                    intent.putExtra("certificateType", certificateType);
                    intent.putExtra("participantIdName", (Serializable) participantIdName);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void applyFilters() {
        List<Participant> filteredList = new ArrayList<>();
        for (Participant participant : participants) {
            if (shouldIncludeParticipant(participant)) {
                filteredList.add(participant);
            }
        }
        participantAdapter.updateList(filteredList);
    }

    private boolean shouldIncludeParticipant(Participant participant) {
        boolean includeAllRegistered = cbAllRegistered.isChecked();
        boolean includeOnlyAttended = cbOnlyAttended.isChecked();
        boolean includeNoParticipation = cbNoParticipation.isChecked();
        boolean includeNoAchievement = cbNoAchievement.isChecked();

        if (includeOnlyAttended && !"Present".equals(participant.getAttendanceStatus())) {
            return false;
        }

        if (includeNoParticipation && participant.hasParticipationCert()) {
            return false;
        }

        if (includeNoAchievement && participant.hasAchievementCert()) {
            return false;
        }

        return includeAllRegistered || includeOnlyAttended;
    }
}
