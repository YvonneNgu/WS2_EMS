package com.example.workshop2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize views
        EditText etEmail = findViewById(R.id.etForgotPasswordEmail);
        Button btnSendResetLink = findViewById(R.id.btnSendResetLink);
        TextView tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // Initialize Firebase instances
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Handle the Reset Password button click
        btnSendResetLink.setOnClickListener(v -> {
            // Retrieve the email input
            String email = etEmail.getText().toString().trim();

            // Validate email input
            if (email.isEmpty()) {
                Toast.makeText(ForgotPasswordActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(ForgotPasswordActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            // Query the Firestore database to check if the email exists
            db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (!querySnapshot.isEmpty()) {
                                // Email exists in the database, proceed to send reset email
                                mAuth.sendPasswordResetEmail(email)
                                        .addOnCompleteListener(resetTask -> {
                                            if (resetTask.isSuccessful()) {
                                                Toast.makeText(ForgotPasswordActivity.this, "Password reset email sent!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(ForgotPasswordActivity.this, "Error sending reset email. Please try again.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                // Email not found in the database
                                Toast.makeText(ForgotPasswordActivity.this, "Email not registered in the system. Please check and try again.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Log and display query error
                            Exception exception = task.getException();
                            if (exception != null) {
                                exception.printStackTrace();
                            }
                            Toast.makeText(ForgotPasswordActivity.this, "Error checking email. Please try again later.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Handle the Back to Login TextView click
        tvBackToLogin.setOnClickListener(v -> {
            // Navigate back to MainActivity
            Intent intent = new Intent(ForgotPasswordActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close ForgotPasswordActivity
        });
    }
}
