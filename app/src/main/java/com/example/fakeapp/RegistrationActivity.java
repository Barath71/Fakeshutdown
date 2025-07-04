package com.example.fakeapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegistrationActivity extends AppCompatActivity {

    EditText editPhone, editEmail, editPassword;
    Button btnRegister;
    private TextView tvlogin;

    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        editPhone = findViewById(R.id.editTextPhone);
        editEmail = findViewById(R.id.GetEmail);
        editPassword = findViewById(R.id.editPassword);
        btnRegister = findViewById(R.id.button);
        tvlogin = findViewById(R.id.textView4);

        db = AppDatabase.getInstance(this);
        btnRegister.setOnClickListener(v -> {
            String phone = editPhone.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            // ❌ Empty check
            if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            // 📱 Phone number validation
            if (!Patterns.PHONE.matcher(phone).matches() || phone.length() < 10) {
                Toast.makeText(this, "Enter a valid phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            // 📧 Email validation
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔐 Password validation (minimum 6 characters)
            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Do DB operations in background thread
            new Thread(() -> {
                if (db.userDao().getUserByEmail(email) != null) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "User already registered", Toast.LENGTH_SHORT).show()
                    );
                } else {
                    User user = new User();
                    user.phone = phone;
                    user.email = email;
                    user.password = password;
                    db.userDao().insert(user);

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                        finish(); // Close activity or go to login
                    });
                }
            }).start();
        });

        tvlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                // Navigate to RegistrationActivity
                Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
