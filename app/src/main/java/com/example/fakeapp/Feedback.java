package com.example.fakeapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Feedback extends AppCompatActivity {

    EditText feedbackText;
    RatingBar ratingBar;
    Button submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        feedbackText = findViewById(R.id.feedback_text);
        submitBtn = findViewById(R.id.submit_feedback);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_device) {
                Intent intent = new Intent(Feedback.this, AddDetailsActivity.class);
                startActivity(intent);
                return true;
            }
            if(id == R.id.nav_home){
                Intent intent = new Intent(Feedback.this, HomeActivity.class);
                startActivity(intent);
                return true;
            }
            if(id == R.id.nav_settings){
                Intent intent = new Intent(Feedback.this, Settings.class);
                startActivity(intent);
                return true;
            }

            // Handle other nav items if needed

            return true;
        });

        submitBtn.setOnClickListener(view -> {
            String feedback = feedbackText.getText().toString();

            if (!feedback.isEmpty()) {
                // TODO: Send to server or save locally
                Toast.makeText(this, "Thanks for your feedback!", Toast.LENGTH_SHORT).show();
                feedbackText.setText("");
            } else {
                Toast.makeText(this, "Please enter your feedback", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
