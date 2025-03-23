package com.example.workshop2.organiser;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.workshop2.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class InputCardDetailsActivity extends AppCompatActivity {

    private EditText etEventName, etStaffName, etPosition, etContactNo;
    private Button btnPreview;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_card_details);

        // Initialize Firebase Storage - Remove the full URL
        storageRef = FirebaseStorage.getInstance().getReference();

        // Initialize views
        etEventName = findViewById(R.id.etEventName);
        etStaffName = findViewById(R.id.etStaffName);
        etPosition = findViewById(R.id.etPosition);
        etContactNo = findViewById(R.id.etContactNo);
        btnPreview = findViewById(R.id.btnPreview);

        btnPreview.setOnClickListener(v -> previewCard());
    }

    private void previewCard() {
        String eventName = etEventName.getText().toString().trim();
        String staffName = etStaffName.getText().toString().trim();
        String position = etPosition.getText().toString().trim();
        String contactNo = etContactNo.getText().toString().trim();

        if (eventName.isEmpty() || staffName.isEmpty() || position.isEmpty() || contactNo.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pass the details to PrintCardActivity
        Intent intent = new Intent(this, PrintCardActivity.class);
        intent.putExtra("eventName", eventName);
        intent.putExtra("staffName", staffName);
        intent.putExtra("position", position);
        intent.putExtra("contactNo", contactNo);
        startActivity(intent);
    }
}