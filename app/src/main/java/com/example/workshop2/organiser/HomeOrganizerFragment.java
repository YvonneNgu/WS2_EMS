package com.example.workshop2.organiser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.workshop2.MainActivity;
import com.example.workshop2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeOrganizerFragment extends Fragment {

    private Button createEventButton, btnPrintCard;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private ImageView profilePicImageView;
    private TextView greetingText;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the fragment layout
        View rootView = inflater.inflate(R.layout.fragment_home_organizer, container, false);

        // Initialize Firebase and UI components
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize views
        createEventButton = rootView.findViewById(R.id.createEventButton);
        btnPrintCard = rootView.findViewById(R.id.btnPrintCard);
        profilePicImageView = rootView.findViewById(R.id.ivProfile3);
        greetingText = rootView.findViewById(R.id.tvGreeting3);


        // Create Event button functionality
        if (createEventButton != null) {
            createEventButton.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), CreateEventActivity.class);
                startActivity(intent);
            });
        }
        // Print Card button functionality
        if (btnPrintCard != null) {
            btnPrintCard.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), InputCardDetailsActivity.class);
                startActivity(intent);
            });
        }


        // Fetch user data
        fetchUserData();

        return rootView;
    }

    private void fetchUserData() {
        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference userDocRef = firestore.collection("users").document(userId);

        userDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    // Retrieve user data from Firestore
                    String fullName = documentSnapshot.getString("fullName");
                    String profilePicUrl = documentSnapshot.getString("profileImageUrl");

                    // Set greeting text
                    greetingText.setText("Hi, " + fullName);

                    // Load profile picture if available
                    if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                        Glide.with(this)
                                .load(profilePicUrl)
                                .circleCrop() // Makes the image circular
                                .into(profilePicImageView);
                    } else {
                        // Set a default profile picture if none exists
                        Glide.with(this)
                                .load(R.drawable.profile_user)
                                .circleCrop()
                                .into(profilePicImageView);
                    }
                } else {
                    Toast.makeText(getActivity(), "User data not found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}