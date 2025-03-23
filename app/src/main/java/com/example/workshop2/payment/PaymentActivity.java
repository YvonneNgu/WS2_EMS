package com.example.workshop2.payment;

import android.animation.Animator;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.workshop2.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class PaymentActivity extends AppCompatActivity {

    private RecyclerView cardRecyclerView;
    private CardAdapter cardAdapter;
    private ArrayList<CardDetail> cardDetails;

    private FirebaseFirestore firestore;
    private String userId;

    private TextView walletBalanceView;
    private LottieAnimationView successAnimation;
    private LottieAnimationView failureAnimation;
    private EditText topUpAmountEditText;
    private Button topUpButton;
    private ImageButton addCardButton;
    private ImageButton removeCardButton;

    private double walletBalance = 0.0;
    private CardDetail selectedCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment);

        userId = getIntent().getStringExtra("userId");

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firestore = FirebaseFirestore.getInstance();

        walletBalanceView = findViewById(R.id.balance_view);
        successAnimation = findViewById(R.id.success_animation);
        failureAnimation = findViewById(R.id.fail_animation);
        cardRecyclerView = findViewById(R.id.card_recycler_view);
        topUpAmountEditText = findViewById(R.id.top_up_amount);
        topUpButton = findViewById(R.id.button_top_up);
        addCardButton = findViewById(R.id.addCardButton);
        removeCardButton = findViewById(R.id.removeCardButton);

        cardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cardDetails = new ArrayList<>();

        fetchWalletBalance();
        fetchUserCardDetails();

        topUpButton.setOnClickListener(v -> {
            if (selectedCard == null) {
                Toast.makeText(this, "Please select a card first.", Toast.LENGTH_SHORT).show();
                return;
            }

            String amountText = topUpAmountEditText.getText().toString().trim();
            if (amountText.isEmpty()) {
                Toast.makeText(this, "Please enter an amount to top up.", Toast.LENGTH_SHORT).show();
                return;
            }

            double topUpAmount = Double.parseDouble(amountText);
            if (topUpAmount <= 0) {
                Toast.makeText(this, "Amount must be greater than zero.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedCard.getBalance() < topUpAmount) {
                Toast.makeText(this, "Insufficient card balance.", Toast.LENGTH_SHORT).show();
                return;
            }

            processTopUp(topUpAmount);
        });

        addCardButton.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentActivity.this, AddCardActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        removeCardButton.setOnClickListener(v -> {
            if (selectedCard == null) {
                Toast.makeText(this, "Please select a card to remove.", Toast.LENGTH_SHORT).show();
                return;
            }

            removeCard(selectedCard.getCardId());
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchWalletBalance();
        fetchUserCardDetails();
    }

    private void fetchWalletBalance() {
        firestore.collection("card_details")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        walletBalance = querySnapshot.getDocuments().get(0).getDouble("wallet");
                        updateWalletBalanceView();
                    } else {
                        Toast.makeText(this, "No wallet data found for this user.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch wallet balance.", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchUserCardDetails() {
        firestore.collection("card_details")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        cardDetails.clear();

                        for (var document : task.getResult()) {
                            String cardNumber = document.getString("CardNumber");
                            Double balance = document.getDouble("Balance");
                            String expiryDate = document.getString("ExpiredDate");
                            String cardId = document.getId();

                            if (cardNumber != null && balance != null && expiryDate != null) {
                                CardDetail cardDetail = new CardDetail(cardId, cardNumber, balance, expiryDate);
                                cardDetails.add(cardDetail);
                            }
                        }
                        setupCardAdapter();
                    } else {
                        playFailureAnimation("Failed to fetch card details.");
                    }
                });
    }

    private void setupCardAdapter() {
        cardAdapter = new CardAdapter(this, cardDetails, cardDetail -> {
            selectedCard = cardDetail;
            Toast.makeText(this, "Selected Card: " + cardDetail.getCardNumber(), Toast.LENGTH_SHORT).show();
        });
        cardRecyclerView.setAdapter(cardAdapter);
    }

    private void processTopUp(double topUpAmount) {
        double newCardBalance = selectedCard.getBalance() - topUpAmount;
        double newWalletBalance = walletBalance + topUpAmount;

        firestore.collection("card_details").document(selectedCard.getCardId())
                .update("Balance", newCardBalance)
                .addOnSuccessListener(aVoid -> {
                    firestore.collection("card_details")
                            .whereEqualTo("userId", userId)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    String walletId = querySnapshot.getDocuments().get(0).getId();
                                    firestore.collection("card_details").document(walletId)
                                            .update("wallet", newWalletBalance)
                                            .addOnSuccessListener(aVoid1 -> {
                                                walletBalance = newWalletBalance;
                                                selectedCard.setBalance(newCardBalance);
                                                updateWalletBalanceView();
                                                setupCardAdapter();
                                                playSuccessAnimation("Top-up successful!");
                                            })
                                            .addOnFailureListener(e -> playFailureAnimation("Failed to update wallet balance."));
                                }
                            })
                            .addOnFailureListener(e -> playFailureAnimation("Failed to fetch wallet data."));
                })
                .addOnFailureListener(e -> playFailureAnimation("Failed to update card balance."));
    }

    private void removeCard(String cardId) {
        // Display a confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Remove Card")
                .setMessage("Are you sure you want to remove this card?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Proceed to delete the card
                    firestore.collection("card_details").document(cardId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                cardDetails.removeIf(cardDetail -> cardDetail.getCardId().equals(cardId));
                                cardAdapter.notifyDataSetChanged();
                                Toast.makeText(this, "Card removed successfully.", Toast.LENGTH_SHORT).show();
                                selectedCard = null;
                            })
                            .addOnFailureListener(e -> playFailureAnimation("Failed to remove card."));
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // User canceled the dialog
                    dialog.dismiss();
                })
                .show();
    }


    private void updateWalletBalanceView() {
        walletBalanceView.setText("Wallet Balance: RM" + String.format("%.2f", walletBalance));
    }

    private void playSuccessAnimation(String message) {
        runOnUiThread(() -> {
            successAnimation.setVisibility(View.VISIBLE);
            successAnimation.playAnimation();
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        successAnimation.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                successAnimation.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
    }

    private void playFailureAnimation(String message) {
        runOnUiThread(() -> {
            failureAnimation.setVisibility(View.VISIBLE);
            failureAnimation.playAnimation();
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        failureAnimation.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                failureAnimation.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
    }
}
