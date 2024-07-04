package com.example.timely;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class EntriesActivity extends AppCompatActivity {

    // Declare UI elements
    private RecyclerView recyclerView;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Current user ID
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entries);

        // Initialize UI elements
        recyclerView = findViewById(R.id.recycler_view_entries); // Initialize RecyclerView

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get current user ID
        userId = mAuth.getCurrentUser().getUid();

        // Log the user ID
        Log.d("EntriesActivity", "User ID: " + userId);

        // Set layout manager for RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Refresh RecyclerView
        refreshRecyclerView();
    }

    // Method to fetch entries from Firestore and set them to RecyclerView
    private void refreshRecyclerView() {
        db.collection("timeRecordings")
                .whereEqualTo("userId", userId)
                .orderBy("recordedTime", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<TimeRecording> entries = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String categoryName = document.getString("categoryName"); // Get the category name directly
                            int recordedTime = document.getLong("recordedTime").intValue();
                            String photoPath = document.getString("photoPath");

                            // Add new entry to the list
                            TimeRecording entry = new TimeRecording(userId, categoryName, recordedTime, photoPath);
                            entries.add(entry);
                        }

                        // Set the adapter for RecyclerView
                        EntriesAdapter adapter = new EntriesAdapter(entries);
                        recyclerView.setAdapter(adapter);

                        if (entries.isEmpty()) {
                            Toast.makeText(this, "No entries found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("EntriesActivity", "Error getting documents: ", task.getException());
                        Toast.makeText(this, "Error fetching entries", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
