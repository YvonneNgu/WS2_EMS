package com.example.workshop2.cert;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop2.R;
import com.example.workshop2.cert.model.CertificateItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.util.Objects;

public class EditCertContentActivity extends AppCompatActivity {

    private static final String TAG = "EditCertContentActivity";
    private Map<String, View> itemViews = new HashMap<>();
    private Map<String, String> contentTexts = new HashMap<>();
    private LinearLayout contentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_cert_content);

        contentContainer = findViewById(R.id.contentContainer);
        initializeViews();
        loadContentFromIntent();

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveAndReturn());
    }

    private void initializeViews() {
        String[] keys = getResources().getStringArray(R.array.certificate_items);
        for (String key : keys) {
            View itemView = getLayoutInflater().inflate(R.layout.item_cert_content, contentContainer, false);
            CheckBox checkBox = itemView.findViewById(R.id.checkBox);
            TextView textView = itemView.findViewById(R.id.textView);
            ImageButton editButton = itemView.findViewById(R.id.editButton);
            ImageButton saveButton = itemView.findViewById(R.id.saveButton);

            checkBox.setText("");
            textView.setText(getTextByKey(key));
            editButton.setOnClickListener(v -> toggleEdit(key));
            saveButton.setOnClickListener(v -> saveEdit(key));

            contentContainer.addView(itemView);
            itemViews.put(key, itemView);
        }
    }

    private void loadContentFromIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra("certificateItems")) {
            HashMap<String, CertificateItem> receivedItems = (HashMap<String, CertificateItem>) intent.getSerializableExtra("certificateItems");
            for (String key : itemViews.keySet()) {
                View itemView = itemViews.get(key);
                Log.d("get key","Key: "+key);

                if (itemView != null) {
                    CheckBox checkBox = itemView.findViewById(R.id.checkBox);
                    TextView textView = itemView.findViewById(R.id.textView);
                    if (receivedItems != null && receivedItems.containsKey(key)) {
                        CertificateItem item = receivedItems.get(key);
                        checkBox.setChecked(true);
                        Log.d("set checkbox true","");
                        textView.setText(item.getText());
                        contentTexts.put(key, item.getText());
                    } else {
                        checkBox.setChecked(false);
                        Log.d("set checkbox false","");
                        String defaultText = getTextByKey(key);
                        Log.d("get default text","text: "+defaultText);
                        textView.setText(defaultText);
                        contentTexts.put(key, defaultText);
                    }
                }
            }
        }
    }

    private void toggleEdit(String key) {
        View itemView = itemViews.get(key);
        if (itemView != null) {
            Log.d("item view not null","");
            TextView textView = itemView.findViewById(R.id.textView);
            EditText editText = itemView.findViewById(R.id.editText);
            ImageButton editButton = itemView.findViewById(R.id.editButton);
            ImageButton saveButton = itemView.findViewById(R.id.saveButton);

            if (editText.getVisibility() == View.GONE) {
                Log.d("edittext invisible","");
                // Switch to edit mode
                textView.setVisibility(View.GONE);
                editText.setVisibility(View.VISIBLE);
                editText.setText(textView.getText());
                editButton.setVisibility(View.GONE);
                saveButton.setVisibility(View.VISIBLE);
            } else {
                // Switch to view mode
                saveEdit(key);
            }
        }
    }

    private void saveEdit(String key) {
        Log.d("save edit","");
        View itemView = itemViews.get(key);
        if (itemView != null) {
            TextView textView = itemView.findViewById(R.id.textView);
            EditText editText = itemView.findViewById(R.id.editText);
            ImageButton editButton = itemView.findViewById(R.id.editButton);
            ImageButton saveButton = itemView.findViewById(R.id.saveButton);

            String newText = editText.getText().toString();
            textView.setText(newText);
            contentTexts.put(key, newText);

            editText.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.VISIBLE);
        }
    }

    private void saveAndReturn() {
        Intent resultIntent = new Intent();
        HashMap<String, CertificateItem> updatedItems = new HashMap<>();
        String[] keys = getResources().getStringArray(R.array.certificate_items);

        for (String key : keys) {
            View itemView = itemViews.get(key);
            if (itemView != null) {
                CheckBox checkBox = itemView.findViewById(R.id.checkBox);
                if (checkBox.isChecked()) {
                    String text = contentTexts.get(key);
                    CertificateItem item;
                    int templateWidth = getIntent().getIntExtra("templateWidth",0);
                    int templateHeight = getIntent().getIntExtra("templateHeight",0);
                    //item = new CertificateItem(key,text,templateWidth,templateHeight);
                    //updatedItems.put(key, item);
                }
            }
        }

        resultIntent.putExtra("certificateItems", updatedItems);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private String getTextByKey(String key) {
        String certificateType = getIntent().getStringExtra("certificateType");
        switch (key) {
            case "certificateType":
                return "CERTIFICATE OF " + (certificateType != null ? certificateType.toUpperCase() : "");
            case "presentedTo":
                return "This certificate is proudly presented to";
            case "participantName":
                return "[PARTICIPANT NAME]";
            case "certificateContent":
                if(Objects.equals(certificateType, "Participation"))
                    return "for attending and participating in";
                else if(Objects.equals(certificateType, "Achievement"))
                    return "for outstanding accomplishment in";
            case "achievementDescription":
                return "[ACHIEVEMENT DESCRIPTION]";
            case "eventDetails":
                String eventName = getIntent().getStringExtra("eventName");
                String eventDate = getIntent().getStringExtra("eventDate");
                return (eventName != null ? eventName : "") + " on " + (eventDate != null ? eventDate : "");
            case "issueDate":
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
                return sdf.format(new Date());
            default:
                return "";
        }
    }
}

