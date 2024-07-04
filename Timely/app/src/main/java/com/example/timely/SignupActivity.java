package com.example.timely;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signupButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize views and Firebase instances
        usernameEditText = findViewById(R.id.signup_username_et);
        emailEditText = findViewById(R.id.signup_email_et);
        passwordEditText = findViewById(R.id.signup_password_et);
        signupButton = findViewById(R.id.signup_signup_btn);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set OnClickListener for signupButton
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get user input
                String username = usernameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                // Check if any field is empty
                if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Register user with Firebase Auth
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                    if (firebaseUser != null) {
                                        String userId = firebaseUser.getUid();
                                        User newUser = new User(username, email, password);
                                        newUser.setUserId(userId);

                                        Map<String, Object> userMap = new HashMap<>();
                                        userMap.put("username", newUser.getUsername());
                                        userMap.put("email", newUser.getEmail());
                                        userMap.put("password", newUser.getPassword()); // Store the hashed password

                                        db.collection("users").document(userId)
                                                .set(userMap)
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        Toast.makeText(SignupActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();

                                                        // Proceed to the home page and pass the userId
                                                        Intent intent = new Intent(SignupActivity.this, HomePageActivity.class);
                                                        intent.putExtra("userId", userId); // Pass the userId to HomePageActivity
                                                        startActivity(intent);

                                                        finish(); // Finish the activity
                                                    } else {
                                                        Toast.makeText(SignupActivity.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                } else {
                                    Toast.makeText(SignupActivity.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }
}
