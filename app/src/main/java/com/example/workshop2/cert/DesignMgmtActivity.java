package com.example.workshop2.cert;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop2.R;
import com.example.workshop2.cert.model.CertificateItem;
import com.example.workshop2.cert.model.Design;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.shuhart.stickyheader.StickyAdapter;
import com.shuhart.stickyheader.StickyHeaderItemDecorator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DesignMgmtActivity extends AppCompatActivity {

    private RecyclerView rvDesigns;
    private TextView tvNoDesigns;
    private Button btnBack, btnNewDesign;
    private FirebaseFirestore db;
    private String eventId, eventName;
    private DesignAdapter adapter;
    private CertificateItemController certificateItemController = new CertificateItemController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_design_mgmt);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");

        // Initialize views
        initViews();

        // Set up RecyclerView
        setupRecyclerView();

        // Load designs
        loadDesigns();

        // Set up button listeners
        setupListeners();
    }

    private void initViews() {
        rvDesigns = findViewById(R.id.rvDesigns);
        tvNoDesigns = findViewById(R.id.tvNoDesigns);
        btnBack = findViewById(R.id.btnBack);
        btnNewDesign = findViewById(R.id.btnNewDesign);
    }

    private void setupRecyclerView() {
        int paddingLeft = rvDesigns.getPaddingLeft();
        int paddingRight = rvDesigns.getPaddingRight();
        adapter = new DesignAdapter(paddingLeft, paddingRight);
        rvDesigns.setLayoutManager(new LinearLayoutManager(this));
        rvDesigns.setAdapter(adapter);

        // Add sticky header decoration
        StickyHeaderItemDecorator decorator = new StickyHeaderItemDecorator(adapter);
        decorator.attachToRecyclerView(rvDesigns);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, CertMgmtActivity.class);
            intent.putExtra("eventId", eventId);
            intent.putExtra("eventName", eventName);
            startActivity(intent);
            finish();
        });

        btnNewDesign.setOnClickListener(v -> {
            // Fetch the certificate types from the resources
            String[] allCertificateTypes = getResources().getStringArray(R.array.certificate_types);
            // Create a new array excluding the last item
            String[] certificateTypes = Arrays.copyOf(allCertificateTypes, allCertificateTypes.length - 1);

            new AlertDialog.Builder(this)
                    .setTitle("Select Certificate Type")
                    .setItems(certificateTypes, (dialog, which) -> {
                        String certificateType = certificateTypes[which];
                        Intent intent = new Intent(this, CreateDesignActivity.class);
                        intent.putExtra("eventId", eventId);
                        intent.putExtra("eventName", eventName);
                        intent.putExtra("certificateType", certificateType);
                        startActivity(intent);
                        finish();
                    })
                    .show();
        });
    }

    private void loadDesigns() {
        // Query designs from Firestore, ordered by certificate type
        db.collection("certificateDesigns")
                .whereEqualTo("eventId", eventId)
                .orderBy("certificateType", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Design> designs = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String designId = document.getId();
                        String certificateType = document.getString("certificateType");
                        String templateId = document.getString("templateId");
                        List<Map<String, Object>> certificateItems = (List<Map<String, Object>>) document.get("certificateItems");
                        Timestamp lastModified = document.getTimestamp("lastModified");

                        // Create design item with all necessary data
                        Design item = new Design();
                        item.setId(designId);
                        item.setDesignName(document.getString("designName"));
                        item.setCertificateType(certificateType);
                        item.setTemplateId(templateId);
                        item.setCertificateItems(certificateItems);
                        item.setLastModified(lastModified);
                        designs.add(item);
                    }

                    if (designs.isEmpty()) {
                        // Show "No designs" message if no designs found
                        tvNoDesigns.setVisibility(View.VISIBLE);
                        rvDesigns.setVisibility(View.GONE);
                    } else {
                        tvNoDesigns.setVisibility(View.GONE);
                        rvDesigns.setVisibility(View.VISIBLE);

                        // Sort designs by certificate type and lastModified in descending order
                        Collections.sort(designs, (d1, d2) -> {
                            int typeComparison = d1.getCertificateType().compareTo(d2.getCertificateType());
                            if (typeComparison != 0) {
                                return typeComparison;
                            }
                            return d2.getLastModified().compareTo(d1.getLastModified());
                        });

                        // Insert a design item to indicate the certificate type change
                        List<Design> sortedDesigns = new ArrayList<>();
                        String currentType = "";
                        for (Design design : designs) {
                            if (!design.getCertificateType().equals(currentType)) {
                                currentType = design.getCertificateType();
                                Log.d("insert header",currentType);
                                Design headerItem = new Design();
                                headerItem.setCertificateType(currentType);
                                sortedDesigns.add(headerItem);
                            }
                            Log.d("current type","add"+currentType);
                            sortedDesigns.add(design);
                        }
                        designs = sortedDesigns;

                        adapter.setItems(designs);
                        adapter.notifyDataSetChanged(); // Ensure adapter is notified of data changes
                    }
                })
                .addOnFailureListener(e -> {
                    // Show error message if loading fails
                    tvNoDesigns.setVisibility(View.VISIBLE);
                    rvDesigns.setVisibility(View.GONE);
                    tvNoDesigns.setText("Error loading designs");
                });
    }

    private void loadTemplatePreview(String templateId, List<Map<String, Object>> certificateItems, ImageView imageView) {
        // Load template from the correct collection path
        db.collection("certificateTemplates")
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
                            certificateItemController.setTemplateDimensions(workingBitmap.getWidth(),workingBitmap.getHeight() );
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

    // ViewHolder for design items
    private static class DesignViewHolder extends RecyclerView.ViewHolder {
        TextView tvDesignName;
        ImageView ivDesignPreview;

        DesignViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDesignName = itemView.findViewById(R.id.tvDesignName);
            ivDesignPreview = itemView.findViewById(R.id.ivDesignPreview);
        }
    }

    // ViewHolder for headers
    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvHeader);
        }
    }

    // Adapter with sticky headers
    private class DesignAdapter extends StickyAdapter<RecyclerView.ViewHolder, RecyclerView.ViewHolder> {
        private List<Design> items = new ArrayList<>();
        private static final int TYPE_ITEM = 0;
        private static final int TYPE_HEADER = 1;
        private final int paddingLeft;
        private final int paddingRight;

        DesignAdapter(int paddingLeft, int paddingRight) {
            this.paddingLeft = paddingLeft;
            this.paddingRight = paddingRight;
        }

        void setItems(List<Design> newItems) {
            items = newItems;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            // If current item's type is different from previous item's type, create both header and item
            if (position == 0) {
                return TYPE_HEADER;
            }
            else{
                String currentType = items.get(position).getCertificateType();
                String previousType = items.get(position - 1).getCertificateType();
                if (!currentType.equals(previousType)) {
                    return TYPE_HEADER;
                }
            }
            return TYPE_ITEM;
        }

        @Override
        public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_design_header, parent, false);
            return new HeaderViewHolder(view);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_HEADER) {
                return onCreateHeaderViewHolder(parent);
            }
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_design, parent, false);
            return new DesignViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Design item = items.get(position);

            if (getItemViewType(position) == TYPE_HEADER) {
                HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
                headerHolder.tvHeader.setText(item.getCertificateType() + " Certificates");
            } else {
                DesignViewHolder designHolder = (DesignViewHolder) holder;
                designHolder.tvDesignName.setText(item.getDesignName());
                loadTemplatePreview(item.getTemplateId(), item.getCertificateItems(), designHolder.ivDesignPreview);

                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(DesignMgmtActivity.this, CreateDesignActivity.class);
                    intent.putExtra("eventId", eventId);
                    intent.putExtra("eventName",eventName);
                    intent.putExtra("designId", item.getId());
                    intent.putExtra("designName", item.getDesignName());
                    startActivity(intent);
                });
            }
        }

        @Override
        public int getHeaderPositionForItem(int itemPosition) {
            // For the first item, return 0 as it's always a header
            if (itemPosition == 0) return 0;

            String currentType = items.get(itemPosition).getCertificateType();

            // Look backwards to find the first item of this section
            for (int i = itemPosition - 1; i >= 0; i--) {
                String previousType = items.get(i).getCertificateType();
                if (!currentType.equals(previousType)) {
                    // Return the position of the first item in current section
                    return i + 1;
                }
            }
            // If we haven't found a different type, this item belongs to the first section
            return 0;
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int headerPosition) {
            String certificateType = items.get(headerPosition).getCertificateType();
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.tvHeader.setText(certificateType + " Certificates");
            // Ensure header alignment with items
            headerHolder.itemView.setPadding(paddingLeft, 0, paddingRight, 0);
            headerHolder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
            ));
        }
    }
}