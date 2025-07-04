package com.example.fakeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Settings extends AppCompatActivity {

    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sessionManager = new SessionManager(this);

        Button logout = findViewById(R.id.btn_logout);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sessionManager.clear(); // Resets session count and login data
                Intent intent = new Intent(Settings.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_device) {
                Intent intent = new Intent(Settings.this, AddDetailsActivity.class);
                startActivity(intent);
                return true;
            }
            if(id == R.id.nav_home){
                Intent intent = new Intent(Settings.this, HomeActivity.class);
                startActivity(intent);
                return true;
            }
            if(id == R.id.nav_feedback){
                Intent intent = new Intent(Settings.this, Feedback.class);
                startActivity(intent);
                return true;
            }

            // Handle other nav items if needed

            return true;
        });

        // Handle other buttons (for demonstration)
        findViewById(R.id.btn_feedback).setOnClickListener(v -> startActivity(new Intent(this, Feedback.class)));
        findViewById(R.id.btn_device).setOnClickListener(v -> startActivity(new Intent(this, AddDetailsActivity.class)));
        // Add similar for Account, Privacy, etc.
    }
}
