package com.example.workshop2.cert;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop2.R;
import com.example.workshop2.cert.model.Participant;

import java.util.List;

public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ParticipantViewHolder> {

    private List<Participant> participants;

    public ParticipantAdapter(List<Participant> participants) {
        this.participants = participants;
    }

    @NonNull
    @Override
    public ParticipantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_participant, parent, false);
        return new ParticipantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantViewHolder holder, int position) {
        Participant participant = participants.get(position);
        holder.bind(participant);
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    public void updateList(List<Participant> newList) {
        participants = newList;
        notifyDataSetChanged();
    }

    static class ParticipantViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName;
        ImageView ivParticipationCert, ivAchievementCert;
        CheckBox cbSelect;

        ParticipantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            ivParticipationCert = itemView.findViewById(R.id.ivParticipationCert);
            ivAchievementCert = itemView.findViewById(R.id.ivAchievementCert);
            cbSelect = itemView.findViewById(R.id.cbSelect);
        }

        void bind(Participant participant) {
            tvFullName.setText(participant.getFullName());

            // Set participation certificate icon
            ivParticipationCert.setImageResource(participant.hasParticipationCert() ?
                    R.drawable.participation_icon : R.drawable.participation_gray_icon);

            // Set achievement certificate icon
            ivAchievementCert.setImageResource(participant.hasAchievementCert() ?
                    R.drawable.achievement_icon : R.drawable.achievement_gray_icon);

            // Set checkbox state
            cbSelect.setChecked(participant.isSelected());

            // Set checkbox listener
            cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                participant.setSelected(isChecked);
            });

            // Set full name tooltip
            tvFullName.setOnLongClickListener(v -> {
                Toast.makeText(v.getContext(), participant.getFullName(), Toast.LENGTH_SHORT).show();
                return true;
            });
        }
    }
}

