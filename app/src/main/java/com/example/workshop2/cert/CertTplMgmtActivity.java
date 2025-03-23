package com.example.workshop2.cert;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop2.R;
import com.example.workshop2.cert.model.CertTpl;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CertTplMgmtActivity extends AppCompatActivity implements CertTplAdapter.OnTemplateActionListener {

    private static final int PICK_FILE_REQUEST = 1;
    private CertTplAdapter adapter;
    private RecyclerView rvTemplates;
    private List<CertTpl> templates;
    private int currentFileSelectionPosition = -1;

    private FirebaseFirestore db;
    private ProgressDialog progressDialog;
    private String eventId;
    private List<String> designToBeRemoved = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cert_tpl_mgmt);

        // Get event details from intent
        eventId = getIntent().getStringExtra("eventId");
        String eventName = getIntent().getStringExtra("eventName");

        // Initialize Firebase components
        db = FirebaseFirestore.getInstance();

        // Initialize views
        TextView tvEventName = findViewById(R.id.tvEventName);
        rvTemplates = findViewById(R.id.rvTemplates);
        Button btnSubmit = findViewById(R.id.btnSaveCertTpl);
        Button btnCancelUpdateCertTpl = findViewById(R.id.btnCancelUpdateCertTpl);

        // Set event name
        tvEventName.setText(eventName);

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing templates...");

        // Initialize RecyclerView
        templates = new ArrayList<>();
        adapter = new CertTplAdapter(this, templates, this);
        rvTemplates.setLayoutManager(new LinearLayoutManager(this));
        rvTemplates.setAdapter(adapter);

        ImageButton ibAddTemplate = findViewById(R.id.ibAddTemplate);
        ibAddTemplate.setOnClickListener(v -> adapter.addTemplate());ibAddTemplate.setOnClickListener(v -> onAddNewItem());

        // Submit button
        btnSubmit.setOnClickListener(v -> {
            uploadTemplates();
        });

        // Cancel button
        btnCancelUpdateCertTpl.setOnClickListener(v -> finish());

        // Load existing templates
        loadExistingTemplates();
    }

    private void loadExistingTemplates() {
        progressDialog.show();
        db.collection("certificateTemplates")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    templates.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        CertTpl template = document.toObject(CertTpl.class);
                        template.setId(document.getId());
                        templates.add(template);
                        Log.d("load tpl","id " + document.getId());
                    }
                    if (templates.isEmpty()) {
                        templates.add(new CertTpl()); // Add an empty template if no templates exist
                    }
                    adapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error loading templates: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onFileSelect(int position) {
        currentFileSelectionPosition = position;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"image/*", "application/pdf"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(Intent.createChooser(intent, "Select Template File"), PICK_FILE_REQUEST);
    }

    @Override
    public void onRemoveItem(int position) {
        CertTpl template = templates.get(position);
        String templateId = template.getId();

        // Check if the template is used by any certificateDesigns
        db.collection("certificateDesigns")
                .whereEqualTo("templateId", templateId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> designIds = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        designIds.add(document.getId());
                    }

                    String message = "Are you sure you want to remove this template?";
                    if (!designIds.isEmpty()) {
                        message = "This template is used by " + designIds.size() + " design(s). " + message;
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Remove Template")
                            .setMessage(message)
                            .setPositiveButton("Yes", (dialog, which) -> {
                                if (!designIds.isEmpty()) {
                                    // Record the design IDs for later removal
                                    designToBeRemoved.addAll(designIds);
                                }
                                adapter.removeTemplate(position);
                            })
                            .setNegativeButton("No", null)
                            .show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking template usage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onChangeFile(int position) {
        onFileSelect(position);
    }

    @Override
    public void onAddNewItem() {
        if (templates.isEmpty() || templates.get(templates.size() - 1).getFileContent() != null) {
            adapter.addTemplate();
            // Scroll to the bottom of the RecyclerView
            rvTemplates.post(() -> rvTemplates.smoothScrollToPosition(adapter.getItemCount() - 1));
        } else {
            Toast.makeText(this, "Please select a file for the current template first", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            CertTpl template = templates.get(currentFileSelectionPosition);

            try {
                InputStream inputStream = getContentResolver().openInputStream(fileUri);
                byte[] fileBytes = getBytes(inputStream);

                // Check file size
                if (fileBytes.length > 1048487) { // 1MB limit
                    Toast.makeText(this, "File size exceeds the 1MB limit. Please select a smaller file.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String base64File = android.util.Base64.encodeToString(fileBytes, android.util.Base64.DEFAULT);

                template.setFileContent(base64File);
                template.setFileName(getFileName(fileUri));
                template.setFileType(getContentResolver().getType(fileUri));

                adapter.notifyItemChanged(currentFileSelectionPosition);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error processing file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadTemplates() {
        List<String> validCertificateTypes = Arrays.asList("Participation", "Achievement", "General");

        // Track template names for auto-generation
        Map<String, Integer> typeNameCount = new HashMap<>();

        // Validate and auto-generate names
        for (CertTpl template : templates) {
            if (template.getFileContent() != null) {
                // Convert PDF to image if the file type is PDF
                if ("application/pdf".equals(template.getFileType())) {
                    byte[] imageBytes = convertPdfToImage(template.getFileContent());
                    if (imageBytes != null) {
                        String base64Image = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);
                        template.setFileContent(base64Image);
                        template.setFileType("image/png"); // Assuming conversion to PNG
                    } else {
                        Toast.makeText(this, "Error converting PDF to image", Toast.LENGTH_SHORT).show();
                        continue;
                    }
                }

                // Auto-generate template name if blank
                String currentName = template.getTemplateName();
                if (currentName == null || currentName.trim().isEmpty()) {
                    String baseTemplateName = template.getCertificateType() + " Template";
                    if (!typeNameCount.containsKey(template.getCertificateType())) {
                        template.setTemplateName(baseTemplateName);
                        typeNameCount.put(template.getCertificateType(), 1);
                    } else {
                        int count = typeNameCount.get(template.getCertificateType()) + 1;
                        template.setTemplateName(baseTemplateName + " " + count);
                        typeNameCount.put(template.getCertificateType(), count);
                    }
                }
                if (!validCertificateTypes.contains(template.getCertificateType())) {
                    template.setCertificateType("General");
                }

                // Set eventId for each template
                template.setEventId(eventId);
            }
        }

        progressDialog.setMessage("Uploading templates...");
        progressDialog.show();

        // Remove certificateDesigns if necessary
        if (designToBeRemoved != null) {
            for (String designId : designToBeRemoved) {
                db.collection("certificateDesigns").document(designId).delete();
            }
        }

        performBatchWrite();
    }

    private byte[] convertPdfToImage(String base64Pdf) {
        byte[] pdfBytes = android.util.Base64.decode(base64Pdf, android.util.Base64.DEFAULT);
        try {
            // Create a temporary file to hold the PDF data
            File tempFile = File.createTempFile("tempPdf", ".pdf", getCacheDir());
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(pdfBytes);
            }

            // Use the temporary file to create a PdfRenderer
            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY));
            if (renderer.getPageCount() > 0) {
                PdfRenderer.Page page = renderer.openPage(0);
                Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                page.close();
                renderer.close();

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                return outputStream.toByteArray();
            }
        } catch (IOException e) {
            Log.e("convertPdfToImage", "Error converting PDF to image", e);
        }
        return null;
    }

    private void performBatchWrite() {
        WriteBatch batch = db.batch();

        db.collection("certificateTemplates")
                .whereEqualTo("eventId", eventId)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> existingIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d("get template(DB)", "id " + document.getId());
                            existingIds.add(document.getId());
                        }

                        for (String existingId : existingIds) {
                            if (templates.stream().noneMatch(t -> existingId.equals(t.getId()))) {
                                Log.d("delete template", "id " + existingId);
                                batch.delete(db.collection("certificateTemplates").document(existingId));
                            }
                        }

                        for (CertTpl template : templates) {
                            Log.d("check template", "");
                            if(template.getFileContent()!=null){
                                DocumentReference docRef;
                                if (template.getId() != null && !template.getId().isEmpty()) {
                                    Log.d("update","");
                                    docRef = db.collection("certificateTemplates").document(template.getId());
                                } else {
                                    Log.d("new","");
                                    docRef = db.collection("certificateTemplates").document();
                                }

                                batch.set(docRef, template);
                            }
                            else Log.d("skip check","content is null");
                        }
                        batch.commit().addOnCompleteListener(batchTask -> {
                            progressDialog.dismiss();
                            if (batchTask.isSuccessful()) {
                                Toast.makeText(CertTplMgmtActivity.this,
                                        "All templates updated successfully",
                                        Toast.LENGTH_SHORT).show();
                                Intent returnIntent = new Intent();
                                setResult(RESULT_OK, returnIntent);
                                finish();
                            } else {
                                Toast.makeText(CertTplMgmtActivity.this,
                                        "Error updating templates: " + batchTask.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(CertTplMgmtActivity.this,
                                "Error fetching existing templates: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        byte[] bytesResult;
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        bytesResult = byteBuffer.toByteArray();
        return bytesResult;
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private String getFileType(Uri fileUri) {
        String mimeType = getContentResolver().getType(fileUri);
        if (mimeType != null) {
            if (mimeType.startsWith("image/")) {
                return "image";
            } else if (mimeType.equals("application/pdf")) {
                return "pdf";
            }
        }
        return "unknown";
    }
}