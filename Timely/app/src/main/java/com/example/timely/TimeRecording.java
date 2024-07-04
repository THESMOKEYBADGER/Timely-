package com.example.timely;

import com.google.firebase.firestore.PropertyName;

public class TimeRecording {
    private String userId;
    private String categoryName; // Changed from "categoryName"
    private int recordedTime;

    private String photoPath;

    public TimeRecording() {
        // Default constructor required for Firestore deserialization
    }

    public TimeRecording(String userId, String categoryName, int recordedTime, String photoPath) {
        this.userId = userId;
        this.categoryName = categoryName;
        this.recordedTime = recordedTime;
        this.photoPath = photoPath;
    }


    public String getUserId() {
        return userId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public int getRecordedTime() {
        return recordedTime;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
}
