package com.example.workshop2.cert;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop2.R;
import com.example.workshop2.cert.model.ParticipantAchievement;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class ParticipantAchievementAdapter extends RecyclerView.Adapter<ParticipantAchievementAdapter.ViewHolder> {

    private List<ParticipantAchievement> participants;
    private Runnable onSelectionChangedListener;

    public ParticipantAchievementAdapter(List<ParticipantAchievement> participants, Runnable onSelectionChangedListener) {
        this.participants = participants;
        this.onSelectionChangedListener = onSelectionChangedListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_participant_achievement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParticipantAchievement participant = participants.get(position);
        holder.bind(participant);
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private CheckBox cbSelect;
        private TextView tvParticipantName;
        private TextInputEditText etAchievement;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelect = itemView.findViewById(R.id.cbSelect);
            tvParticipantName = itemView.findViewById(R.id.tvParticipantName);
            etAchievement = itemView.findViewById(R.id.etAchievement);
        }

        void bind(ParticipantAchievement participant) {
            tvParticipantName.setText(participant.getName());
            etAchievement.setText(participant.getAchievement());

            // Remove any existing TextWatcher to prevent duplicate listeners
            if (etAchievement.getTag() instanceof android.text.TextWatcher) {
                etAchievement.removeTextChangedListener((android.text.TextWatcher) etAchievement.getTag());
            }

            // Add new TextWatcher
            android.text.TextWatcher textWatcher = new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    participant.setAchievement(s.toString());
                }
            };

            // Save the TextWatcher as a tag to remove it later
            etAchievement.setTag(textWatcher);
            etAchievement.addTextChangedListener(textWatcher);

            cbSelect.setChecked(participant.isSelected());
            cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                participant.setSelected(isChecked);
                onSelectionChangedListener.run();
            });
        }
    }
}

