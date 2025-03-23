package com.example.workshop2.cert;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop2.R;
import com.example.workshop2.cert.model.GeneratedCert;
import com.github.barteksc.pdfviewer.PDFView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CertAdapter extends RecyclerView.Adapter<CertAdapter.ViewHolder> {
    private List<GeneratedCert> certificates = new ArrayList<>();
    private final CertificateCallbacks callbacks;
    private final String participantId;
    private final CertViewModel viewModel;

    public CertAdapter(CertificateCallbacks callbacks, String participantId, CertViewModel viewModel) {
        this.callbacks = callbacks;
        this.participantId = participantId;
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cert, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(certificates.get(position));
    }

    @Override
    public int getItemCount() {
        return certificates.size();
    }

    public void setCertificates(List<GeneratedCert> newCertificates) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return certificates.size();
            }

            @Override
            public int getNewListSize() {
                return newCertificates.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return certificates.get(oldItemPosition).getId()
                        .equals(newCertificates.get(newItemPosition).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return certificates.get(oldItemPosition)
                        .equals(newCertificates.get(newItemPosition));
            }
        });

        certificates = new ArrayList<>(newCertificates);
        diffResult.dispatchUpdatesTo(this);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEventName;
        private final TextView tvCertType;
        private final TextView tvIssueDate;
        private final ImageView imageView;
        private final ProgressBar previewProgress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvCertType = itemView.findViewById(R.id.tvCertType);
            tvIssueDate = itemView.findViewById(R.id.tvIssueDate);
            imageView = itemView.findViewById(R.id.imageView);
            previewProgress = itemView.findViewById(R.id.previewProgress);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    callbacks.onCertificateClick(certificates.get(position));
                }
            });
        }

        void bind(GeneratedCert certificate) {
            String eventName = viewModel.getEventName(certificate.getEventId());
            tvEventName.setText(eventName);
            tvCertType.setText(certificate.getCertificateType());

            // Format timestamp to string
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
            String issueDateStr = certificate.getIssueDate() != null ?
                    sdf.format(certificate.getIssueDate().toDate()) : "N/A";
            tvIssueDate.setText(issueDateStr);

            // Load image preview
            callbacks.loadImagePreview(
                    participantId,
                    certificate.getId(),
                    imageView,
                    previewProgress
            );
        }
    }
}
interface CertificateCallbacks {
    void onCertificateClick(GeneratedCert certificate);
    void loadImagePreview(String participantId, String certificateId, ImageView imageView, ProgressBar progressBar);

}