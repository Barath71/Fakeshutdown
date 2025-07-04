package com.example.fakeapp;


import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import android.provider.Settings;


import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Get the button
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        Button btnAccessibility = findViewById(R.id.btn_accessibility);
        btnAccessibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            }
        });


        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_device) {
                Intent intent = new Intent(HomeActivity.this, AddDetailsActivity.class);
                startActivity(intent);
                return true;
            }
            if(id == R.id.nav_feedback){
                Intent intent = new Intent(HomeActivity.this, Feedback.class);
                startActivity(intent);
                return true;
            }
            if(id == R.id.nav_settings){
                Intent intent = new Intent(HomeActivity.this, com.example.fakeapp.Settings.class);
                startActivity(intent);
                return true;
            }
            // Handle other nav items if needed

            return true;
        });

    }
}

