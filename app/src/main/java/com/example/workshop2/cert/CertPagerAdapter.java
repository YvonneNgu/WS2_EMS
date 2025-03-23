package com.example.workshop2.cert;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop2.R;
import com.example.workshop2.cert.model.GeneratedCert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CertPagerAdapter extends RecyclerView.Adapter<CertPagerAdapter.CertificatePageViewHolder> {

    private Context context;
    private List<String> tabTitles;
    private Map<String, List<GeneratedCert>> certificatesByType;

    public CertPagerAdapter(Context context, Map<String, List<GeneratedCert>> certificatesByType, List<String> tabTitles) {
        this.context = context;
        this.certificatesByType = certificatesByType;
        this.tabTitles = tabTitles;
    }

    @NonNull
    @Override
    public CertificatePageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cert_page, parent, false);
        return new CertificatePageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CertificatePageViewHolder holder, int position) {
        if (position == tabTitles.size() - 1) {
            // This is the "+" tab
            holder.recyclerView.setVisibility(View.GONE);
            holder.tvNoCertificates.setVisibility(View.VISIBLE);
            holder.btnGenerate.setVisibility(View.VISIBLE);

            holder.btnGenerate.setOnClickListener(v -> {
                if (context instanceof CertMgmtActivity) {
                    ((CertMgmtActivity) context).showCertificateTypeDialog();
                }
            });
        } else {
            // Regular certificate type tab
            String certificateType = tabTitles.get(position);
            List<GeneratedCert> certificates = certificatesByType.getOrDefault(certificateType, new ArrayList<>());

            holder.recyclerView.setVisibility(View.VISIBLE);
            holder.tvNoCertificates.setVisibility(View.GONE);
            holder.btnGenerate.setVisibility(View.GONE);

            CertificateListAdapter adapter = new CertificateListAdapter(certificates, certificate -> {
                Intent intent = new Intent(context, ViewCertActivity.class);
                intent.putExtra("certificateId", certificate.getId());
                context.startActivity(intent);
            });

            holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
            holder.recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public int getItemCount() {
        return tabTitles.size();
    }

    public String getTabTitle(int position) {
        return tabTitles.get(position);
    }

    static class CertificatePageViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerView;
        TextView tvNoCertificates;
        Button btnGenerate;

        CertificatePageViewHolder(View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.rvCertificates);
            tvNoCertificates = itemView.findViewById(R.id.tvNoCertificates);
            btnGenerate = itemView.findViewById(R.id.btnSaveDesign);
        }
    }

    private static class CertificateListAdapter extends RecyclerView.Adapter<CertificateListAdapter.CertificateViewHolder> {

        private List<GeneratedCert> certificates;
        private OnCertificateClickListener listener;

        CertificateListAdapter(List<GeneratedCert> certificates, OnCertificateClickListener listener) {
            this.certificates = certificates;
            this.listener = listener;
        }

        @NonNull
        @Override
        public CertificateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new CertificateViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CertificateViewHolder holder, int position) {
            GeneratedCert certificate = certificates.get(position);
            holder.textView.setText(certificate.getParticipantName());
            holder.itemView.setOnClickListener(v -> listener.onCertificateClick(certificate));
        }

        @Override
        public int getItemCount() {
            return certificates.size();
        }

        static class CertificateViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            CertificateViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }

        interface OnCertificateClickListener {
            void onCertificateClick(GeneratedCert certificate);
        }
    }
}