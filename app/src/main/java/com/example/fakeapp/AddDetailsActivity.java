package com.example.fakeapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class AddDetailsActivity extends AppCompatActivity {

    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_CONTACT_PERMISSION = 1;

    private Button addContactBtn, addEmailBtn, checkContactBtn, checkEmailbtn;

    private EditText emailField;
    private SessionManager session;
    private int currentUserId;

    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        addContactBtn = findViewById(R.id.AddContact);
        addEmailBtn = findViewById(R.id.AddEmail);
        emailField = findViewById(R.id.GetEmail);
        checkContactBtn = findViewById(R.id.CheckContact);
        checkEmailbtn = findViewById(R.id.CheckEmail);
        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);
        currentUserId = session.getUserId();


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(AddDetailsActivity.this, HomeActivity.class);
                startActivity(intent);
                return true;
            }
            if(id == R.id.nav_feedback){
                Intent intent = new Intent(AddDetailsActivity.this, Feedback.class);
                startActivity(intent);
                return true;
            }
            if(id == R.id.nav_settings){
                Intent intent = new Intent(AddDetailsActivity.this, Settings.class);
                startActivity(intent);
                return true;
            }

            // Handle other nav items if needed

            return true;
        });

        addContactBtn.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CONTACT_PERMISSION);
            } else {
                pickContact();
            }
        });

        checkContactBtn.setOnClickListener(v -> checkSavedContacts());
        checkEmailbtn.setOnClickListener(v -> {
            new Thread(() -> {
                List<EmailEntity> emailList = db.userDao().getEmailsByUserId(currentUserId);


                runOnUiThread(() -> {
                    if (emailList.isEmpty()) {
                        Toast.makeText(AddDetailsActivity.this, "No emails found", Toast.LENGTH_SHORT).show();
                    } else {
                        // Displaying in a Toast or use AlertDialog for better UX
                        StringBuilder emails = new StringBuilder("Registered Emails:\n");
                        for (EmailEntity email : emailList) {
                            emails.append(email).append("\n");
                        }

                        Toast.makeText(AddDetailsActivity.this, emails.toString(), Toast.LENGTH_LONG).show();
                    }
                });
            }).start();
        });



        addEmailBtn.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }


            EmailEntity emailEntity = new EmailEntity();
            emailEntity.email = email;
            emailEntity.userId = currentUserId;


            new Thread(() -> db.userDao().insertEmail(emailEntity)).start();
            Toast.makeText(this, "Email added", Toast.LENGTH_SHORT).show();
            emailField.setText("");
        });
    }

    private void pickContact() {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, 2); // You can use any request code
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        if (requestCode == 2 && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                String name = cursor.getString(nameIndex);
                String number = cursor.getString(numberIndex);
                cursor.close();

                // Save to Room DB
                ContactEntity contact = new ContactEntity();
                contact.name = name;
                contact.phone = number;
                contact.userId = currentUserId;

                new Thread(() -> {
                    AppDatabase.getInstance(this).userDao().insertContact(contact);
                    runOnUiThread(() -> Toast.makeText(this, "Contact saved", Toast.LENGTH_SHORT).show());
                }).start();
            }
        }
    }


    private void checkSavedContacts() {
        new Thread(() -> {
            List<ContactEntity> contacts = db.userDao().getContactsByUserId(currentUserId);
            StringBuilder message = new StringBuilder();

            if (contacts.isEmpty()) {
                message.append("No contacts saved.");
            } else {
                for (ContactEntity contact : contacts) {
                    message.append("Name: ").append(contact.name)
                            .append("\nPhone: ").append(contact.phone)
                            .append("\n\n");
                }
            }

            runOnUiThread(() -> {
                Toast.makeText(this, message.toString(), Toast.LENGTH_LONG).show();
            });
        }).start();
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CONTACT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickContact();
            } else {
                Toast.makeText(this, "Permission denied to access contacts", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
