package com.example.workshop2.cert;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop2.R;
import com.example.workshop2.cert.model.CertTpl;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.List;

public class CertTplAdapter extends RecyclerView.Adapter<CertTplAdapter.ViewHolder> {
    private List<CertTpl> templates;
    private Context context;
    private OnTemplateActionListener listener;

    public interface OnTemplateActionListener {
        void onFileSelect(int position);
        void onRemoveItem(int position);
        void onChangeFile(int position);
        void onAddNewItem();
    }

    public CertTplAdapter(Context context, List<CertTpl> templates, OnTemplateActionListener listener) {
        this.context = context;
        this.templates = templates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cert_tpl_upload, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CertTpl template = templates.get(position);

        // Set up template name
        holder.etTplName.setText(template.getTemplateName());
        holder.etTplName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                template.setTemplateName(s.toString());
            }
        });

        // Set up certificate type spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.certificate_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spCertType.setAdapter(adapter);

        // Set selected certificate type
        int spinnerPosition = adapter.getPosition(template.getCertificateType());
        holder.spCertType.setSelection(spinnerPosition);

        holder.spCertType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                template.setCertificateType(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Show/hide appropriate views based on file selection
        if (template.getFileContent() != null) {
            holder.flFileSelection.setVisibility(View.GONE);
            holder.llPreviewArea.setVisibility(View.VISIBLE);
            holder.tvFileName.setText(template.getFileName());

            // Show appropriate preview
            String fileType = template.getFileType();
            if (fileType != null && (fileType.startsWith("image/") || fileType.contains("image"))) {
                holder.ivTplPreview.setVisibility(View.VISIBLE);
                holder.pdfvTplPreview.setVisibility(View.GONE);

                // Load and scale image from Base64 string
                byte[] decodedString = android.util.Base64.decode(template.getFileContent(), android.util.Base64.DEFAULT);
                android.graphics.Bitmap originalBitmap = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                // Scale bitmap to fit preview area while maintaining aspect ratio
                int maxWidth = context.getResources().getDisplayMetrics().widthPixels - 32; // 16dp padding on each side
                int maxHeight = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.3); // 30% of screen height

                float scale = Math.min(
                        (float) maxWidth / originalBitmap.getWidth(),
                        (float) maxHeight / originalBitmap.getHeight()
                );

                int scaledWidth = (int) (originalBitmap.getWidth() * scale);
                int scaledHeight = (int) (originalBitmap.getHeight() * scale);

                android.graphics.Bitmap scaledBitmap = android.graphics.Bitmap.createScaledBitmap(
                        originalBitmap, scaledWidth, scaledHeight, true);

                holder.ivTplPreview.setImageBitmap(scaledBitmap);
                holder.ivTplPreview.setScaleType(ImageView.ScaleType.FIT_CENTER);

            } else if (fileType != null && (fileType.equals("application/pdf") || fileType.contains("pdf"))) {
                holder.ivTplPreview.setVisibility(View.GONE);
                holder.pdfvTplPreview.setVisibility(View.VISIBLE);

                // Load PDF from Base64 string
                byte[] decodedString = android.util.Base64.decode(template.getFileContent(), android.util.Base64.DEFAULT);
                holder.pdfvTplPreview.fromBytes(decodedString)
                        .enableSwipe(true)
                        .swipeHorizontal(false)
                        .enableDoubletap(true)
                        .defaultPage(0)
                        .spacing(0)
                        .autoSpacing(false)
                        .pageFitPolicy(com.github.barteksc.pdfviewer.util.FitPolicy.BOTH)
                        .pageSnap(true)
                        .pageFling(true)
                        .load();
            }
        } else {
            holder.flFileSelection.setVisibility(View.VISIBLE);
            holder.llPreviewArea.setVisibility(View.GONE);
        }

        // Set click listeners
        holder.ivSelectFile.setOnClickListener(v -> listener.onFileSelect(position));
        holder.ibRemoveItem.setOnClickListener(v -> listener.onRemoveItem(position));
        holder.ibChangeFile.setOnClickListener(v -> listener.onChangeFile(position));
    }

    @Override
    public int getItemCount() {
        return templates.size();
    }

    public void addTemplate() {
        templates.add(new CertTpl());
        notifyItemInserted(templates.size() - 1);
    }

    public void removeTemplate(int position) {
        if (templates.size() > 1) {
            templates.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, templates.size());
        } else {
            // Reset the only item instead of removing
            templates.remove(position);
            templates.add(new CertTpl());
            notifyItemChanged(0);
        }
    }

    public List<CertTpl> getTemplates() {
        return templates;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        EditText etTplName;
        Spinner spCertType;
        FrameLayout flFileSelection;
        ImageView ivSelectFile;
        LinearLayout llPreviewArea;
        ImageView ivTplPreview;
        PDFView pdfvTplPreview;
        TextView tvFileName;
        ImageButton ibRemoveItem;
        ImageButton ibChangeFile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            etTplName = itemView.findViewById(R.id.etTplName);
            spCertType = itemView.findViewById(R.id.spCertType);
            flFileSelection = itemView.findViewById(R.id.flFileSelection);
            ivSelectFile = itemView.findViewById(R.id.ivSelectFile);
            llPreviewArea = itemView.findViewById(R.id.llPreviewArea);
            ivTplPreview = itemView.findViewById(R.id.ivTplPreview);
            pdfvTplPreview = itemView.findViewById(R.id.pdfvTplPreview);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            ibRemoveItem = itemView.findViewById(R.id.ibRemoveItem);
            ibChangeFile = itemView.findViewById(R.id.ibChangeFile);
        }
    }
}