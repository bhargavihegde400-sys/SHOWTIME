package com.example.mini;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone;
    private DatabaseHelper dbHelper;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        userEmail = getIntent().getStringExtra("user_email");
        if (userEmail == null || userEmail.isEmpty()) {
            userEmail = "user@showtime.com";
        }
        dbHelper = new DatabaseHelper(this);

        etName = findViewById(R.id.etProfileName);
        etEmail = findViewById(R.id.etProfileEmail);
        etPhone = findViewById(R.id.etProfilePhone);
        Button btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnExitApp = findViewById(R.id.btnExitApp);

        loadUserData();

        btnUpdateProfile.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(SettingsActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean updated = dbHelper.updateUserProfile(userEmail, name, phone);
            if (updated) {
                Toast.makeText(SettingsActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SettingsActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent);
            finishAffinity();
        });

        btnExitApp.setOnClickListener(v -> {
            finishAffinity();
            System.exit(0);
        });
    }

    private void loadUserData() {
        Cursor cursor = dbHelper.getUserProfile(userEmail);
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
            cursor.close();

            etName.setText(name);
            etEmail.setText(email);
            etPhone.setText(phone);
        }
    }
}
