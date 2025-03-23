package com.example.workshop2.payment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

public class WalletManager {

    private final CollectionReference cardCollection;

    public WalletManager(CollectionReference cardCollection) {
        this.cardCollection = cardCollection;
    }

    public void topUp(String cardNumber, double amount, WalletCallback callback) {
        if (amount <= 0) {
            callback.onFailure("Top-up amount must be positive.");
            return;
        }

        DocumentReference cardRef = cardCollection.document(cardNumber);
        cardRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Double currentBalance = task.getResult().getDouble("balance");
                if (currentBalance == null) currentBalance = 0.0;

                cardRef.update("balance", currentBalance + amount).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        callback.onSuccess("Top-up successful!");
                    } else {
                        callback.onFailure("Failed to update balance.");
                    }
                });
            } else {
                callback.onFailure("Card not found.");
            }
        });
    }

    public interface WalletCallback {
        void onSuccess(String message);

        void onFailure(String error);
    }
}
