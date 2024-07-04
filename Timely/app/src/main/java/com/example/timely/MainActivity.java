package com.example.timely;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Launch WelcomeActivity
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);

        // Finish MainActivity so it doesn't remain in the back stack
        finish();
    }
}
