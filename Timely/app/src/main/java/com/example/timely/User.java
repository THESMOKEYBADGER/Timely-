package com.example.timely;

import java.util.UUID;

public class User {
    private String userId;
    private String username;
    private String email;
    private String password;

    // Empty constructor required for Firestore serialization
    public User() {}

    public User(String username, String email, String password) {
        this.userId = UUID.randomUUID().toString();
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
