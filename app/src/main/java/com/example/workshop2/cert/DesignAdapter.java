package com.example.workshop2.cert;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop2.R;
import com.example.workshop2.cert.model.Design;

import java.util.ArrayList;
import java.util.List;

// Adapter for displaying designs in dialog
public class DesignAdapter extends RecyclerView.Adapter<DesignAdapter.ViewHolder> {
    private List<Design> designs = new ArrayList<>();
    private final OnDesignSelectedListener listener;

    interface OnDesignSelectedListener {
        void onDesignSelected(Design design);
    }

    DesignAdapter(OnDesignSelectedListener listener) {
        this.listener = listener;
    }

    void setDesigns(List<Design> designs) {
        this.designs = designs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_design, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Design design = designs.get(position);
        holder.tvDesignName.setText(design.getDesignName());
        holder.itemView.setOnClickListener(v -> listener.onDesignSelected(design));
    }

    @Override
    public int getItemCount() {
        return designs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDesignName;
        ImageView ivDesignPreview;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDesignName = itemView.findViewById(R.id.tvDesignName);
            ivDesignPreview = itemView.findViewById(R.id.ivDesignPreview);
        }
    }
}
