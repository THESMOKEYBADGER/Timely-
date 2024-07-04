package com.example.timely;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;

public class Category {
    private String userId;
    private String categoryName;
    private int goalTime;


    private int accumulatedTime;

    // Add a no-argument constructor
    public Category() {
        // Default constructor required for Firestore deserialization
    }

    public Category(String userId, String categoryName, int goalTime, String photoPath) {
        this.userId = userId;
        this.categoryName = categoryName;
        this.goalTime = goalTime;

    }

    public String getUserId() {
        return userId;
    }

    public String getCategoryName() {
        return categoryName;
    }
    public int getPercentage() {
        int percentage = (int) ((accumulatedTime / (float) (goalTime*60)) * 100); // Calculate the percentage

        return percentage;
    }

    public void setAccumulatedTime(int time) {
        accumulatedTime = time;
    }

    public int getGoalTime() {
        return goalTime;
    }





    public static String convertTimeRecordingsToString(List<TimeRecording> recordings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (TimeRecording recording : recordings) {
            stringBuilder.append("UserId: ").append(recording.getUserId())
                    .append(", CategoryName: ").append(recording.getCategoryName())
                    .append(", RecordedTime: ").append(recording.getRecordedTime())
                    .append("\n");
        }
        return stringBuilder.toString();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("UserId: ").append(userId)
                    .append(", CategoryName: ").append(categoryName)
                    .append(", AccumalatedTime: ").append(accumulatedTime)
                    .append(", GoalTime: ").append(goalTime)
                    .append("\n");
        return stringBuilder.toString();
    }



    public interface TotalTimeCallback {
        void onTotalTimeCalculated(HashMap<String, Integer> totalTime);
        void onFailure(Exception e);
    }

    public static void calculateTotalTime(String userId, TotalTimeCallback callback) {
        new MyDatabaseHelper().getUserSpecificTimeRecordings(userId, new MyDatabaseHelper.TimeRecordingsCallback() {
            @Override
            public void onTimeRecordingsLoaded(List<TimeRecording> recordings) {
                // Process the fetched recordings here
                HashMap<String, Integer> totalTimeMap = new HashMap<>();

                // Iterate over the list of recordings and sum the recorded times for each category
                for (TimeRecording recording : recordings) {
                    String categoryName = recording.getCategoryName();
                    int recordedTime = recording.getRecordedTime();

                    // Update the total time for the category
                    totalTimeMap.put(categoryName, totalTimeMap.getOrDefault(categoryName, 0) + recordedTime);
                }

                // Pass the result to the callback
                callback.onTotalTimeCalculated(totalTimeMap);
            }

            @Override
            public void onFailure(Exception e) {
                // Pass the failure to the callback
                callback.onFailure(e);
            }
        });
    }

    // Method to delete the category document from Firestore

    // Method to delete the category document from Firestore
    public void deleteCategoryFromFirestore(Context context, CategoryAdapter adapter, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmation")
                .setMessage("Are you sure you want to delete this category and its time recordings?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked yes button, proceed with deletion
                        deleteCategoryAndTimeRecordingsFromFirestore();

                        // Remove the item from the adapter's list
                        adapter.removeCategory(position);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked no button, do nothing
                    }
                })
                .show();
    }





    // Method to delete the category document and its associated timeRecordings from Firestore
    private void deleteCategoryAndTimeRecordingsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Query the Firestore collection to find the document where categoryName matches
        db.collection("categories")
                .whereEqualTo("categoryName", categoryName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Loop through the query result to delete the document(s) found
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Delete the document
                                document.getReference().delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> deleteTask) {
                                                if (deleteTask.isSuccessful()) {
                                                    Log.d("Category", "Category deleted successfully");
                                                    // Delete timeRecordings associated with this category
                                                    deleteTimeRecordingsFromFirestore();
                                                } else {
                                                    Log.e("Category", "Error deleting category", deleteTask.getException());
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.e("Category", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    // Method to delete all timeRecordings documents with categoryName equal to this category's name
    private void deleteTimeRecordingsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Query the Firestore collection to find the documents where categoryName matches
        db.collection("timeRecordings")
                .whereEqualTo("categoryName", categoryName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Loop through the query result to delete the documents found
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Delete the document
                                document.getReference().delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> deleteTask) {
                                                if (deleteTask.isSuccessful()) {
                                                    Log.d("Category", "TimeRecording document deleted successfully");
                                                } else {
                                                    Log.e("Category", "Error deleting TimeRecording document", deleteTask.getException());
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.e("Category", "Error getting TimeRecording documents: ", task.getException());
                        }
                    }
                });
    }
}



