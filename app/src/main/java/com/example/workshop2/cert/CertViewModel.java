package com.example.workshop2.cert;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.util.Base64;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.workshop2.cert.model.GeneratedCert;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// CertViewModel.java
public class CertViewModel extends ViewModel {
    private final FirebaseFirestore db;
    private final Map<String, String> eventNameCache;

    public CertViewModel() {
        db = FirebaseFirestore.getInstance();
        eventNameCache = new HashMap<>();
    }

    public LiveData<List<GeneratedCert>> getCertificates(String participantId) {
        MutableLiveData<List<GeneratedCert>> certificatesLiveData = new MutableLiveData<>();

        db.collection("certificates")
                .whereEqualTo("participantId", participantId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<GeneratedCert> certificates = new ArrayList<>();
                    List<Task<Void>> eventNameTasks = new ArrayList<>();

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        GeneratedCert certificate = document.toObject(GeneratedCert.class);
                        if (certificate != null) {
                            certificate.setId(document.getId());
                            certificates.add(certificate);

                            // Fetch event name if not in cache
                            if (!eventNameCache.containsKey(certificate.getEventId())) {
                                Task<Void> task = fetchEventName(certificate.getEventId());
                                eventNameTasks.add(task);
                            }
                        }
                    }

                    // Wait for all event names to be fetched
                    Tasks.whenAllComplete(eventNameTasks)
                            .addOnSuccessListener(tasks -> certificatesLiveData.setValue(certificates))
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", "Error fetching event names", e);
                                certificatesLiveData.setValue(certificates);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting certificates", e);
                    certificatesLiveData.setValue(new ArrayList<>());
                });

        return certificatesLiveData;
    }

    private Task<Void> fetchEventName(String eventId) {
        return db.collection("events")
                .document(eventId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String eventName = task.getResult().getString("name");
                        if (eventName != null) {
                            eventNameCache.put(eventId, eventName);
                        } else {
                            eventNameCache.put(eventId, "Unknown Event");
                        }
                    } else {
                        eventNameCache.put(eventId, "Unknown Event");
                    }
                    return null;
                });
    }

    public String getEventName(String eventId) {
        return eventNameCache.getOrDefault(eventId, "Loading...");
    }

    public LiveData<byte[]> downloadCertificate(String certificateId) {
        MutableLiveData<byte[]> imageLiveData = new MutableLiveData<>();

        db.collection("certificates")
                .document(certificateId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fileContent = documentSnapshot.getString("fileContent");

                        if (fileContent != null) {
                            byte[] decodedBytes = Base64.decode(fileContent, Base64.DEFAULT);
                            imageLiveData.setValue(decodedBytes);
                        } else {
                            imageLiveData.setValue(null);
                        }
                    } else {
                        imageLiveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error downloading certificate", e);
                    imageLiveData.setValue(null);
                });

        return imageLiveData;
    }
}




