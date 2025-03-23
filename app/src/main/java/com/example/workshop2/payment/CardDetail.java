package com.example.workshop2.payment;

public class CardDetail {
    private String cardId;
    private String cardNumber;
    private double balance;
    private String expiryDate;
    private int cvv;
    private double wallet;

    public CardDetail() {
        // Required empty constructor for Firestore
    }

    public CardDetail(String cardId, String cardNumber, double balance, String expiryDate) {
        this.cardId = cardId;
        this.cardNumber = cardNumber;
        this.balance = balance;
        this.expiryDate = expiryDate;
        this.wallet = 0.0; // Initialize wallet with default value
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }


    public double getWallet() {
        return wallet;
    }

    public void setWallet(double wallet) {
        this.wallet = wallet;
    }
}
