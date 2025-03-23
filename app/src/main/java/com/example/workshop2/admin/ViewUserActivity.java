package com.example.workshop2.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop2.R;
import com.example.workshop2.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ViewUserActivity extends AppCompatActivity {

    private RecyclerView usersRecyclerView;
    private ViewUserAdapter viewUserAdapter;
    private ArrayList<User> userList;
    private FirebaseFirestore db;
    private ImageView noUsersImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_admin);

        // Initialize RecyclerView, Firestore, and ImageView
        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        noUsersImageView = findViewById(R.id.noUsersImageView);
        userList = new ArrayList<>();
        viewUserAdapter = new ViewUserAdapter(this, userList);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(viewUserAdapter);
        db = FirebaseFirestore.getInstance();

        // Fetch users from Firestore
        fetchUsers();
    }

    private void fetchUsers() {
        db.collection("users")
                .whereEqualTo("userType", "organizer") // Filter for users with userType "Organizer"
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    queryDocumentSnapshots.getDocuments().forEach(document -> {
                        User user = document.toObject(User.class);
                        if (user != null) {
                            userList.add(user);
                        }
                    });

                    // Show or hide the "no user" image
                    if (userList.isEmpty()) {
                        noUsersImageView.setVisibility(View.VISIBLE);
                        usersRecyclerView.setVisibility(View.GONE);
                    } else {
                        noUsersImageView.setVisibility(View.GONE);
                        usersRecyclerView.setVisibility(View.VISIBLE);
                    }

                    viewUserAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Handle failure (Optional)
                    e.printStackTrace();
                });
    }
}
