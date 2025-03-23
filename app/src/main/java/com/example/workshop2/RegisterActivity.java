package com.example.workshop2;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.example.workshop2.model.User;
import com.example.workshop2.organiser.HomeOrganizerFragment;
import com.example.workshop2.participant.HomeParticipantFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private EditText fullNameField, dobField, idCardField, orgNameField, positionField, emailField, passwordField, phoneNumberField;
    private RadioGroup userTypeGroup;
    private RadioButton participantRadioButton, organizerRadioButton;
    private Button registerButton;
    private ConstraintLayout constraintLayout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private CollectionReference usersCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        usersCollection = firestore.collection("users");

        // Initialize Views
        constraintLayout = findViewById(R.id.constraintLayout);
        fullNameField = findViewById(R.id.fullNameField);
        dobField = findViewById(R.id.dobField);
        idCardField = findViewById(R.id.idCardField);
        orgNameField = findViewById(R.id.orgNameField);
        positionField = findViewById(R.id.positionField);
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        phoneNumberField = findViewById(R.id.phoneNumberField);
        userTypeGroup = findViewById(R.id.userTypeGroup);
        participantRadioButton = findViewById(R.id.participantRadioButton);
        organizerRadioButton = findViewById(R.id.organizerRadioButton);
        registerButton = findViewById(R.id.registerButton);

        // Set up DatePickerDialog for dobField
        dobField.setOnClickListener(v -> showDatePickerDialog());

        // Set input filter for idCardField to allow only 12 digits
        idCardField.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(12), // Ensure max length of 12 characters
                new InputFilter() { // Custom filter to allow only digits
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        // Ensure only digits are input
                        for (int i = start; i < end; i++) {
                            if (!Character.isDigit(source.charAt(i))) {
                                return "";
                            }
                        }
                        return null;
                    }
                }
        });
        idCardField.setInputType(InputType.TYPE_CLASS_NUMBER);

        // Set input filter for phoneNumberField to allow only 10-11 digits
        phoneNumberField.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(11), // Max length of 11 digits
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        for (int i = start; i < end; i++) {
                            if (!Character.isDigit(source.charAt(i))) {
                                return "";
                            }
                        }
                        return null;
                    }
                }
        });

        // Set up the RadioGroup listener to toggle visibility of specific fields
        userTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Initially hide all dynamic fields
            dobField.setVisibility(View.GONE);
            idCardField.setVisibility(View.GONE);
            orgNameField.setVisibility(View.GONE);
            positionField.setVisibility(View.GONE);

            // Adjust visibility based on selected user type
            if (checkedId == R.id.participantRadioButton) {
                dobField.setVisibility(View.VISIBLE);
                idCardField.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.organizerRadioButton) {
                orgNameField.setVisibility(View.VISIBLE);
                positionField.setVisibility(View.VISIBLE);
            }
        });

        // Set up the register button click listener
        registerButton.setOnClickListener(v -> registerUser());
    }

    // Show DatePickerDialog for dobField
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                RegisterActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    dobField.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    // Register user with Firebase Authentication and Firestore
    private void registerUser() {
        String fullName = fullNameField.getText().toString().trim();
        String dob = dobField.getText().toString().trim();
        String idCard = idCardField.getText().toString().trim();
        String orgName = orgNameField.getText().toString().trim();
        String position = positionField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String phoneNumber = phoneNumberField.getText().toString().trim();

        // Validate fields
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(RegisterActivity.this, "Please enter a valid email.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidPhoneNumber(phoneNumber)) {
            Toast.makeText(RegisterActivity.this, "Please enter a valid phone number (10-11 digits).", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (participantRadioButton.isChecked() && (!isValidIdCard(idCard) || dob.isEmpty())) {
            Toast.makeText(RegisterActivity.this, "Please provide a valid ID card and date of birth.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (organizerRadioButton.isChecked() && (orgName.isEmpty() || position.isEmpty())) {
            Toast.makeText(RegisterActivity.this, "Please provide organization name and position.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            User user = createUserObject(fullName, dob, idCard, orgName, position, email, userId, phoneNumber);
                            saveUserDataToFirestore(user);
                        }
                    } else {
                        Log.e("RegisterActivity", "User creation failed", task.getException());
                        handleRegistrationError(task.getException());
                    }
                });
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return !TextUtils.isEmpty(phoneNumber) && phoneNumber.length() >= 10 && phoneNumber.length() <= 11 && phoneNumber.matches("\\d+");
    }

    private boolean isValidIdCard(String idCard) {
        return !TextUtils.isEmpty(idCard) && idCard.length() == 12 && idCard.matches("\\d{12}");
    }

    private User createUserObject(String fullName, String dob, String idCard, String orgName, String position, String email, String userId, String phoneNumber) {
        User user = null;

        if (participantRadioButton.isChecked()) {
            user = new User(fullName, dob, idCard, "", "", email, userId, "participant", phoneNumber);
        } else if (organizerRadioButton.isChecked()) {
            user = new User(fullName, null, null, orgName, position, email, userId, "organizer", phoneNumber);
        }

        return user;
    }

    private void saveUserDataToFirestore(User user) {
        usersCollection.document(user.getUserId()).set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("RegisterActivity", "User data saved successfully.");
                        Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                        navigateToDashboard(user);
                    } else {
                        Log.e("RegisterActivity", "Error saving data", task.getException());
                        Toast.makeText(RegisterActivity.this, "Failed to save data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToDashboard(User user) {
        Intent intent;
        if (user.userType.equals("participant")) {
            intent = new Intent(RegisterActivity.this, HomeParticipantFragment.class);
        } else {
            intent = new Intent(RegisterActivity.this, HomeOrganizerFragment.class);
        }
        startActivity(intent);
        finish();
    }

    private void handleRegistrationError(Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            Toast.makeText(RegisterActivity.this, "Email already in use. Try a different email.", Toast.LENGTH_SHORT).show();
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(RegisterActivity.this, "Invalid email format.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(RegisterActivity.this, "Registration Failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
