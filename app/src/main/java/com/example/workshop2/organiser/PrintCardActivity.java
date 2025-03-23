package com.example.workshop2.organiser;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.workshop2.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PrintCardActivity extends AppCompatActivity {

    private ImageView ivCardPreview;
    private String eventName, staffName, position, contactNo;
    private Bitmap cardBitmap;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_card);

        // Set up the Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set a custom title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Staff Card Preview");
        }

        ivCardPreview = findViewById(R.id.ivCardPreview);

        // Initialize Firebase Storage
        storageRef = FirebaseStorage.getInstance().getReference();

        // Get intent extras
        eventName = getIntent().getStringExtra("eventName");
        staffName = getIntent().getStringExtra("staffName");
        position = getIntent().getStringExtra("position");
        contactNo = getIntent().getStringExtra("contactNo");

        // Load background and generate card
        loadBackgroundAndGenerateCard();
    }

    private void loadBackgroundAndGenerateCard() {
        // Note the "Card Background" with a space instead of "CardBackground"
        StorageReference backgroundRef = storageRef.child("Card Background/CardBackgroundUTeMFTMK.png");

        final long FIVE_MEGABYTES = 5 * 1024 * 1024;
        backgroundRef.getBytes(FIVE_MEGABYTES).addOnSuccessListener(bytes -> {
            // Convert bytes to bitmap
            Bitmap backgroundBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (backgroundBitmap != null) {
                generateCard(backgroundBitmap);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(PrintCardActivity.this, "Error loading background: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            generateCard(null);
        });
    }


    private void generateCard(Bitmap backgroundBitmap) {
        int width, height;

        if (backgroundBitmap != null) {
            width = backgroundBitmap.getWidth();
            height = backgroundBitmap.getHeight();
        } else {
            // Default dimensions if background loading fails
            width = 600;
            height = 1000;
        }

        // Create a bitmap for the card using background dimensions
        cardBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(cardBitmap);

        if (backgroundBitmap != null) {
            canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        } else {
            canvas.drawColor(Color.WHITE);
        }

        // Set up paint for text
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setFakeBoldText(true);
        paint.setStrokeCap(Paint.Cap.ROUND);

        float x = width * 0.5f; // 50% of width;

        // Draw event name
        paint.setColor(Color.WHITE);
        paint.setTextSize(height * 0.035f);
        canvas.drawText(eventName, x, height * 0.33f, paint);

        // Draw staff name
        paint.setColor(Color.BLACK);
        paint.setTextSize(height * 0.045f);
        paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        // Check name length
        float maxWidth = width * 0.85f;
        if(paint.measureText(staffName) > maxWidth){
            List<String> lines = splitTextIntoLines(staffName, maxWidth, paint);

            float y = height * 0.51f;
            float lineHeight = paint.getFontSpacing();
            float totalHeight = lineHeight * lines.size();
            y -= (totalHeight - lineHeight) / 2;

            for (String line : lines) {
                canvas.drawText(line, x, y, paint);
                y += lineHeight;
            }
            // Draw position
            paint.setTextSize(height * 0.03f);
            canvas.drawText(position, x, height * 0.58f, paint);
        }
        else {
            canvas.drawText(staffName, x, height * 0.51f, paint);
            // Draw position
            paint.setTextSize(height * 0.03f);
            canvas.drawText(position, x, height * 0.56f, paint);
        }

        // Draw phone icon and contact number
            paint.setTextSize(height * 0.025f);
            paint.setTypeface(Typeface.MONOSPACE);
            paint.setTextAlign(Paint.Align.LEFT);

            // Load and scale phone icon
            Bitmap phoneIcon = BitmapFactory.decodeResource(getResources(), R.drawable.phone_icon);
            int iconSize = (int)(height * 0.025f);
            Bitmap scaledIcon = Bitmap.createScaledBitmap(phoneIcon, iconSize, iconSize, true);

            // Calculate positions for icon and text
            float textWidth = paint.measureText(contactNo);
            float totalWidth = iconSize + 10 + textWidth; // 10 is spacing
            float startX = x - (totalWidth * 0.5f);
        // Draw icon
        canvas.drawBitmap(scaledIcon, startX, height * 0.684f - iconSize, null);

        // Draw contact number
        canvas.drawText(contactNo, startX + iconSize + 10, height * 0.681f, paint);

        // Recycle the bitmaps
        phoneIcon.recycle();
        scaledIcon.recycle();

        // Display the preview
        ivCardPreview.setImageBitmap(cardBitmap);
    }

    private List<String> splitTextIntoLines(String text, float maxWidth, Paint paint) {
        List<String> lines = new ArrayList<>();
        String[] paragraphs = text.split("\n");

        for (String paragraph : paragraphs) {
            String[] words = paragraph.split("\\s");
            StringBuilder line = new StringBuilder();

            for (String word : words) {
                if (line.length() == 0) {
                    line.append(word);
                } else {
                    float lineWidth = paint.measureText(line + " " + word);
                    if (lineWidth <= maxWidth) {
                        line.append(" ").append(word);
                    } else {
                        lines.add(line.toString());
                        line = new StringBuilder(word);
                    }
                }
            }

            if (line.length() > 0) {
                lines.add(line.toString());
            }
        }

        return lines;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_print_card, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_print) {
            printCard();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void printCard() {
        // Create a PdfDocument with a page of the same size as our bitmap
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                cardBitmap.getWidth(),
                cardBitmap.getHeight(),
                1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        // Draw our bitmap onto the page
        Canvas canvas = page.getCanvas();
        canvas.drawBitmap(cardBitmap, 0, 0, null);
        document.finishPage(page);

        // Write the PDF file to cache directory
        File outputFile = new File(getCacheDir(), "staff_card.pdf");
        try {
            document.writeTo(new FileOutputStream(outputFile));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating PDF", Toast.LENGTH_SHORT).show();
            return;
        }
        document.close();

        // Get a URI for the PDF file
        String authority = getApplicationContext().getPackageName() + ".fileprovider";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(FileProvider.getUriForFile(this, authority, outputFile), "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Open the PDF viewer
        startActivity(Intent.createChooser(intent, "Open PDF"));
    }
}