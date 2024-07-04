package com.example.timely;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyDatabaseHelper {

    private static final String TAG = "MyDatabaseHelper";
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    public MyDatabaseHelper() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public void insertUser(User user, OnCompleteListener<Void> listener) {
        mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("username", user.getUsername());
                            userMap.put("email", user.getEmail());

                            db.collection("users").document(userId)
                                    .set(userMap)
                                    .addOnCompleteListener(listener);
                        } else {
                            listener.onComplete(null);
                        }
                    } else {
                        listener.onComplete(null);
                    }
                })
                .addOnFailureListener(e -> listener.onComplete(null));
    }

    public void checkUserCredentials(String usernameOrEmail, String password, OnCompleteListener<Boolean> listener) {
        mAuth.signInWithEmailAndPassword(usernameOrEmail, password)
                .addOnCompleteListener(task -> {
                    listener.onComplete(Tasks.forResult(task.isSuccessful()));
                })
                .addOnFailureListener(e -> listener.onComplete(Tasks.forResult(false)));
    }

    public String getCurrentUserId() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public void insertCategory(Category category, OnCompleteListener<DocumentReference> listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("user_id", userId);
            categoryMap.put("category_id", category.getCategoryName());
            categoryMap.put("goal_time", category.getGoalTime());


            db.collection("categories")
                    .add(categoryMap)
                    .addOnCompleteListener(listener);
        }
    }

    public void insertTimeRecording(TimeRecording timeRecording, OnCompleteListener<DocumentReference> listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            String categoryName = timeRecording.getCategoryName(); // Get the category name
            int recordedTime = timeRecording.getRecordedTime();

            // Create a map to store the time recording details
            Map<String, Object> recordingMap = new HashMap<>();
            recordingMap.put("user_id", userId);
            recordingMap.put("categoryName", categoryName); // Store category name instead of category ID
            recordingMap.put("recorded_time", recordedTime);

            // Insert the new time recording
            db.collection("time_recordings")
                    .add(recordingMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Get the ID of the newly added time recording
                            String timeRecordingId = task.getResult().getId();

                        } else {
                            // Notify the original listener about the failure
                            listener.onComplete(null);
                        }
                    });
        }
    }
    public interface TimeRecordingsCallback {
        void onTimeRecordingsLoaded(List<TimeRecording> recordings);
        void onFailure(Exception e);
    }

    public void getUserSpecificTimeRecordings(String userId, TimeRecordingsCallback callback) {
        db.collection("timeRecordings")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<TimeRecording> recordings = task.getResult().toObjects(TimeRecording.class);
                        callback.onTimeRecordingsLoaded(recordings);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }



    public void getUserIdByUsernameAndPassword(String usernameOrEmail, String password, OnCompleteListener<String> listener) {
        mAuth.signInWithEmailAndPassword(usernameOrEmail, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        listener.onComplete(Tasks.forResult(user != null ? user.getUid() : null));
                    } else {
                        listener.onComplete(Tasks.forResult(null));
                    }
                });
    }
}
