package com.example.workshop2.cert;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop2.R;
import com.example.workshop2.cert.model.GeneratedCert;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.auth.FirebaseAuth;

public class ViewCertListActivity extends AppCompatActivity implements CertificateCallbacks {

    private CertAdapter adapter;
    private CertViewModel viewModel;
    private final String participantId = FirebaseAuth.getInstance().getCurrentUser().getUid();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_cert_list);

        RecyclerView rv = findViewById(R.id.rvCertList);
        ProgressBar pb = findViewById(R.id.pbCertList);

        viewModel = new ViewModelProvider(this).get(CertViewModel.class);

        adapter = new CertAdapter(this, participantId, viewModel);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        viewModel.getCertificates(participantId).observe(this, certificates -> {
            pb.setVisibility(View.GONE);
            if (certificates.isEmpty()) {
                Toast.makeText(this, "No certificates found", Toast.LENGTH_SHORT).show();
                finish();
            }
            adapter.setCertificates(certificates);
        });
    }

    @Override
    public void onCertificateClick(GeneratedCert certificate) {
        Intent intent = new Intent(this, ViewCertParticipantActivity.class);
        intent.putExtra("certificateId", certificate.getId());
        startActivity(intent);
    }

    @Override
    public void loadImagePreview(String participantId, String certificateId,
                                 ImageView imageView, ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        viewModel.downloadCertificate(certificateId)
                .observe(this, imageBytes -> {
                    progressBar.setVisibility(View.GONE);
                    if (imageBytes != null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        imageView.setImageBitmap(bitmap);
                    } else {
                        Toast.makeText(this, "Failed to load preview", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}