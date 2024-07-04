package com.example.timely;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomePageActivity extends AppCompatActivity {

    public static final int BUTTON_CATEGORIES = R.id.button_categories;
    private TextView timerDisplay;
    private CountDownTimer countDownTimer;
    private boolean timerRunning;
    private long startTime;
    private long elapsedTime;
    private FirebaseFirestore db;

    // Initialize Firebase Authentication
    private FirebaseAuth mAuth;
    private String userId; // Variable to store the current user ID
    private Spinner categoryDropdown;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private String photoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        ImageButton selectPhotoButton = findViewById(R.id.uploadImage);
        selectPhotoButton.setOnClickListener(v -> showPhotoOptions());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        timerDisplay = findViewById(R.id.timer_display);

        findViewById(R.id.timer_start).setOnClickListener(v -> startTimer());
        findViewById(R.id.timer_stop).setOnClickListener(v -> stopTimer());

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Get the current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is signed in, retrieve the user ID
            userId = currentUser.getUid();
            // Log the user ID
            Log.d("HomePageActivity", "User ID: " + userId);
        } else {
            // User is not signed in, handle accordingly (e.g., redirect to login)
            Log.d("HomePageActivity", "User not signed in");
            // Example: Redirect to login activity
            startActivity(new Intent(this, LoginActivity.class));
            finish(); // Finish the current activity to prevent going back
        }

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Get the Spinner instance
        categoryDropdown = findViewById(R.id.category_dropdown_menu);

        // Populate the category dropdown menu
        refreshCategoryDropdown();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Log the user ID every time the activity resumes
        Log.d("HomePageActivity", "User ID: " + userId);
        // Refresh the category dropdown menu every time the activity is resumed
        refreshCategoryDropdown();
    }

    // Method to refresh the category dropdown menu
    private void refreshCategoryDropdown() {
        // Fetch categories from Firestore
        db.collection("categories")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> categoryNames = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            categoryNames.add(document.getString("categoryName"));
                        }
                        // Create an ArrayAdapter using the category names
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);

                        // Specify the layout to use when the list of choices appears
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        // Apply the adapter to the Spinner
                        categoryDropdown.setAdapter(adapter);
                    } else {
                        Toast.makeText(HomePageActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("HomePageActivity", "onCreateOptionsMenu called");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.button_categories:
                openCategoriesActivity();
                return true;
            case R.id.entries: // Handle the "Entries" button click
                openEntriesActivity();
                return true;

            case R.id.report: // Handle the "Entries" button click
                openReportActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Method to open CategoriesActivity
    private void openCategoriesActivity() {
        Intent intent = new Intent(this, CategoryActivity.class);
        intent.putExtra("userId", userId); // Pass userId to CategoryActivity
        startActivity(intent);
    }

    private void openEntriesActivity() {
        Intent intent = new Intent(this, EntriesActivity.class);
        intent.putExtra("userId", userId); // Pass userId to EntriesActivity
        startActivity(intent);
    }

    private void openReportActivity() {
        Intent intent = new Intent(this, ReportActivity.class);
        intent.putExtra("userId", userId); // Pass userId to EntriesActivity
        startActivity(intent);
    }

    private void startTimer() {
        if (!timerRunning) {
            if (isCategorySelected()) {
                startTime = System.currentTimeMillis();
                countDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        elapsedTime = System.currentTimeMillis() - startTime;
                        updateTimerDisplay();
                    }

                    @Override
                    public void onFinish() {
                        // Timer finished
                    }
                };
                countDownTimer.start();
                timerRunning = true;
            } else {
                Toast.makeText(this, "Please select a category before starting the timer.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isCategorySelected() {
        return categoryDropdown.getSelectedItem() != null;
    }

    private void stopTimer() {
        if (timerRunning) {
            countDownTimer.cancel();
            timerRunning = false;

            // Fetch the selected category from the dropdown menu
            String selectedCategoryName = categoryDropdown.getSelectedItem().toString();

            // Log the captured entry information
            Log.d("HomePageActivity", "User ID: " + userId + ", Category: " + selectedCategoryName + ", Recorded Time: " + elapsedTime + ", Photo Path:" + photoPath);

            // Fetch the category document directly
            db.collection("categories")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("categoryName", selectedCategoryName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Get the first document (assuming there's only one matching category)
                            DocumentSnapshot categoryDocument = task.getResult().getDocuments().get(0);
                            String categoryId = categoryDocument.getId(); // Get the category ID
                            // Extract the category name
                            String categoryName = categoryDocument.getString("categoryName");

                            // Create a TimeRecording object with the category name and photo path
                            TimeRecording timeRecording = new TimeRecording(userId, categoryName, (int) (elapsedTime / 1000), photoPath);

                            // Store the TimeRecording object in Firestore
                            db.collection("timeRecordings")
                                    .add(timeRecording)
                                    .addOnSuccessListener(documentReference -> {
                                        Log.d("HomePageActivity", "Entry recorded successfully. Document ID: " + documentReference.getId());
                                        // Reset photoPath after successful recording
                                        photoPath = null;
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("HomePageActivity", "Failed to record entry", e);
                                    });
                        } else {
                            Log.e("HomePageActivity", "Failed to find category");
                        }
                    });
        }
    }

    private void updateTimerDisplay() {
        int seconds = (int) (elapsedTime / 1000) % 60;
        int minutes = (int) ((elapsedTime / 1000) / 60);
        int hours = (int) ((elapsedTime / (1000 * 60 * 60)) % 24);
        String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        timerDisplay.setText(timeFormatted);
    }

    private void showPhotoOptions() {
        CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo");
        builder.setItems(options, (dialog, which) -> {
            if (options[which].equals("Take Photo")) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            } else if (options[which].equals("Choose from Gallery")) {
                Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
            } else if (options[which].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Handling camera image
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                // Save the bitmap to a file and get its path
                Uri tempUri = getImageUri(this, imageBitmap);
                photoPath = getRealPathFromURI(tempUri);
                Log.d("HomePageActivity", "Photo path from camera: " + photoPath);
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                // Handling gallery image
                Uri selectedImage = data.getData();
                photoPath = getRealPathFromURI(selectedImage);
                Log.d("HomePageActivity", "Photo path from gallery: " + photoPath);
            }
        }
    }

    // Helper method to get URI from bitmap
    private Uri getImageUri(Context context, Bitmap image) {
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), image, "Title", null);
        return Uri.parse(path);
    }

    // Helper method to get the real file path from URI
    private String getRealPathFromURI(Uri uri) {
        String path = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            path = cursor.getString(columnIndex);
            cursor.close();
        }
        return path;
    }
}
