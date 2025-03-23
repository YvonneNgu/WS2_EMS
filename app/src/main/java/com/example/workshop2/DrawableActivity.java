package com.example.workshop2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop2.admin.AdminFragment;
import com.example.workshop2.admin.ReactivateRequest;
import com.example.workshop2.organiser.CustomScannerActivity;
import com.example.workshop2.participant.EventParticipantFragment;
import com.example.workshop2.organiser.AnalyticsOrganizerFragment;
import com.example.workshop2.organiser.HomeOrganizerFragment;
import com.example.workshop2.organiser.ViewEventsForOrganizerFragment;
import com.example.workshop2.participant.HomeParticipantFragment;
import com.example.workshop2.participant.LeaderboardFragment;
import com.example.workshop2.participant.ViewQrCodesActivity;
import com.example.workshop2.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DrawableActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton qrActionButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private HomeOrganizerFragment homeFragmentOrganizer = new HomeOrganizerFragment();
    private HomeParticipantFragment homeFragmentParticipant = new HomeParticipantFragment();
    private ProfileFragment profileFragment = new ProfileFragment();
    private LeaderboardFragment leaderboardFragment = new LeaderboardFragment();
    private AnalyticsOrganizerFragment analyticsOrganizerFragment = new AnalyticsOrganizerFragment();
    private EventParticipantFragment eventParticipantFragment = new EventParticipantFragment();
    private ViewEventsForOrganizerFragment viewEventsForOrganizerFragment = new ViewEventsForOrganizerFragment();
    private AdminFragment adminFragment = new AdminFragment();

    private boolean isOnHomePage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawable);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        qrActionButton = findViewById(R.id.qr_scanner_button);

        loadHomeFragmentBasedOnRole();
        setupBottomNavigation();
        setupQRActionButton();
    }

    private void loadHomeFragmentBasedOnRole() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId != null) {
            DocumentReference userDoc = firestore.collection("users").document(userId);

            userDoc.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String userRole = task.getResult().getString("userType");
                    String userStatus = task.getResult().getString("userStatus");

                    if ("deactivated".equals(userStatus)) {
                        showDeactivatedAccountPopup();
                        return;
                    }

                    if (userRole != null) {
                        switch (userRole) {
                            case "participant":
                                setFragment(homeFragmentParticipant);
                                isOnHomePage = true;
                                break;

                            case "organizer":
                                setFragment(homeFragmentOrganizer);
                                isOnHomePage = true;
                                break;

                            case "admin":
                                setFragment(adminFragment);
                                isOnHomePage = true;
                                break;

                            default:
                                showToastAndLog("Unknown user role: " + userRole, "Unknown user role: " + userRole);
                                break;
                        }
                    } else {
                        showToastAndLog("User role not found.", "User role not found.");
                    }
                } else {
                    showToastAndLog("Failed to fetch user role. Please try again.", "Failed to get user role", task.getException());
                }
            });
        } else {
            showToastAndLog("User not authenticated.", "User not authenticated.");
        }
    }

    private void setupBottomNavigation() {
        Map<Integer, Runnable> fragmentMap = new HashMap<>();
        fragmentMap.put(R.id.homeButton, this::loadHomeFragmentBasedOnRole);
        fragmentMap.put(R.id.eventsButton, this::loadEventFragmentBasedOnRole);
        fragmentMap.put(R.id.chartButton, this::loadAnalyticsFragmentBasedOnRole);
        fragmentMap.put(R.id.profileButton, () -> setFragment(profileFragment));

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Runnable fragmentTransaction = fragmentMap.get(item.getItemId());
            if (fragmentTransaction != null) {
                fragmentTransaction.run();
                return true;
            }
            return false;
        });
    }

    private void loadEventFragmentBasedOnRole() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId != null) {
            DocumentReference userDoc = firestore.collection("users").document(userId);
            userDoc.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String userRole = task.getResult().getString("userType");
                    switch (userRole) {
                        case "participant":
                            setFragment(eventParticipantFragment);
                            break;
                        case "organizer":
                            setFragment(viewEventsForOrganizerFragment);
                            break;
                        case "admin":
                            setFragment(adminFragment);
                            break;
                        default:
                            Toast.makeText(this, "Unknown user role: " + userRole, Toast.LENGTH_SHORT).show();
                            Log.e("EventFragmentLoader", "Unknown user role: " + userRole);
                            break;
                    }
                } else {
                    Toast.makeText(this, "Failed to fetch user role. Please try again.", Toast.LENGTH_SHORT).show();
                    Log.e("EventFragmentLoader", "Error fetching user role", task.getException());
                }
            });
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            Log.e("EventFragmentLoader", "User not authenticated.");
        }
    }

    private void loadAnalyticsFragmentBasedOnRole() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId != null) {
            DocumentReference userDoc = firestore.collection("users").document(userId);
            userDoc.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String userRole = task.getResult().getString("userType");
                    switch (userRole) {
                        case "participant":
                            setFragment(leaderboardFragment);
                            break;
                        case "organizer":
                            setFragment(analyticsOrganizerFragment);
                            break;
                        default:
                            Toast.makeText(this, "Unknown user role: " + userRole, Toast.LENGTH_SHORT).show();
                            Log.e("AnalyticsFragmentLoader", "Unknown user role: " + userRole);
                            break;
                    }
                } else {
                    Toast.makeText(this, "Failed to fetch user role. Please try again.", Toast.LENGTH_SHORT).show();
                    Log.e("AnalyticsFragmentLoader", "Error fetching user role", task.getException());
                }
            });
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            Log.e("AnalyticsFragmentLoader", "User not authenticated.");
        }
    }

    private void setFragment(androidx.fragment.app.Fragment fragment) {
        if (fragment instanceof AdminFragment) {
            bottomNavigationView.setVisibility(View.GONE);
        } else {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    private void setupQRActionButton() {
        qrActionButton = findViewById(R.id.qr_scanner_button);

        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId != null) {
            DocumentReference userDoc = firestore.collection("users").document(userId);
            userDoc.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String userRole = task.getResult().getString("userType");
                    if (userRole != null) {
                        switch (userRole) {
                            case "participant":
                                qrActionButton.setVisibility(View.VISIBLE);
                                qrActionButton.setOnClickListener(v -> {
                                    Intent intent = new Intent(DrawableActivity.this, ViewQrCodesActivity.class);
                                    startActivity(intent);
                                });
                                break;

                            case "organizer":
                                qrActionButton.setVisibility(View.VISIBLE);
                                qrActionButton.setOnClickListener(v -> {
                                    IntentIntegrator integrator = new IntentIntegrator(DrawableActivity.this);
                                    integrator.setOrientationLocked(false); // Allow vertical scanning
                                    integrator.setCaptureActivity(CustomScannerActivity.class);
                                    integrator.setPrompt("Align the QR Code within the frame");
                                    integrator.setBeepEnabled(true);
                                    integrator.initiateScan();
                                });
                                break;

                            case "admin":
                                qrActionButton.setVisibility(View.GONE);
                                break;

                            default:
                                qrActionButton.setVisibility(View.GONE);
                                break;
                        }
                    }
                }
            });
        } else {
            qrActionButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            String contents = intentResult.getContents();
            if (contents != null) {
                try {
                    JSONObject qrData = new JSONObject(contents);
                    String eventId = qrData.getString("eventId");
                    String userId = qrData.getString("userId");

                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    db.collection("events")
                            .document(eventId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String eventDateStr = documentSnapshot.getString("date");

                                    if (eventDateStr != null) {
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                        Date eventDate;
                                        try {
                                            eventDate = dateFormat.parse(eventDateStr);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                            return;
                                        }

                                        if (eventDate != null) {
                                            Date currentDate = new Date();
                                            SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                            if (dateOnlyFormat.format(currentDate).equals(dateOnlyFormat.format(eventDate))) {
                                                db.collection("eventQR")
                                                        .whereEqualTo("eventId", eventId)
                                                        .whereEqualTo("userId", userId)
                                                        .get()
                                                        .addOnSuccessListener(queryDocumentSnapshots -> {
                                                            if (!queryDocumentSnapshots.isEmpty()) {
                                                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                                                    db.collection("eventQR")
                                                                            .document(document.getId())
                                                                            .update("status", "Present")
                                                                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Attendance marked as Present!", Toast.LENGTH_SHORT).show())
                                                                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to update status.", Toast.LENGTH_SHORT).show());
                                                                }
                                                            } else {
                                                                Toast.makeText(this, "Record not found for this user.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        })
                                                        .addOnFailureListener(e -> Toast.makeText(this, "Error checking record: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                            } else {
                                                Toast.makeText(this, "This QR code is not valid for today's date.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    } else {
                                        Toast.makeText(this, "Event details mismatch.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error retrieving event details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Invalid QR Code format.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No QR code detected.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showToastAndLog(String message, String logMessage) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.e("DrawableActivity", logMessage);
    }

    private void showToastAndLog(String message, String logMessage, Exception e) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.e("DrawableActivity", logMessage, e);
    }

    private void showDeactivatedAccountPopup() {
        new AlertDialog.Builder(this)
                .setTitle("Account Deactivated")
                .setMessage("Your account is deactivated. Do you want to request reactivation?")
                .setPositiveButton("Request Reactivation", (dialog, which) -> {
                    requestReactivation();
                    Intent intent = new Intent(DrawableActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                    Intent intent = new Intent(DrawableActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    private void requestReactivation() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("reactivationRequests")
                    .document(userId)
                    .set(new ReactivateRequest(userId, new Date()))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Reactivation request sent successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to send request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onBackPressed() {
        if (isOnHomePage) {
            Log.d("DrawableActivity", "Back press disabled on home page");
        } else {
            super.onBackPressed();
        }
    }
}
