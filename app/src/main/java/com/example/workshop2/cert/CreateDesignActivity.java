package com.example.workshop2.cert;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop2.R;
import com.example.workshop2.cert.command.CertificateCommand;
import com.example.workshop2.cert.command.Command;
import com.example.workshop2.cert.model.CertTpl;
import com.example.workshop2.cert.model.CertificateItem;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

public class CreateDesignActivity extends AppCompatActivity{

    private FirebaseFirestore db;
    private String eventId, eventName;
    private String designId, designName;
    private String certificateType;

    private PhotoView pvCertificatePreview;
    private Button btnSaveDesign;
    private ImageButton ibFont, ibFontSize, ibTextColor,
            ibBold, ibItalic, ibUnderline,
            ibLeftAlign, ibCenterAlign, ibRightAlign,
            btnSelectTemplate, ibEditItem, ibDel, ibAddItemText,
            ibUndo, ibRedo;

    private CertificateItemController certificateItemController;;
    private String currentEditingItem;
    private CertTpl selectedTemplate;
    private List<CertTpl> templates;
    private int templateWidth, templateHeight;

    private float lastTouchX, lastTouchY;
    private CertificateItem selectedItem;
    private boolean isDragging = false;
    private static final float TOUCH_TOLERANCE = 10f;

    private Matrix matrix = new Matrix();
    private float[] matrixValues = new float[9];
    private GestureDetector gestureDetector;
    private Stack<Command> undoStack = new Stack<>();
    private Stack<Command> redoStack = new Stack<>();

    private PointF dragStartPosition;
    String[] allCertificateItemText;
    private static final int REQUEST_CODE_MANAGE_TEMPLATES = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_design);

        allCertificateItemText = getResources().getStringArray(R.array.certificate_items);

        templateWidth = 1000; templateHeight = 1000;

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get intent data
        Intent intent = getIntent();
        eventId = intent.getStringExtra("eventId");
        eventName = intent.getStringExtra("eventName");
        if (intent.hasExtra("designId")) {  // edit design
            designId = intent.getStringExtra("designId");
            designName = intent.getStringExtra("designName");
            loadDesign();
        } else {    // new design
            designId = null;
            certificateType = intent.getStringExtra("certificateType");

            // Initialize the controller with a callback
            certificateItemController = new CertificateItemController(certificateType, eventId, event -> {
                if (event != null) {
                    certificateItemController.getOrganizer(event.getUserId(), orgName -> {
                        certificateItemController.setOrganizerName(orgName);
                        Log.d("Controller", "Got organizer name: " + orgName);
                        // Proceed with initialization that depends on the event
                        initializeViews();
                        setupListeners();
                        loadTemplates();
                        setupPhotoView();
                    });
                } else {
                    // Handle the case where the event is not found or an error occurred
                    Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Initialize GestureDetector
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                handleClick(e.getX(), e.getY());
                return true;
            }
        });
    }

    private void loadDesign(){
        db.collection("certificateDesigns")
                .document(designId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        certificateType = documentSnapshot.getString("certificateType");
                        String templateId = documentSnapshot.getString("templateId");
                        List<Map<String, Object>> certificateItems = (List<Map<String, Object>>) documentSnapshot.get("certificateItems");

                        // Set the selected template ID
                        selectedTemplate = new CertTpl();
                        selectedTemplate.setId(templateId);

                        // Initialize the controller with a callback
                        certificateItemController = new CertificateItemController(certificateType, eventId, event -> {
                            if (event != null) {
                                // Add items to the controller
                                if (certificateItems != null) {
                                    for (Map<String, Object> itemData : certificateItems) {
                                        String key = (String) itemData.get("title");
                                        String text = (String) itemData.get("text");
                                        CertificateItem item = new CertificateItem(text);
                                        item.setTitle(key);
                                        item.setFontSize(((Number) itemData.get("fontSize")).floatValue());
                                        item.setTextColor(((Number) itemData.get("textColor")).intValue());
                                        item.setFontStyle((String) itemData.get("fontStyle"));
                                        item.setBold((boolean) itemData.get("isBold"));
                                        item.setItalic((boolean) itemData.get("isItalic"));
                                        item.setUnderline((boolean) itemData.get("isUnderline"));

                                        // Set position
                                        Map<String, Double> positionMap = (Map<String, Double>) itemData.get("position");
                                        item.setPosition(new PointF(positionMap.get("x").floatValue(), positionMap.get("y").floatValue()));

                                        // Set alignment
                                        String alignment = (String) itemData.get("alignment");
                                        switch (alignment) {
                                            case "LEFT":
                                                item.setAlignment(CertificateItem.Alignment.LEFT);
                                                break;
                                            case "CENTER":
                                                item.setAlignment(CertificateItem.Alignment.CENTER);
                                                break;
                                            case "RIGHT":
                                                item.setAlignment(CertificateItem.Alignment.RIGHT);
                                                break;
                                        }

                                        // Add item to the controller
                                        certificateItemController.addItem(key, item);
                                    }

                                    // Proceed with initialization that depends on the event
                                    initializeViews();
                                    setupListeners();
                                    loadTemplates();
                                    setupPhotoView();
                                }
                            } else {
                                // Handle the case where the event is not found or an error occurred
                                Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                    Toast.makeText(this, "Error loading design", Toast.LENGTH_SHORT).show();
                });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPhotoView() {
        // Configure PhotoView for zoom and pan
        pvCertificatePreview.setMinimumScale(0.5f);  // Allow zooming out a bit
        pvCertificatePreview.setMediumScale(1.5f);
        pvCertificatePreview.setMaximumScale(3.0f);

        // Enable zoom
        pvCertificatePreview.setZoomable(true);

        // Handle touch events
        pvCertificatePreview.setOnTouchListener((v, event) -> {
            // Get the current matrix values for coordinate transformation
            matrix = new Matrix();
            pvCertificatePreview.getSuppMatrix(matrix);
            matrix.getValues(matrixValues);

            // Transform touch coordinates to image coordinates
            float[] imagePoint = transformCoordinates(event.getX(), event.getY());
            float imageX = imagePoint[0];
            float imageY = imagePoint[1];

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastTouchX = imageX;
                    lastTouchY = imageY;

                    // Check if we're touching a certificate item
                    CertificateItem touchedItem = findTouchedItem(imageX, imageY);
                    if (touchedItem != null) {
                        selectedItem = touchedItem;
                        currentEditingItem = getItemKey(touchedItem);
                        isDragging = true;
                        dragStartPosition = new PointF(touchedItem.getPosition().x, touchedItem.getPosition().y);
                        updateEditingToolbarState();
                        Log.d("set up photo view item selected","");
                        updateCertificatePreview();
                        return true;
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (isDragging && selectedItem != null) {
                        float dx = imageX - lastTouchX;
                        float dy = imageY - lastTouchY;

                        // Only update position if movement is significant
                        if (Math.abs(dx) > TOUCH_TOLERANCE || Math.abs(dy) > TOUCH_TOLERANCE) {
                            PointF currentPos = selectedItem.getPosition();
                            float newX = currentPos.x + dx;
                            float newY = currentPos.y + dy;

                            // Ensure the new position is within the template bounds
                            Paint paint = CertificateItemController.setupPaint(selectedItem);
                            int textLength = (int) certificateItemController.getItemWidth(selectedItem);

                            int minY = (int) paint.getFontSpacing(); // textHeight
                            int maxY = templateHeight;
                            int minX = 0;
                            int maxX = templateWidth - textLength;

                            CertificateItem.Alignment alignment = selectedItem.getAlignment();
                            if (alignment == CertificateItem.Alignment.CENTER) {
                                minX = textLength / 2;
                                maxX = templateWidth - textLength / 2;
                            } else if (alignment == CertificateItem.Alignment.RIGHT) {
                                minX = textLength;
                                maxX = templateWidth;
                            }
                            if (newX < minX) newX = minX;
                            if (newY < minY) newY = minY;
                            if (newX > maxX) newX = maxX;
                            if (newY > maxY) newY = maxY;

                            // Update position directly without pushing to undo stack
                            selectedItem.setPosition(new PointF(newX, newY));
                            lastTouchX = imageX;
                            lastTouchY = imageY;
                            Log.d("set photo view update position","");
                            updateCertificatePreview();
                            return true;
                        }
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (isDragging && selectedItem != null) {
                        // Push to undo stack only when drag ends
                        updateItemPosition(currentEditingItem, dragStartPosition);
                        isDragging = false;
                    } else if (!isDragging) {
                        // Handle click when not dragging
                        handleClick(event.getX(), event.getY());
                    }
                    isDragging = false;
                    break;
            }

            // Let PhotoView handle the event if we're not dragging
            return pvCertificatePreview.onTouchEvent(event);
        });
    }

    private RectF getImageBounds() {
        if (pvCertificatePreview == null || pvCertificatePreview.getDrawable() == null) {
            return null;
        }

        // Get the displayed rectangle of the image
        return pvCertificatePreview.getDisplayRect();
    }

    private boolean isPointInsideImage(float x, float y) {
        RectF imageBounds = getImageBounds();
        return imageBounds != null && imageBounds.contains(x, y);
    }

    private void handleClick(float x, float y) {
        if (!isPointInsideImage(x, y)) {
            // Click is outside the image area
            selectedItem = null;
            currentEditingItem = null;
            updateEditingToolbarState();
            Log.d("handle click point out","");
            updateCertificatePreview();
            return;
        }

        // Convert screen coordinates to image coordinates
        float[] imagePoint = transformCoordinates(x, y);
        float imageX = imagePoint[0];
        float imageY = imagePoint[1];

        // Find and select clicked item
        CertificateItem clickedItem = findTouchedItem(imageX, imageY);
        if (clickedItem != null) {
            selectedItem = clickedItem;
            currentEditingItem = getItemKey(clickedItem);
            updateEditingToolbarState();
            Log.d("handle click if","");
            updateCertificatePreview();
        } else {
            selectedItem = null;
            currentEditingItem = null;
            updateEditingToolbarState();
            Log.d("handle click else","");
            updateCertificatePreview();
        }
    }

    private float[] transformCoordinates(float screenX, float screenY) {
        RectF displayRect = pvCertificatePreview.getDisplayRect();
        if (displayRect == null) return new float[]{screenX, screenY};

        float scale = pvCertificatePreview.getScale();

        // Convert screen coordinates to image coordinates
        float imageX = (screenX - displayRect.left) / scale;
        float imageY = (screenY - displayRect.top) / scale;

        // Scale to template dimensions
        imageX = (imageX / displayRect.width()) * templateWidth;
        imageY = (imageY / displayRect.height()) * templateHeight;

        return new float[]{imageX, imageY};
    }

    private CertificateItem findTouchedItem(float x, float y) {
        Map<String, CertificateItem> items = certificateItemController.getAllItems();
        for (Map.Entry<String, CertificateItem> entry : items.entrySet()) {
            CertificateItem item = entry.getValue();
            if (isTouchInItemBounds(x, y, item)) {
                return item;
            }
        }
        return null;
    }

    private boolean isTouchInItemBounds(float x, float y, CertificateItem item) {

        // Create touch area bounds (larger than text for easier selection)
        float padding = 30;  // Increased padding for easier selection
        RectF bounds = new RectF(
                certificateItemController.getItemLeft(item) - padding,
                certificateItemController.getItemTop(item) - padding,
                certificateItemController.getItemRight(item) + padding,
                certificateItemController.getItemBottom(item) + padding
        );

        return bounds.contains(x, y);
    }

    private void drawSelectionHighlight(Canvas canvas, CertificateItem item) {
        Paint highlightPaint = new Paint();
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setColor(Color.BLUE);
        highlightPaint.setStrokeWidth(2);

        // Draw selection rectangle
        canvas.drawRect(
                certificateItemController.getItemLeft(item) - 5,
                certificateItemController.getItemTop(item) - 5,
                certificateItemController.getItemRight(item) + 5,
                certificateItemController.getItemBottom(item) + 5,
                highlightPaint
        );
    }

    // Initialize views
    private void initializeViews() {
        pvCertificatePreview = findViewById(R.id.pvCertificatePreview);
        btnSaveDesign = findViewById(R.id.btnSaveDesign);
        ibFont = findViewById(R.id.ibFont);
        ibFontSize = findViewById(R.id.ibFontSize);
        ibTextColor = findViewById(R.id.ibTextColor);
        ibBold = findViewById(R.id.ibBold);
        ibItalic = findViewById(R.id.ibItalic);
        ibUnderline = findViewById(R.id.ibUnderline);
        ibLeftAlign = findViewById(R.id.ibLeftAlign);
        ibCenterAlign = findViewById(R.id.ibCenterAlign);
        ibRightAlign = findViewById(R.id.ibRightAlign);
        btnSelectTemplate = findViewById(R.id.ibSelectTemplate);
        ibDel = findViewById(R.id.ibDel);
        ibEditItem = findViewById(R.id.ibEditItem);
        ibAddItemText = findViewById(R.id.ibAddCertItemTxt);

        ibUndo = findViewById(R.id.ibUndo);
        ibRedo = findViewById(R.id.ibRedo);
        updateUndoRedoButtons(); // Initial state
    }

    // Set up listeners
    private void setupListeners() {
        btnSaveDesign.setOnClickListener(v -> saveDesign());
        btnSelectTemplate.setOnClickListener(v -> showTemplateSelectionDialog());

        ibFont.setOnClickListener(v -> changeFontForCurrentItem());
        ibFontSize.setOnClickListener(v -> changeFontSizeForCurrentItem());
        ibTextColor.setOnClickListener(v -> changeTextColorForCurrentItem());
        ibBold.setOnClickListener(v -> toggleBoldForCurrentItem());
        ibItalic.setOnClickListener(v -> toggleItalicForCurrentItem());
        ibUnderline.setOnClickListener(v -> toggleUnderlineForCurrentItem());
        ibLeftAlign.setOnClickListener(v -> alignCurrentItem(CertificateItem.Alignment.LEFT));
        ibCenterAlign.setOnClickListener(v -> alignCurrentItem(CertificateItem.Alignment.CENTER));
        ibRightAlign.setOnClickListener(v -> alignCurrentItem(CertificateItem.Alignment.RIGHT));
        ibDel.setOnClickListener(v -> deleteSelectedItem());
        ibEditItem.setOnClickListener(v -> editCertificateItemText());
        ibAddItemText.setOnClickListener(v -> addCertificateItemText());
        ibUndo.setOnClickListener(v -> undo());
        ibRedo.setOnClickListener(v -> redo());
    }

    private String getItemKey(CertificateItem targetItem) {
        Map<String, CertificateItem> items = certificateItemController.getAllItems();
        for (Map.Entry<String, CertificateItem> entry : items.entrySet()) {
            if (entry.getValue() == targetItem) {
                return entry.getKey();
            }
        }
        return null;
    }

    // Load templates from Firestore
    private void loadTemplates() {

        templates = new ArrayList<>();

        db.collection("certificateTemplates")
                .whereIn("certificateType", List.of(certificateType, "General", "System Default"))
                .whereIn("eventId", List.of(eventId, "System Default"))
                .orderBy("certificateType", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    // load all templates
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        CertTpl template = document.toObject(CertTpl.class);
                        template.setId(document.getId());
                        templates.add(template);
                    }

                    if (!templates.isEmpty()) { // has template

                        if (designId == null){  // new design
                            selectedTemplate = templates.get(0);
                        }
                        else{   // editing existing design
                            for (CertTpl template : templates) {
                                if (template.getId().equals(selectedTemplate.getId())) {
                                    selectedTemplate = template;
                                }
                            }
                        }
                        byte[] decodedString = Base64.decode(selectedTemplate.getFileContent(), Base64.DEFAULT);
                        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        templateWidth = decodedBitmap.getWidth();
                        templateHeight = decodedBitmap.getHeight();
                        certificateItemController.setTemplateDimensions(templateWidth, templateHeight);

                        if(designId == null) {    // setup all items for new design
                            certificateItemController.defaultSetup(allCertificateItemText);
                        }

                        Log.d("load tpl","");

                        updateCertificatePreview();
                    }
                    else{   // if no template, use default template
                        Log.d("Error", "No templates found");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                });
    }

    // Update certificate preview with custom drawing
    private void updateCertificatePreview() {
        Log.d("update preview","");
        if (selectedTemplate == null) return;

        // Decode and create working bitmap
        byte[] decodedString = Base64.decode(selectedTemplate.getFileContent(), Base64.DEFAULT);
        Bitmap templateBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        Bitmap workingBitmap = templateBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas workingCanvas = new Canvas(workingBitmap);

        // Draw all certificate items
        Map<String, CertificateItem> items = certificateItemController.getAllItems();
        for (Map.Entry<String, CertificateItem> entry : items.entrySet()) {
            CertificateItem item = entry.getValue();

            // Highlight selected item
            if (item == selectedItem) {
                drawSelectionHighlight(workingCanvas, item);
            }

            drawCertificateItem(workingCanvas, item);
        }
        Log.d("end draw", "________________________________________________________________");

        pvCertificatePreview.setImageBitmap(workingBitmap);
    }

    private void drawCertificateItem(Canvas canvas, CertificateItem item) {
        Paint paint = CertificateItemController.setupPaint(item);
        String text = item.getText();

        float x = item.getPosition().x;
        float y = item.getPosition().y;

        // Handle long text
        List<String> lines = CertificateItemController.splitTextIntoLines(text, paint);

        float lineHeight = paint.getFontSpacing();
        float totalHeight = lineHeight * lines.size();
        y -= (totalHeight - lineHeight) / 2;

        for (String line : lines) {
            canvas.drawText(line, x, y, paint);
            y += lineHeight;
        }
    }

    // Save cert design
    private void saveDesign() {
        // Create an AlertDialog to ask for the design name
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Design Name");

        // Set up the input
        final EditText input = new EditText(this);
        input.setHint("Design Name");
        if (designId != null) {
            // If editing, set the current design name
            input.setText(designName);
        }
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            String designName = input.getText().toString().trim();
            if (designName.isEmpty()) {
                Toast.makeText(this, "Design name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Saving your design...", Toast.LENGTH_SHORT).show();

            // Prepare data to be saved
            Map<String, Object> designData = new HashMap<>();
            designData.put("designName", designName);
            designData.put("certificateType", certificateType);
            designData.put("eventId", eventId);
            designData.put("templateId", selectedTemplate.getId());
            designData.put("lastModified", new Date());

            // Get all certificate items
            Map<String, CertificateItem> items = certificateItemController.getAllItems();
            List<Map<String, Object>> itemList = new ArrayList<>();
            for (Map.Entry<String, CertificateItem> entry : items.entrySet()) {
                CertificateItem item = entry.getValue();
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("title", entry.getKey());
                Log.d("item key",entry.getKey());
                itemData.put("text", item.getText());
                itemData.put("position", item.getPosition());
                itemData.put("fontStyle", item.getFontStyle());
                itemData.put("fontSize", item.getFontSize());
                itemData.put("textColor", item.getTextColor());
                itemData.put("isBold", item.isBold());
                itemData.put("isItalic", item.isItalic());
                itemData.put("isUnderline", item.isUnderline());
                itemData.put("alignment", item.getAlignment().name());
                itemList.add(itemData);
            }
            designData.put("certificateItems", itemList);

            // Save to Firestore
            if (designId != null) {
                db.collection("certificateDesigns")
                        .document(designId)
                        .set(designData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Design updated", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, DesignMgmtActivity.class);
                            intent.putExtra("eventId", eventId);
                            intent.putExtra("eventName", eventName);
                            finish();
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to update design", Toast.LENGTH_SHORT).show();
                            Log.e("CreateDesignActivity", "Error updating design", e);
                        });
            } else {
                db.collection("certificateDesigns")
                        .add(designData)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "Design saved", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, DesignMgmtActivity.class);
                            intent.putExtra("eventId", eventId);
                            intent.putExtra("eventName", eventName);
                            finish();
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to save design", Toast.LENGTH_SHORT).show();
                            Log.e("CreateDesignActivity", "Error saving design", e);
                        });
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // Show template selection dialog
    private void showTemplateSelectionDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_template_selection, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        LinearLayout llTemplates = bottomSheetView.findViewById(R.id.llTemplates);

        for (CertTpl template : templates) {
            View templateView = LayoutInflater.from(this).inflate(R.layout.item_cert_tpl, null);
            ImageView ivTemplate = templateView.findViewById(R.id.ivTemplate);
            TextView tvTemplateName = templateView.findViewById(R.id.tvTemplateName);

            byte[] decodedString = Base64.decode(template.getFileContent(), Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            ivTemplate.setImageBitmap(decodedBitmap);

            tvTemplateName.setText(template.getTemplateName());

            templateView.setOnClickListener(v -> {
                if(selectedTemplate != template) { // User select different template
                    selectedTemplate = template;
                    templateWidth = decodedBitmap.getWidth();
                    templateHeight = decodedBitmap.getHeight();

                    certificateItemController.setTemplateDimensions(templateWidth, templateHeight);
                    certificateItemController.defaultSetup(allCertificateItemText);
                    Log.d("tpl selection dialog","");
                    updateCertificatePreview();
                }
                bottomSheetDialog.dismiss();
            });

            llTemplates.addView(templateView);
        }

        ImageButton ibManageTpl = bottomSheetView.findViewById(R.id.ibManageTpl);
        ibManageTpl.setOnClickListener(v -> {   // back to here when finish CertTplMgmtActivity
            Intent intent = new Intent(this, CertTplMgmtActivity.class);
            intent.putExtra("eventId", eventId);
            intent.putExtra("eventName", eventName);
            startActivityForResult(intent, REQUEST_CODE_MANAGE_TEMPLATES);
            bottomSheetDialog.dismiss();
        });

        Button btnCancel = bottomSheetView.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_MANAGE_TEMPLATES && resultCode == RESULT_OK) {
            Log.d("onActivityResult", "Returned from CertTplMgmtActivity");
            // Reload templates when returning from CertTplMgmtActivity
            db.collection("certificateTemplates")
                    .whereIn("certificateType", List.of(certificateType, "General", "System Default"))
                    .whereIn("eventId", List.of(eventId, "System Default"))
                    .orderBy("certificateType", Query.Direction.ASCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {

                        // load all templates
                        templates.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            CertTpl template = document.toObject(CertTpl.class);
                            template.setId(document.getId());
                            templates.add(template);
                        }
                    })
                .addOnFailureListener(e -> {
                            // Handle the error
                        });

        }
    }

    private void updateEditingToolbarState() {
        if (currentEditingItem != null) {
            CertificateItem item = certificateItemController.getItem(currentEditingItem);
            if (item != null) {

                // Recipient name and achievement cannot be removed/edit
                boolean isRecipientNameOrAchievement
                         = Objects.equals(currentEditingItem, "Recipient Name")
                        || Objects.equals(currentEditingItem, "Achievement");
                Log.d("key", currentEditingItem);
                ibDel.setEnabled(!isRecipientNameOrAchievement);
                ibDel.setAlpha(isRecipientNameOrAchievement ? 0.5f : 1.0f);

                ibEditItem.setEnabled(!isRecipientNameOrAchievement);
                ibEditItem.setAlpha(isRecipientNameOrAchievement ? 0.5f : 1.0f);

                // Bold Italic Underline
                ibBold.setAlpha(item.isBold() ? 1.0f : 0.5f);
                ibItalic.setAlpha(item.isItalic() ? 1.0f : 0.5f);
                ibUnderline.setAlpha(item.isUnderline() ? 1.0f : 0.5f);

                // Update alignment buttons
                ibLeftAlign.setAlpha(item.getAlignment() == CertificateItem.Alignment.LEFT ? 1.0f : 0.5f);
                ibCenterAlign.setAlpha(item.getAlignment() == CertificateItem.Alignment.CENTER ? 1.0f : 0.5f);
                ibRightAlign.setAlpha(item.getAlignment() == CertificateItem.Alignment.RIGHT ? 1.0f : 0.5f);
            }
        }
    }

    // Change font for current item
    private void changeFontForCurrentItem() {
        if (currentEditingItem == null) return;

        // Define font options with display names and their resource identifiers
        String[] fontNames = {"Default", "Monospace", "Sans Serif", "Serif", "Signature"};
        String[] fontStyles = {"DEFAULT", "FONT_MONOSPACE", "FONT_SANS_SERIF", "FONT_SERIF", "FONT_SIGNATURE"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Font Style")
                .setItems(fontNames, (dialog, which) -> {
                    updateItemFont(currentEditingItem, fontStyles[which]);
                    Log.d("font","");
                    updateCertificatePreview();
                })
                .show();
    }

    // Change font size for current item
    private void changeFontSizeForCurrentItem() {
        if (currentEditingItem == null) return;

        View popupView = getLayoutInflater().inflate(R.layout.dialog_font_size, null);
        SeekBar seekBar = popupView.findViewById(R.id.sbFontSize);
        TextView tvFontSize = popupView.findViewById(R.id.tvFontSize);

        // Set initial value
        CertificateItem item = certificateItemController.getItem(currentEditingItem);
        int currentSize = (int) item.getFontSize();
        final float[] initialFontSize = {currentSize}; // Store initial font size
        seekBar.setProgress(currentSize);
        tvFontSize.setText(String.valueOf(currentSize));

        seekBar.setMax((int) (templateHeight*0.12f)); // Maximum font size
        seekBar.setMin((int) (templateHeight*0.03f));  // Minimum font size

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvFontSize.setText(String.valueOf(progress));
                // Update font size directly without pushing to undo stack
                CertificateItem item = certificateItemController.getItem(currentEditingItem);
                if (item != null) {
                    item.setFontSize(progress);
                    Log.d("fontSize","");
                    updateCertificatePreview();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                initialFontSize[0] = item.getFontSize();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Push to undo stack only when seek bar tracking ends
                updateItemFontSize(currentEditingItem, initialFontSize[0]);
            }
        });

        // Create PopupWindow
        PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_speech_bubble));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        // Show the PopupWindow below the ibFontSize button
        int[] location = new int[2];
        ibFontSize.getLocationOnScreen(location);
        popupWindow.showAtLocation(ibFontSize, Gravity.NO_GRAVITY, location[0], location[1] + ibFontSize.getHeight() + 0);
    }

    // Change text color for current item
    private void changeTextColorForCurrentItem() {
        // Implement text color change functionality
        if (currentEditingItem == null) return;

        ColorPickerDialogBuilder
                .with(CreateDesignActivity.this)
                .setTitle("Choose text color")
                .initialColor(certificateItemController.getItem(currentEditingItem).getTextColor())
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setPositiveButton("OK", (dialog, selectedColor, allColors) -> {

                    updateItemTextColor(currentEditingItem, selectedColor);
                    Log.d("textColor","");
                    updateCertificatePreview();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .build()
                .show();
    }

    // Toggle bold for current item
    private void toggleBoldForCurrentItem() {
        if (currentEditingItem == null) return;
        CertificateItem item = certificateItemController.getItem(currentEditingItem);
        if (item != null) {
            updateItemBold(currentEditingItem, !item.isBold());
            Log.d("toggleBold","");
            updateCertificatePreview();
            updateEditingToolbarState();
        }
    }

    // Toggle italic for current item
    private void toggleItalicForCurrentItem() {
        if (currentEditingItem == null) return;
        CertificateItem item = certificateItemController.getItem(currentEditingItem);
        if (item != null) {
            updateItemItalic(currentEditingItem, !item.isItalic());
            Log.d("toggleItalic","");
            updateCertificatePreview();
            updateEditingToolbarState();
        }
    }

    // Toggle underline for current item
    private void toggleUnderlineForCurrentItem() {
        if (currentEditingItem == null) return;
        CertificateItem item = certificateItemController.getItem(currentEditingItem);
        if (item != null) {
            updateItemUnderline(currentEditingItem, !item.isUnderline());
            Log.d("toggleUnderline","");
            updateCertificatePreview();
            updateEditingToolbarState();
        }
    }

    // Align current item
    private void alignCurrentItem(CertificateItem.Alignment alignment) {
        if (currentEditingItem == null) return;
        CertificateItem item = certificateItemController.getItem(currentEditingItem);
        if (item != null) {
            updateItemAlignment(currentEditingItem, alignment);
            Log.d("alignCurrentItem","");
            updateCertificatePreview();
            updateEditingToolbarState();
        }
    }

    private void deleteSelectedItem() {
        if (currentEditingItem != null) {
            selectedItem = null;
            deleteItem(currentEditingItem);
            currentEditingItem = null;
            Log.d("deleteSelectedItem","");
        }
    }

    private void editCertificateItemText() {
        if (currentEditingItem == null) return;

        // Create a custom layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_text_cert, null);
        EditText etItemText = dialogView.findViewById(R.id.etItemText);

        // Set the current text of the item in the EditText
        CertificateItem item = certificateItemController.getItem(currentEditingItem);
        etItemText.setText(item.getText());

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Certificate Item Text")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    // Save the new text
                    String newText = etItemText.getText().toString();
                    updateItemText(currentEditingItem, newText);
                    Log.d("edit text","");
                    updateCertificatePreview();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Do nothing on cancel
                })
                .setCancelable(false) // Make the dialog modal
                .show();
    }

    // Add text to certificate
    private void addCertificateItemText(){
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_add_text, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        LinearLayout llTextItems = bottomSheetView.findViewById(R.id.llTextItems);
        String[] allCertificateItemText = getResources().getStringArray(R.array.certificate_items);

        for (String key : allCertificateItemText) {
            // Skip if item already exists
            if (certificateItemController.getItem(key) != null) {
                continue;
            }

            Log.d("key not found", key);

            View itemView = getLayoutInflater().inflate(R.layout.item_cert_text, null);
            TextView tvItemTitle = itemView.findViewById(R.id.tvItemTitle);
            TextView tvItemDefaultText = itemView.findViewById(R.id.tvItemDefaultText);

            tvItemTitle.setText(key);
            String defaultText = certificateItemController.getItemDefaultText(key);
            if (defaultText != null) {
                tvItemDefaultText.setText(defaultText);

                // Make the entire item clickable
                itemView.setOnClickListener(v -> {
                    addItem(key);
                    bottomSheetDialog.dismiss();
                });

                llTextItems.addView(itemView);
            }
        }

        // Set up cancel button
        Button btnCancel = bottomSheetView.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    private void executeCommand(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear(); // Clear redo stack when new command is executed
        updateUndoRedoButtons();
        Log.d("executeCommand","");
        updateCertificatePreview();
    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            Command command = undoStack.pop();
            command.undo();
            redoStack.push(command);
            updateUndoRedoButtons();
            Log.d("undo","");
            updateCertificatePreview();
            updateEditingToolbarState();
        }
    }

    private void redo() {
        if (!redoStack.isEmpty()) {
            Command command = redoStack.pop();
            command.execute();
            undoStack.push(command);
            updateUndoRedoButtons();
            Log.d("redo","");
            updateCertificatePreview();
            updateEditingToolbarState();
        }
    }

    private void updateUndoRedoButtons() {
        ibUndo.setEnabled(!undoStack.isEmpty());
        ibRedo.setEnabled(!redoStack.isEmpty());
        ibUndo.setAlpha(undoStack.isEmpty() ? 0.5f : 1.0f);
        ibRedo.setAlpha(redoStack.isEmpty() ? 0.5f : 1.0f);
    }

    // Update all modification methods to use commands
    private void updateItemPosition(String key, PointF oldPosition) {
        CertificateItem item = certificateItemController.getItem(key);
        if (item != null) {
            PointF newPosition = item.getPosition();
            if(!newPosition.equals(oldPosition)) {
                Command command = new CertificateCommand(
                        certificateItemController,
                        key,
                        CertificateCommand.CommandType.MOVE,
                        oldPosition,
                        newPosition
                );
                executeCommand(command);
            }
        }
    }

    private void updateItemText(String key, String newText) {
        CertificateItem item = certificateItemController.getItem(key);
        if (item != null) {
            String oldText = item.getText();
            if(!oldText.equals(newText)) {
                Command command = new CertificateCommand(
                        certificateItemController,
                        key,
                        CertificateCommand.CommandType.TEXT,
                        oldText,
                        newText
                );
                executeCommand(command);
            }
        }
    }

    public void updateItemFontSize(String key, float oldFontSize) {
        CertificateItem item = certificateItemController.getItem(key);
        if (item != null) {
            float newFontSize = item.getFontSize();
            if(newFontSize!=oldFontSize){
                Command command = new CertificateCommand(
                        certificateItemController,
                        key,
                        CertificateCommand.CommandType.FONT_SIZE,
                        oldFontSize,
                        newFontSize
                );
                executeCommand(command);
            }
        }
    }

    public void updateItemFont(String key, String newFontStyle) {
        CertificateItem item = certificateItemController.getItem(key);
        if (item != null) {
            String oldFontStyle = item.getFontStyle();
            if(!newFontStyle.equals(oldFontStyle)){
                Command command = new CertificateCommand(
                        certificateItemController,
                        key,
                        CertificateCommand.CommandType.FONT_STYLE,
                        oldFontStyle,
                        newFontStyle
                );
                executeCommand(command);
            }
        }
    }

    public void updateItemTextColor(String key, int newColor) {
        CertificateItem item = certificateItemController.getItem(key);
        if (item != null) {
            int oldColor = item.getTextColor();
            if(oldColor!=newColor){
                Command command = new CertificateCommand(
                        certificateItemController,
                        key,
                        CertificateCommand.CommandType.COLOR,
                        oldColor,
                        newColor
                );
                executeCommand(command);
            }
        }
    }

    public void updateItemBold(String key, boolean isBold) {
        CertificateItem item = certificateItemController.getItem(key);
        if (item != null) {
            Command command = new CertificateCommand(
                    certificateItemController,
                    key,
                    CertificateCommand.CommandType.BOLD,
                    item.isBold(),
                    isBold
            );
            executeCommand(command);
        }
    }

    public void updateItemItalic(String key, boolean isItalic) {
        CertificateItem item = certificateItemController.getItem(key);
        if (item != null) {
            Command command = new CertificateCommand(
                    certificateItemController,
                    key,
                    CertificateCommand.CommandType.ITALIC,
                    item.isItalic(),
                    isItalic
            );
            executeCommand(command);
        }
    }

    public void updateItemUnderline(String key, boolean isUnderline) {
        CertificateItem item = certificateItemController.getItem(key);
        if (item != null) {
            Command command = new CertificateCommand(
                    certificateItemController,
                    key,
                    CertificateCommand.CommandType.UNDERLINE,
                    item.isUnderline(),
                    isUnderline
            );
            executeCommand(command);
        }
    }

    public void updateItemAlignment(String key, CertificateItem.Alignment newAlign) {
        CertificateItem item = certificateItemController.getItem(key);
        if (item != null) {
            CertificateItem.Alignment oldAlign = item.getAlignment();
            float oldXPosition = item.getPosition().x;
            if (oldAlign != newAlign) {
                Command command = new CertificateCommand(
                        certificateItemController,
                        key,
                        CertificateCommand.CommandType.ALIGNMENT,
                        oldAlign,
                        newAlign
                );
                executeCommand(command);
            }
            else if (oldXPosition != templateWidth * 0.5f) {
                Command command = new CertificateCommand(
                        certificateItemController,
                        key,
                        CertificateCommand.CommandType.MOVE,
                        item.getPosition(),
                        new PointF(templateWidth * 0.5f, item.getPosition().y)
                );
                executeCommand(command);
            }
        }
    }

    public void addItem(String key) {
        Command command = new CertificateCommand(
                certificateItemController,
                key,
                CertificateCommand.CommandType.ADD,
                null,
                null
        );
        executeCommand(command);

    }

    public void deleteItem(String key) {
        Log.d("deleteItem",key);
        CertificateItem item = certificateItemController.getItem(key);
        if (item != null) {
            Command command = new CertificateCommand(
                    certificateItemController,
                    key,
                    CertificateCommand.CommandType.DELETE,
                    null,
                    null
            );
            executeCommand(command);
        }
    }
}