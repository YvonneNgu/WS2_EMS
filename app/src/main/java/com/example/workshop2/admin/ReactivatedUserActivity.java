package com.example.workshop2.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop2.R;
import com.example.workshop2.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ReactivatedUserActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageView noDeactivatedUsersImageView;
    private ReactivateUserAdapter adapter;
    private ArrayList<User> deactivatedUsers = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reactivated_user);

        recyclerView = findViewById(R.id.recyclerViewDeactivatedUsers);
        noDeactivatedUsersImageView = findViewById(R.id.noDeactivatedUsersImageView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Pass the refresh callback to the adapter
        adapter = new ReactivateUserAdapter(deactivatedUsers, this::fetchDeactivatedUsers);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        fetchDeactivatedUsers();
    }

    private void fetchDeactivatedUsers() {
        db.collection("users")
                .whereEqualTo("userStatus", "deactivated")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    deactivatedUsers.clear();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        User user = document.toObject(User.class);
                        deactivatedUsers.add(user);
                    }

                    // Toggle visibility based on user list
                    if (deactivatedUsers.isEmpty()) {
                        noDeactivatedUsersImageView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        noDeactivatedUsersImageView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(ReactivatedUserActivity.this, "Error fetching users: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
