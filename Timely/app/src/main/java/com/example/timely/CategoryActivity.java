package com.example.timely;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    // Declare UI elements
    private EditText categoryNameEditText;
    private EditText goalTimeEditText;
    private Button createCategoryButton;
    private RecyclerView recyclerView; // Add RecyclerView

    // Firebase Firestore instance
    private FirebaseFirestore db;
    private String userId;
    private List<Category> categories; // List to store categories
    private CategoryAdapter adapter; // Adapter for RecyclerView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // Initialize UI elements
        categoryNameEditText = findViewById(R.id.edit_text_category_name);
        goalTimeEditText = findViewById(R.id.edit_text_goal_time);
        createCategoryButton = findViewById(R.id.button_create_category);
        recyclerView = findViewById(R.id.recycler_view_categories); // Initialize RecyclerView

        // Get user ID from intent
        userId = getIntent().getStringExtra("userId");

        // Log the user ID
        Log.d("CategoryActivity", "User ID: " + userId);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Set layout manager for RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize categories list and adapter
        categories = new ArrayList<>();
        adapter = new CategoryAdapter(categories);
        recyclerView.setAdapter(adapter);

        createCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get user input
                String categoryName = categoryNameEditText.getText().toString();
                String goalTimeString = goalTimeEditText.getText().toString();

                // Check if any of the fields are empty
                if (categoryName.isEmpty() || goalTimeString.isEmpty()) {
                    Toast.makeText(CategoryActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Parse goal time to integer
                    int goalTime = Integer.parseInt(goalTimeString);

                    // You need to obtain the photoPath from somewhere and pass it here
                    String photoPath = ""; // Replace this with the actual photo path

                    // Create Category object
                    Category newCategory = new Category(userId, categoryName, goalTime, photoPath);

                    // Insert category into Firestore
                    db.collection("categories")
                            .add(newCategory)
                            .addOnSuccessListener(documentReference -> {
                                // Initialize accumulatedTime to 0 when the category is created
                                documentReference.update("accumulatedTime", 0)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("CategoryActivity", "Category created successfully. Document ID: " + documentReference.getId());
                                            Toast.makeText(CategoryActivity.this, "Category created successfully", Toast.LENGTH_SHORT).show();
                                            // Refresh RecyclerView
                                            refreshRecyclerView();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("CategoryActivity", "Failed to set accumulatedTime", e);
                                            Toast.makeText(CategoryActivity.this, "Failed to create category. Please try again.", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("CategoryActivity", "Failed to create category", e);
                                Toast.makeText(CategoryActivity.this, "Failed to create category. Please try again.", Toast.LENGTH_SHORT).show();
                            });

                }
            }
        });

        // Refresh RecyclerView initially
        refreshRecyclerView();
    }

    // Method to fetch categories from Firestore and set them to RecyclerView
    private void refreshRecyclerView() {
        db.collection("categories")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        categories.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Category category = document.toObject(Category.class);
                            categories.add(category);
                        }
//                        // Notify adapter about data changes
//                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("CategoryActivity", "Failed to fetch categories", task.getException());
                        Toast.makeText(CategoryActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
                    }
                });

        Category.calculateTotalTime(userId, new Category.TotalTimeCallback() {
            @Override
            public void onTotalTimeCalculated(HashMap<String, Integer> totalTime) {
                // Handle the total time calculation result here
                // For example, update your UI or perform any other operations with the calculated total time
                for (Category category : categories) {
                    String name = category.getCategoryName();
                    int accTime = 0;
                    if(totalTime.containsKey(name)) accTime = totalTime.get(name) ;
                    category.setAccumulatedTime(accTime);
                   // Log.d("Category", "percentage: " + category.toString());


                }
                adapter.notifyDataSetChanged();
                Log.d("TotalTime", "onTotalTimeCalculated: " + totalTime);
            }

            @Override
            public void onFailure(Exception e) {
                // Handle failure, such as displaying an error message
                Log.e("TotalTime", "onFailure: " + e.getMessage());
            }
        });
    }
}
