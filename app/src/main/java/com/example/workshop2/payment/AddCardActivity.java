package com.example.workshop2.payment;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop2.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class AddCardActivity extends AppCompatActivity {

    private EditText editTextCardNumber, editTextExpiryDate;
    private Button buttonAddCard;
    private FirebaseFirestore firestore;

    private String userId; // User ID passed from PaymentActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_add_activity);

        // Get the userId passed from PaymentActivity
        userId = getIntent().getStringExtra("userId");
        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(this, "User ID not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize Views
        editTextCardNumber = findViewById(R.id.editTextCardNumber);
        editTextExpiryDate = findViewById(R.id.editTextExpiryDate);
        buttonAddCard = findViewById(R.id.buttonAddCard);

        // Set up button click listener
        buttonAddCard.setOnClickListener(v -> {
            validateAndAddCard();
        });
    }

    private void validateAndAddCard() {
        String cardNumber = editTextCardNumber.getText().toString().trim();
        String expiryDate = editTextExpiryDate.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(cardNumber)) {
            Toast.makeText(this, "Card number cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cardNumber.length() != 16 || !TextUtils.isDigitsOnly(cardNumber)) {
            Toast.makeText(this, "Card number must be 16 digits.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(expiryDate) || !expiryDate.matches("(0[1-9]|1[0-2])/[0-9]{2}")) {
            Toast.makeText(this, "Invalid expiry date. Use MM/YY format.", Toast.LENGTH_SHORT).show();
            return;
        }

        checkIfCardExists(cardNumber, expiryDate);
    }

    private void checkIfCardExists(String cardNumber, String expiryDate) {
        firestore.collection("card_details")
                .whereEqualTo("CardNumber", cardNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (!task.getResult().isEmpty()) {
                            // Card number already exists
                            Toast.makeText(this, "Card number already exists. Please use a different card.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Card number is unique, proceed to add card
                            addCardToDatabase(cardNumber, expiryDate);
                        }
                    } else {
                        Toast.makeText(this, "Failed to check card number. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addCardToDatabase(String cardNumber, String expiryDate) {
        // Create a HashMap to store card details
        HashMap<String, Object> cardDetails = new HashMap<>();
        cardDetails.put("CardNumber", cardNumber);
        cardDetails.put("ExpiredDate", expiryDate);
        cardDetails.put("Balance", 100.0); // Set balance to 100
        cardDetails.put("userId", userId); // Add the userId to card details
        cardDetails.put("wallet", 0.0); // Initialize wallet with 0.0 balance

        // Add card details to Firestore under the card_details collection
        firestore.collection("card_details")
                .add(cardDetails)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String cardId = task.getResult().getId(); // Get the unique card ID
                        updateCardWithCardId(cardId);
                    } else {
                        Toast.makeText(this, "Failed to add card.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error adding card: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateCardWithCardId(String cardId) {
        // Update the card document with the cardId
        firestore.collection("card_details").document(cardId)
                .update("cardId", cardId)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Card added successfully!", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity
                    } else {
                        Toast.makeText(this, "Failed to update card with card ID.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating card ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
