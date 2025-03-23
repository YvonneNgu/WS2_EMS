package com.example.workshop2.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.workshop2.MainActivity;
import com.example.workshop2.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private MaterialTextView userNameTextView, userEmailTextView, userDobTextView, userIdCardTextView, userOrgNameTextView, userPositionTextView, userPhoneNumberTextView;
    private ImageView profilePicImageView, iconDob, iconIdCard, iconOrgName, iconPosition;
    private MaterialButton editProfileButton, deleteAccountButton;
    private FloatingActionButton logoutButton;

    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;

    private String userId;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firestore and Firebase Authentication
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        userNameTextView = view.findViewById(R.id.tvName);
        userEmailTextView = view.findViewById(R.id.userEmailTextView);
        userDobTextView = view.findViewById(R.id.userDobTextView);
        userIdCardTextView = view.findViewById(R.id.userIdCardTextView);
        userOrgNameTextView = view.findViewById(R.id.userOrgNameTextView);
        userPositionTextView = view.findViewById(R.id.userPositionTextView);
        userPhoneNumberTextView = view.findViewById(R.id.tvPhoneNumber);
        profilePicImageView = view.findViewById(R.id.profilePicImageView);
        editProfileButton = view.findViewById(R.id.btnEditProfile);
        deleteAccountButton = view.findViewById(R.id.btnDisableAccount);
        logoutButton = view.findViewById(R.id.btnLogout);

        // Initialize Icons
        iconDob = view.findViewById(R.id.iconDob);
        iconIdCard = view.findViewById(R.id.iconIdCard);
        iconOrgName = view.findViewById(R.id.iconOrgName);
        iconPosition = view.findViewById(R.id.iconPosition);

        // Fetch user data from Firestore
        fetchUserData();

        // Handle Edit Profile button click
        editProfileButton.setOnClickListener(v -> navigateToEditProfile());

        // Handle Delete Account button click
        deleteAccountButton.setOnClickListener(v -> showDeactivateAccountDialog());

        // Set click listener for logout button
        logoutButton.setOnClickListener(v -> logoutUser());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fetch user data from Firestore when the fragment resumes
        fetchUserData();
    }

    private void fetchUserData() {
        // Check if user is authenticated
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "No user logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current user ID
        userId = mAuth.getCurrentUser().getUid();
        DocumentReference userDocRef = firestore.collection("users").document(userId);

        userDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    // Retrieve user data from Firestore
                    String fullName = documentSnapshot.getString("fullName");
                    String email = documentSnapshot.getString("email");
                    String dob = documentSnapshot.getString("dob");
                    String idCard = documentSnapshot.getString("idCard");
                    String orgName = documentSnapshot.getString("orgName");
                    String position = documentSnapshot.getString("position");
                    String phoneNumber = documentSnapshot.getString("phoneNumber");
                    String profilePicUrl = documentSnapshot.getString("profileImageUrl"); // Get the profile picture URL
                    String role = documentSnapshot.getString("role"); // Fetch the user's role

                    // Log user role
                    Log.d(TAG, "User role: " + role);

                    // Set the data to the UI
                    userNameTextView.setText(fullName);
                    userEmailTextView.setText(email);
                    userPhoneNumberTextView.setText(phoneNumber); // Display phone number

                    // Handle role-specific visibility
                    if ("participant".equalsIgnoreCase(role)) {
                        Log.d(TAG, "Setting visibility for participant");
                        userOrgNameTextView.setVisibility(View.GONE);
                        iconOrgName.setVisibility(View.GONE);
                        userPositionTextView.setVisibility(View.GONE);
                        iconPosition.setVisibility(View.GONE);
                        userDobTextView.setVisibility(View.VISIBLE);
                        iconDob.setVisibility(View.VISIBLE);
                        userIdCardTextView.setVisibility(View.VISIBLE);
                        iconIdCard.setVisibility(View.VISIBLE);
                    } else if ("organizer".equalsIgnoreCase(role)) {
                        Log.d(TAG, "Setting visibility for organizer");
                        userDobTextView.setVisibility(View.GONE);
                        iconDob.setVisibility(View.GONE);
                        userIdCardTextView.setVisibility(View.GONE);
                        iconIdCard.setVisibility(View.GONE);
                        userOrgNameTextView.setVisibility(View.VISIBLE);
                        iconOrgName.setVisibility(View.VISIBLE);
                        userPositionTextView.setVisibility(View.VISIBLE);
                        iconPosition.setVisibility(View.VISIBLE);
                    }

                    // Display the Date of Birth, if available
                    if (dob != null && !dob.isEmpty()) {
                        userDobTextView.setText(dob);
                        userDobTextView.setVisibility(View.VISIBLE);
                        iconDob.setVisibility(View.VISIBLE);
                    } else {
                        userDobTextView.setVisibility(View.GONE);
                        iconDob.setVisibility(View.GONE);
                    }

                    // Display ID Card, if available
                    if (idCard != null && !idCard.isEmpty()) {
                        userIdCardTextView.setText(idCard);
                        userIdCardTextView.setVisibility(View.VISIBLE);
                        iconIdCard.setVisibility(View.VISIBLE);
                    } else {
                        userIdCardTextView.setVisibility(View.GONE);
                        iconIdCard.setVisibility(View.GONE);
                    }

                    // Display Organization Name, if available
                    if (orgName != null && !orgName.isEmpty()) {
                        userOrgNameTextView.setText(orgName);
                        userOrgNameTextView.setVisibility(View.VISIBLE);
                        iconOrgName.setVisibility(View.VISIBLE);
                    } else {
                        userOrgNameTextView.setVisibility(View.GONE);
                        iconOrgName.setVisibility(View.GONE);
                    }

                    // Display Position, if available
                    if (position != null && !position.isEmpty()) {
                        userPositionTextView.setText(position);
                        userPositionTextView.setVisibility(View.VISIBLE);
                        iconPosition.setVisibility(View.VISIBLE);
                    } else {
                        userPositionTextView.setVisibility(View.GONE);
                        iconPosition.setVisibility(View.GONE);
                    }

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
                    Toast.makeText(getContext(), "User data not found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Failed to load user data. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToEditProfile() {
        Intent intent = new Intent(requireContext(), EditProfileActivity.class);
        startActivity(intent);
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private void showDeactivateAccountDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Deactivate Account")
                .setMessage("Are you sure you want to deactivate your account? You can reactivate it later.")
                .setPositiveButton("Yes", (dialog, which) -> deactivateAccount())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void deactivateAccount() {
        userId = mAuth.getCurrentUser().getUid();
        DocumentReference userDocRef = firestore.collection("users").document(userId);

        // Update the userStatus field to "deactivated"
        userDocRef.update("userStatus", "deactivated")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Account deactivated successfully.", Toast.LENGTH_SHORT).show();
                    logoutUser(); // Log the user out after deactivating
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to deactivate account.", Toast.LENGTH_SHORT).show());
    }
}