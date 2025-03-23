package com.example.workshop2.cert;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop2.R;
import com.example.workshop2.cert.model.CertificateItem;
import com.example.workshop2.cert.model.Design;
import com.example.workshop2.cert.model.ParticipantAchievement;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CertGenerator {
    private String eventId;
    private String eventName;
    private String certificateType;
    private Context context;
    private CertificateItemController certificateItemController = new CertificateItemController();
    private  ProgressBar progressBar;
    private int totalCertificates;
    private int savedCertificates;

    public CertGenerator(Context context, String eventId, String eventName, String certificateType, ProgressBar progressBar){
        this.context = context;
        this.eventId = eventId;
        this.eventName = eventName;
        this.certificateType = certificateType;
        this.progressBar = progressBar;
    }

    // Show dialog for selecting certificate design
    public void showDesignSelectionDialog(List<ParticipantAchievement> participantAchievements) {
        // Create dialog view
        View dialogView = LayoutInflater.from(context).inflate(R.layout.activity_design_mgmt, null);
        RecyclerView rvDesigns = dialogView.findViewById(R.id.rvDesigns);
        TextView tvNoDesigns = dialogView.findViewById(R.id.tvNoDesigns);

        // Hide unnecessary views
        dialogView.findViewById(R.id.menuDesignMgmt).setVisibility(View.GONE);
        dialogView.findViewById(R.id.btnBack).setVisibility(View.GONE);
        dialogView.findViewById(R.id.btnNewDesign).setVisibility(View.GONE);

        // Create and configure dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Select a Design")
                .setView(dialogView)
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();

        // Set up RecyclerView
        rvDesigns.setLayoutManager(new LinearLayoutManager(context));
        DesignAdapter adapter = new DesignAdapter((design) -> {
            // Handle design selection
            generateCertificates(design, participantAchievements);
            dialog.dismiss();
        });
        rvDesigns.setAdapter(adapter);

        // Load designs from database
        loadDesigns(adapter, tvNoDesigns, rvDesigns);

        // Show dialog and set dimensions
        dialog.show();

        // Set dialog dimensions
        int displayWidth = context.getResources().getDisplayMetrics().widthPixels;
        int displayHeight = context.getResources().getDisplayMetrics().heightPixels;
        int dialogWidth = (int) (displayWidth * 0.9); // 90% of screen width
        int dialogHeight = (int) (displayHeight * 0.8); // 80% of screen height

        dialog.getWindow().setLayout(dialogWidth, dialogHeight);
    }

    public void showDesignSelectionDialog(Map<String,String> participantIdNames) {
        // Create dialog view
        View dialogView = LayoutInflater.from(context).inflate(R.layout.activity_design_mgmt, null);
        RecyclerView rvDesigns = dialogView.findViewById(R.id.rvDesigns);
        TextView tvNoDesigns = dialogView.findViewById(R.id.tvNoDesigns);

        // Hide unnecessary views
        dialogView.findViewById(R.id.menuDesignMgmt).setVisibility(View.GONE);
        dialogView.findViewById(R.id.btnBack).setVisibility(View.GONE);
        dialogView.findViewById(R.id.btnNewDesign).setVisibility(View.GONE);

        // Create and configure dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Select a Design")
                .setView(dialogView)
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();

        // Set up RecyclerView
        rvDesigns.setLayoutManager(new LinearLayoutManager(context));
        DesignAdapter adapter = new DesignAdapter((design) -> {
            // Handle design selection
            generateCertificates(design, participantIdNames);
            dialog.dismiss();
        });
        rvDesigns.setAdapter(adapter);

        // Load designs from database
        loadDesigns(adapter, tvNoDesigns, rvDesigns);

        // Show dialog and set dimensions
        dialog.show();

        // Set dialog dimensions
        int displayWidth = context.getResources().getDisplayMetrics().widthPixels;
        int displayHeight = context.getResources().getDisplayMetrics().heightPixels;
        int dialogWidth = (int) (displayWidth * 0.9); // 90% of screen width
        int dialogHeight = (int) (displayHeight * 0.8); // 80% of screen height

        dialog.getWindow().setLayout(dialogWidth, dialogHeight);
    }

    // Load certificate designs from Firestore
    private void loadDesigns(DesignAdapter adapter, TextView tvNoDesigns, RecyclerView rvDesigns) {
        FirebaseFirestore.getInstance()
                .collection("certificateDesigns")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("certificateType", certificateType)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Design> designs = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Design design = new Design(
                                document.getId(),
                                document.getString("designName"),
                                document.getString("templateId"),
                                (List<Map<String, Object>>) document.get("certificateItems")
                        );
                        designs.add(design);
                    }

                    if (designs.isEmpty()) {
                        tvNoDesigns.setVisibility(View.VISIBLE);
                        rvDesigns.setVisibility(View.GONE);
                    } else {
                        tvNoDesigns.setVisibility(View.GONE);
                        rvDesigns.setVisibility(View.VISIBLE);
                        adapter.setDesigns(designs);
                    }
                });
    }

    // Generate certificates using selected design and participant achievements
    private void generateCertificates(Design design, Map<String, String> participantIdName) {
        totalCertificates = participantIdName.size();
        savedCertificates = 0;
        progressBar.setVisibility(View.VISIBLE);
        // Get template data
        FirebaseFirestore.getInstance()
                .collection("certificateTemplates")
                .document(design.getTemplateId())
                .get()
                .addOnSuccessListener(templateDoc -> {
                    if (templateDoc.exists()) {
                        String templateContent = templateDoc.getString("fileContent");

                        // Template/background
                        byte[] decodedString = Base64.decode(templateContent, Base64.DEFAULT);
                        Bitmap templateBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        certificateItemController.setTemplateDimensions(templateBitmap.getWidth(),templateBitmap.getHeight() );
                        // Generate certificate for each participant
                        for (Map.Entry<String, String> entry : participantIdName.entrySet()) {
                            Bitmap certificateBitmap = templateBitmap.copy(Bitmap.Config.ARGB_8888, true);
                            Canvas canvas = new Canvas(certificateBitmap);

                            String participantId = entry.getKey();
                            String participantName = entry.getValue();

                            // Certificate items/text
                            List<Map<String, Object>> items = design.getCertificateItems();
                            for (Map<String, Object> item : items) {

                                String itemTitle = (String) item.get("title");
                                Log.d("check item title", itemTitle);
                                // Replace specific fields with participant data
                                switch (itemTitle) {
                                    case "Recipient Name":
                                        item.put("text", participantName);
                                        break;
                                    case "Issue Date":
                                        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
                                        item.put("text", "Issued on " + sdf.format(new Date()));
                                        break;
                                }
                                drawCertificateItem(canvas, item);
                            }

                            // Save generated certificate to database
                            saveCertificate(participantId, bitmapToBase64(certificateBitmap), participantName);
                        }
                    }
                });
    }

    private void generateCertificates(Design design, List<ParticipantAchievement> participantAchievements) {
        totalCertificates = participantAchievements.size();
        savedCertificates = 0;
        progressBar.setVisibility(View.VISIBLE);
        // Get template data
        FirebaseFirestore.getInstance()
                .collection("certificateTemplates")
                .document(design.getTemplateId())
                .get()
                .addOnSuccessListener(templateDoc -> {
                    if (templateDoc.exists()) {
                        String templateContent = templateDoc.getString("fileContent");

                        // Template/background
                        byte[] decodedString = Base64.decode(templateContent, Base64.DEFAULT);
                        Bitmap templateBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        certificateItemController.setTemplateDimensions(templateBitmap.getWidth(),templateBitmap.getHeight() );
                        // Generate certificate for each participant
                        for (ParticipantAchievement participantAchievement : participantAchievements) {
                            Bitmap certificateBitmap = templateBitmap.copy(Bitmap.Config.ARGB_8888, true);
                            Canvas canvas = new Canvas(certificateBitmap);

                            // Certificate items/text
                            List<Map<String, Object>> items = design.getCertificateItems();
                            for (Map<String, Object> item : items) {

                                String itemTitle = (String) item.get("title");
                                Log.d("check item title", itemTitle);
                                // Replace specific fields with participant data
                                switch (itemTitle) {
                                    case "Recipient Name":
                                        item.put("text", participantAchievement.getName());
                                        break;
                                    case "Achievement":
                                        item.put("text", participantAchievement.getAchievement());
                                        break;
                                    case "Issue Date":
                                        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
                                        item.put("text", "Issued on " + sdf.format(new Date()));
                                        break;
                                }
                                drawCertificateItem(canvas, item);
                            }

                            // Save generated certificate to database
                            saveCertificate(participantAchievement.getId(), bitmapToBase64(certificateBitmap), participantAchievement.getName());
                        }

                    }
                });
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void drawCertificateItem(Canvas canvas, Map<String, Object> itemData) {
        // Create a CertificateItem from itemData
        CertificateItem certificateItem = new CertificateItem((String) itemData.get("text"));
        certificateItem.setFontSize(((Number) itemData.get("fontSize")).floatValue());
        certificateItem.setTextColor(((Number) itemData.get("textColor")).intValue());
        certificateItem.setFontStyle((String) itemData.get("fontStyle"));
        certificateItem.setBold((boolean) itemData.get("isBold"));
        certificateItem.setItalic((boolean) itemData.get("isItalic"));
        certificateItem.setUnderline((boolean) itemData.get("isUnderline"));

        // Set position
        Map<String, Double> positionMap = (Map<String, Double>) itemData.get("position");
        certificateItem.setPosition(new PointF(positionMap.get("x").floatValue(), positionMap.get("y").floatValue()));

        // Set alignment
        String alignment = (String) itemData.get("alignment");
        switch (alignment) {
            case "LEFT":
                certificateItem.setAlignment(CertificateItem.Alignment.LEFT);
                break;
            case "CENTER":
                certificateItem.setAlignment(CertificateItem.Alignment.CENTER);
                break;
            case "RIGHT":
                certificateItem.setAlignment(CertificateItem.Alignment.RIGHT);
                break;
        }

        // Set up paint object using the CertificateItem's style
        Paint paint = certificateItemController.setupPaint(certificateItem);

        float x = certificateItem.getPosition().x;
        float y = certificateItem.getPosition().y;

        // Handle long text
        List<String> lines = CertificateItemController.splitTextIntoLines(certificateItem.getText(), paint);

        float lineHeight = paint.getFontSpacing();
        float totalHeight = lineHeight * lines.size();
        y -= (totalHeight - lineHeight) / 2;

        for (String line : lines) {
            canvas.drawText(line, x, y, paint);
            y += lineHeight;
        }
    }

    // Save generated certificate to Firestore
    private void saveCertificate(String participantId, String fileContent, String participantName) {
        Map<String, Object> certificateData = new HashMap<>();
        certificateData.put("certificateType", certificateType);
        certificateData.put("eventId", eventId);
        certificateData.put("participantId", participantId);
        certificateData.put("participantName", participantName);
        certificateData.put("issueDate", new Date());
        certificateData.put("fileContent", fileContent);
        certificateData.put("fileName", participantName.replace(" ", "_") + "_"
                + eventName.replace(" ", "_") + "_" + certificateType + ".png");

        FirebaseFirestore.getInstance()
                .collection("certificates")
                .whereEqualTo("participantId", participantId)
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("certificateType", certificateType)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Update existing certificate
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            FirebaseFirestore.getInstance()
                                    .collection("certificates")
                                    .document(document.getId())
                                    .update(certificateData)
                                    .addOnSuccessListener(aVoid -> checkAllCertificatesSaved())
                                    .addOnFailureListener(e -> Log.e("CertGenerator", "Error updating certificate", e));
                        }
                    } else {
                        // Add new certificate
                        FirebaseFirestore.getInstance()
                                .collection("certificates")
                                .add(certificateData)
                                .addOnSuccessListener(documentReference -> checkAllCertificatesSaved())
                                .addOnFailureListener(e -> Log.e("CertGenerator", "Error adding certificate", e));
                    }
                });
    }

    // Method to check if all certificates are saved
    private void checkAllCertificatesSaved() {
        savedCertificates++;
        if (savedCertificates == totalCertificates) {
            progressBar.setVisibility(View.GONE);
            startCertMgmtActivity();
        }
    }

    // Add a new method to start CertMgmtActivity
    private void startCertMgmtActivity() {
        Toast.makeText(context, "Certificates generated successfully", Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.GONE);
        Intent intent = new Intent(context, CertMgmtActivity.class);
        intent.putExtra("eventId", eventId);
        intent.putExtra("eventName", eventName);
        context.startActivity(intent);
        ((Activity) context).finish();
    }


    // Load template preview for design
    private void loadTemplatePreview(String templateId, List<Map<String, Object>> certificateItems, ImageView imageView) {
        FirebaseFirestore.getInstance()
                .collection("certificateTemplates")
                .document(templateId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fileContent = documentSnapshot.getString("fileContent");
                        if (fileContent != null) {
                            // Decode base64 string to bitmap
                            byte[] decodedString = Base64.decode(fileContent, Base64.DEFAULT);
                            Bitmap templateBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                            // Create a mutable copy of the bitmap for drawing
                            Bitmap workingBitmap = templateBitmap.copy(Bitmap.Config.ARGB_8888, true);
                            certificateItemController.setTemplateDimensions(workingBitmap.getWidth(), workingBitmap.getHeight());
                            Canvas canvas = new Canvas(workingBitmap);

                            // Draw all certificate items on the template
                            if (certificateItems != null) {
                                for (Map<String, Object> itemData : certificateItems) {
                                    drawCertificateItem(canvas, itemData);
                                }
                            }

                            // Set the final bitmap to the ImageView
                            imageView.setImageBitmap(workingBitmap);
                        }
                    }
                });
    }
    interface OnDesignSelectedListener {
        void onDesignSelected(Design design);
    }

    // Adapter for displaying designs in dialog
    private class DesignAdapter extends RecyclerView.Adapter<DesignAdapter.ViewHolder> {
        private List<Design> designs = new ArrayList<>();
        private final OnDesignSelectedListener listener;


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

            // Load and display the design preview
            loadTemplatePreview(design.getTemplateId(), design.getCertificateItems(), holder.ivDesignPreview);

            holder.itemView.setOnClickListener(v -> listener.onDesignSelected(design));
        }

        @Override
        public int getItemCount() {
            return designs.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDesignName;
            ImageView ivDesignPreview;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDesignName = itemView.findViewById(R.id.tvDesignName);
                ivDesignPreview = itemView.findViewById(R.id.ivDesignPreview);
            }
        }
    }
}
