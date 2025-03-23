package com.example.workshop2.payment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop2.R;

import java.util.ArrayList;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private Context context;
    private ArrayList<CardDetail> cardDetails;
    private OnCardClickListener onCardClickListener;

    public CardAdapter(Context context, ArrayList<CardDetail> cardDetails, OnCardClickListener onCardClickListener) {
        this.context = context;
        this.cardDetails = cardDetails;
        this.onCardClickListener = onCardClickListener;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_details_item, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        CardDetail cardDetail = cardDetails.get(position);

        // Get the last 4 digits of the card number
        String cardNumber = cardDetail.getCardNumber();
        String lastFourDigits = cardNumber.substring(cardNumber.length() - 4);

        // Format the card number as **** **** **** 1234
        String formattedCardNumber = "**** **** **** " + lastFourDigits;

        // Set the formatted card number
        holder.cardNumber.setText(formattedCardNumber);
        holder.expiryDate.setText(cardDetail.getExpiryDate());

        // Set text size and font for both TextViews
        holder.cardNumber.setTextSize(10);  // Set text size to 12sp
        holder.expiryDate.setTextSize(7);   // Set text size to 7sp

        holder.itemView.setOnClickListener(v -> onCardClickListener.onCardClick(cardDetail));
    }

    @Override
    public int getItemCount() {
        return cardDetails.size();
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView cardNumber, balance, expiryDate;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardNumber = itemView.findViewById(R.id.card_number);
            expiryDate = itemView.findViewById(R.id.card_expiry_date);
        }
    }

    public interface OnCardClickListener {
        void onCardClick(CardDetail cardDetail);
    }
}