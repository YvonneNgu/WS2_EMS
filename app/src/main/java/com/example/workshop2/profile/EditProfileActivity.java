package com.example.workshop2.profile;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.workshop2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText nameEditText, emailEditText, dobEditText, orgNameEditText, positionEditText, phoneNumberEditText;
    private Button saveButton;
    private ImageView profileImageView;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;

    private String userId;
    private Uri imageUri;
    private String role; // To store the user's role

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("profile_pictures");

        // Get current user's ID
        userId = mAuth.getCurrentUser().getUid();

        // Initialize Views
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        dobEditText = findViewById(R.id.dobEditText);
        orgNameEditText = findViewById(R.id.orgNameEditText);
        positionEditText = findViewById(R.id.positionEditText);
        saveButton = findViewById(R.id.saveButton);
        profileImageView = findViewById(R.id.profileImageView);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating profile...");
        progressDialog.setCancelable(false);

        // Set input types and filters
        emailEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        phoneNumberEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        phoneNumberEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});


        // Load existing data
        loadUserProfile();

        // Set listeners
        saveButton.setOnClickListener(v -> saveProfile());
        profileImageView.setOnClickListener(v -> openImageSelector());
        dobEditText.setOnClickListener(v -> showDatePickerDialog());
    }

    private void loadUserProfile() {
        progressDialog.show();
        firestore.collection("users").document(userId).get()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Map<String, Object> userData = task.getResult().getData();
                        if (userData != null) {
                            String fullName = (String) userData.get("fullName");
                            String email = (String) userData.get("email");
                            String phoneNumber = (String) userData.get("phoneNumber");
                            String dob = (String) userData.get("dob");
                            String orgName = (String) userData.get("orgName");
                            String position = (String) userData.get("position");
                            String profileImageUrl = (String) userData.get("profileImageUrl");
                            role = (String) userData.get("role"); // Fetch the user's role

                            nameEditText.setText(fullName);
                            emailEditText.setText(email);
                            phoneNumberEditText.setText(phoneNumber);

                            // Handle role-specific visibility
                            if ("participant".equalsIgnoreCase(role)) {
                                orgNameEditText.setVisibility(View.GONE);
                                positionEditText.setVisibility(View.GONE);
                                dobEditText.setVisibility(View.VISIBLE);
                            } else if ("organizer".equalsIgnoreCase(role)) {
                                dobEditText.setVisibility(View.GONE);
                                orgNameEditText.setVisibility(View.VISIBLE);
                                positionEditText.setVisibility(View.VISIBLE);
                            }

                            // Display values if available
                            updateVisibility(dobEditText, dob);
                            updateVisibility(orgNameEditText, orgName);
                            updateVisibility(positionEditText, position);

                            // Load profile picture if available
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Glide.with(this).load(profileImageUrl).circleCrop().into(profileImageView);
                            } else {
                                // Set a default profile picture if none exists
                                Glide.with(this).load(R.drawable.profile_user).circleCrop().into(profileImageView);
                            }
                        }
                    } else {
                        Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                });
    }

    private void updateVisibility(EditText editText, String value) {
        if (value != null && !value.isEmpty()) {
            editText.setText(value);
            editText.setVisibility(View.VISIBLE);
        } else {
            editText.setVisibility(View.GONE);
        }
    }

    private void openImageSelector() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }

    private void saveProfile() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        String dob = dobEditText.getText().toString().trim();
        String orgName = orgNameEditText.getText().toString().trim();
        String position = positionEditText.getText().toString().trim();

        // Validate inputs
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidPhoneNumber(phoneNumber)) {
            Toast.makeText(this, "Phone number must be 10-11 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        if (imageUri != null) {
            uploadImage(name, email, phoneNumber, dob,  orgName, position);
        } else {
            updateUserProfile(name, email, phoneNumber, dob,  orgName, position, null);
        }
    }

    private void uploadImage(String name, String email, String phoneNumber, String dob, String orgName, String position) {
        StorageReference fileRef = storageReference.child(userId + ".jpg");
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> updateUserProfile(name, email, phoneNumber, dob, orgName, position, uri.toString()))
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserProfile(String name, String email, String phoneNumber, String dob, String orgName, String position, String profileImageUrl) {
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("fullName", name);
        updatedData.put("email", email);
        updatedData.put("phoneNumber", phoneNumber);
        updatedData.put("dob", dob);
        updatedData.put("orgName", orgName);
        updatedData.put("position", position);


        if (profileImageUrl != null) {
            updatedData.put("profileImageUrl", profileImageUrl);
        }

        firestore.collection("users").document(userId).update(updatedData)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDatePickerDialog() {
        // Get the current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            // Set the selected date to the dobEditText
            String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
            dobEditText.setText(selectedDate);
        }, year, month, day);

        // Show the DatePickerDialog
        datePickerDialog.show();
    }

    private boolean isValidEmail(CharSequence email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPhoneNumber(CharSequence phoneNumber) {
        return !TextUtils.isEmpty(phoneNumber) && Pattern.matches("\\d{10,11}", phoneNumber);
    }

}