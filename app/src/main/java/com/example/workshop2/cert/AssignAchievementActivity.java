package com.example.workshop2.cert;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop2.R;
import com.example.workshop2.cert.model.CertificateItem;
import com.example.workshop2.cert.model.Design;
import com.example.workshop2.cert.model.ParticipantAchievement;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AssignAchievementActivity extends AppCompatActivity {

    private RecyclerView rvParticipants;
    private ImageButton ibAssignBatch;
    private Button btnSubmit, btnBack;
    private ParticipantAchievementAdapter adapter;
    private List<ParticipantAchievement> participants;
    private String eventId;
    private String eventName;
    private String certificateType;
    private CheckBox cbSelectAll;
    private TextView tvInstructions;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_achievement);

        // Initialize views
        rvParticipants = findViewById(R.id.rvParticipants);
        ibAssignBatch = findViewById(R.id.ibAssignBatch);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack = findViewById(R.id.btnBackToSelect);
        cbSelectAll = findViewById(R.id.cbSelectAll);
        tvInstructions = findViewById(R.id.tvInstructions);
        progressBar = findViewById(R.id.progressBar);

        // Get intent extras
        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");
        certificateType = getIntent().getStringExtra("certificateType");
        Map<String, String> participantIdNames = (HashMap<String, String>)getIntent().getSerializableExtra("participantIdName");

        // Initialize participants list
        participants = new ArrayList<>();
        for (Map.Entry<String, String> participantIdName : participantIdNames.entrySet()) {
            participants.add(new ParticipantAchievement(participantIdName.getKey(), participantIdName.getValue(), ""));
        }

        // Set up RecyclerView
        adapter = new ParticipantAchievementAdapter(participants, this::updateBatchAssignIbVisibility);
        rvParticipants.setLayoutManager(new LinearLayoutManager(this));
        rvParticipants.setAdapter(adapter);

        // Set up batch assign button
        ibAssignBatch.setOnClickListener(v -> showBatchAssignmentDialog());

        // Set up back button
        btnBack.setOnClickListener(v -> {
            Intent intent;
            intent = new Intent(this, SelectParticipantActivity.class);
            intent.putExtra("eventId", eventId);
            intent.putExtra("eventName", eventName);
            intent.putExtra("certificateType", certificateType);
            intent.putExtra("participantIdName", (Serializable) participantIdNames);
            startActivity(intent);
            finish();
        });

        // Set up Submit button
        btnSubmit.setOnClickListener(v -> submitAchievements());

        // Set up Select All checkbox
        cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (ParticipantAchievement participant : participants) {
                participant.setSelected(isChecked);
            }
            adapter.notifyDataSetChanged();
            updateBatchAssignIbVisibility();
        });

        // Set instructions
        tvInstructions.setText("Instructions:\n1. Enter achievements individually in the text boxes.\n2. Use checkboxes for batch assignment.");

        // Restore state if available
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }
    }

    private void updateBatchAssignIbVisibility() {
        boolean anySelected = false;
        for (ParticipantAchievement participant : participants) {
            if (participant.isSelected()) {
                anySelected = true;
                break;
            }
        }
        ibAssignBatch.setVisibility(anySelected ? View.VISIBLE : View.GONE);
    }

    private void showBatchAssignmentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Assign Achievement");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_batch_achievement, null);
        final EditText input = viewInflated.findViewById(R.id.etBatchAchievement);
        builder.setView(viewInflated);

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            String achievement = input.getText().toString();
            if (!achievement.isEmpty()) {
                for (ParticipantAchievement participant : participants) {
                    if (participant.isSelected()) {
                        participant.setAchievement(achievement);
                        participant.setSelected(false);
                    }
                }
                adapter.notifyDataSetChanged();
                updateBatchAssignIbVisibility();
                cbSelectAll.setChecked(false);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void submitAchievements() {
        // Validate that all participants have achievements assigned
        boolean allAssigned = true;

        for (ParticipantAchievement participant : participants) {
            String achievement = participant.getAchievement().trim();
            if (achievement.isEmpty()) {
                allAssigned = false;
                break;
            }
        }

        if (allAssigned) {
            CertGenerator certGenerator = new CertGenerator(this,eventId, eventName, certificateType, progressBar);
            certGenerator.showDesignSelectionDialog(participants);
        } else {
            Toast.makeText(this, "Please assign achievements to all participants", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("participants", new ArrayList<>(participants));
        outState.putBoolean("selectAllChecked", cbSelectAll.isChecked());
    }

    private void restoreState(Bundle savedInstanceState) {
        ArrayList<ParticipantAchievement> savedParticipants = savedInstanceState.getParcelableArrayList("participants");
        if (savedParticipants != null) {
            participants.clear();
            participants.addAll(savedParticipants);
            adapter.notifyDataSetChanged();
            updateBatchAssignIbVisibility();
        }
        cbSelectAll.setChecked(savedInstanceState.getBoolean("selectAllChecked", false));
    }
}