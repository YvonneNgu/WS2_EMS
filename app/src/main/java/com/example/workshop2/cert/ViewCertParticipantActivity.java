package com.example.workshop2.cert;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop2.R;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ViewCertParticipantActivity extends AppCompatActivity {

    private PDFView pdfView;
    private ProgressBar pbViewCert;
    private ImageButton iBtnBack;
    private ImageButton iBtnDownload;
    private byte[] currentPdfBytes;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_cert_participant);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        pdfView = findViewById(R.id.pdfView);
        pbViewCert = findViewById(R.id.pbViewCert);
        iBtnBack = findViewById(R.id.iBtnBack);
        iBtnDownload = findViewById(R.id.iBtnDownload);

        // Get certificate details from intent
        String certificateId = getIntent().getStringExtra("certificateId");

        // Show progress bar initially
        pbViewCert.setVisibility(View.VISIBLE);

        // Back button
        iBtnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewCertListActivity.class);
            startActivity(intent);
            finish();
        });

        // Download button
        iBtnDownload.setOnClickListener(v -> {
            if (currentPdfBytes != null && currentPdfBytes.length > 0) {
                savePdfToDownloads(currentPdfBytes, "certificate_" + certificateId + ".pdf");
            } else {
                Toast.makeText(this, "No PDF available to download", Toast.LENGTH_SHORT).show();
            }
        });

        // Load certificate
        loadCertificate(certificateId);
    }

    private void loadCertificate(String certificateId) {
        db.collection("certificates")
                .document(certificateId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    pbViewCert.setVisibility(View.GONE);

                    if (documentSnapshot.exists()) {
                        String fileContent = documentSnapshot.getString("fileContent");

                        if (fileContent != null) {
                            byte[] decodedBytes = Base64.decode(fileContent, Base64.DEFAULT);

                            // Convert PNG to PDF
                            try {
                                currentPdfBytes = convertPngToPdf(decodedBytes);
                                displayPdf(currentPdfBytes);
                            } catch (IOException e) {
                                showError("Error converting PNG to PDF: " + e.getMessage());
                            }

                        } else {
                            showError("No file content found");
                        }
                    } else {
                        showError("Certificate not found");
                    }
                })
                .addOnFailureListener(e -> {
                    pbViewCert.setVisibility(View.GONE);
                    showError("Error loading certificate: " + e.getMessage());
                });
    }

    private void displayPdf(byte[] pdfBytes) {
        try {
            pdfView.fromBytes(pdfBytes)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .defaultPage(0)
                    .onError(t -> {
                        t.printStackTrace();
                        showError("Error loading PDF: " + t.getMessage());
                    })
                    .load();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading PDF: " + e.getMessage());
        }
    }

    private byte[] convertPngToPdf(byte[] pngBytes) throws IOException {
        // Convert byte array to bitmap
        Bitmap originalBitmap = BitmapFactory.decodeByteArray(pngBytes, 0, pngBytes.length);

        // Get dimensions
        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();

        // Create PDF document
        PdfDocument pdfDocument = new PdfDocument();

        // Convert pixels to points (72 points = 1 inch)
        // Using a higher DPI for better quality
        float dpi = 300f; // Higher DPI for better quality
        float scale = 72f / dpi;

        int pdfWidth = (int) (originalWidth * scale);
        int pdfHeight = (int) (originalHeight * scale);

        // Create page info with the calculated dimensions
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                pdfWidth,
                pdfHeight,
                1)
                .create();

        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Scale the canvas
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);

        // Configure paint for high quality
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        paint.setAntiAlias(true);
        paint.setDither(true);

        // Draw bitmap with matrix
        canvas.drawBitmap(originalBitmap, matrix, paint);

        pdfDocument.finishPage(page);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        pdfDocument.writeTo(outputStream);

        // Clean up
        pdfDocument.close();
        originalBitmap.recycle();

        return outputStream.toByteArray();
    }

    private void savePdfToDownloads(byte[] pdfBytes, String fileName) {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
            values.put(MediaStore.Downloads.IS_PENDING, 1);

            ContentResolver resolver = getContentResolver();
            Uri collection = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
            }
            Uri itemUri = resolver.insert(collection, values);

            if (itemUri != null) {
                try (OutputStream outputStream = resolver.openOutputStream(itemUri)) {
                    if (outputStream != null) {
                        outputStream.write(pdfBytes);
                    }
                }

                values.clear();
                values.put(MediaStore.Downloads.IS_PENDING, 0);
                resolver.update(itemUri, values, null, null);

                Toast.makeText(this, "PDF saved to Downloads", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error saving PDF: " + e.getMessage());
        }
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        });
    }
}