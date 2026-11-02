package com.example.mini;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail, etPhone, etNewPassword;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        dbHelper = new DatabaseHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etNewPassword = findViewById(R.id.etNewPassword);
        Button btnResetSubmit = findViewById(R.id.btnResetSubmit);
        ImageView btnBackForgot = findViewById(R.id.btnBackForgot);

        btnBackForgot.setOnClickListener(v -> finish());

        btnResetSubmit.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();

            if (email.isEmpty() || phone.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(ForgotPasswordActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Cursor cursor = dbHelper.getUserProfile(email);
            if (cursor != null && cursor.moveToFirst()) {
                String registeredPhone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                cursor.close();

                if (registeredPhone != null && registeredPhone.equals(phone)) {
                    boolean updated = dbHelper.updatePassword(email, newPassword);
                    if (updated) {
                        Toast.makeText(ForgotPasswordActivity.this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Phone number does not match registered phone number", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (cursor != null) cursor.close();
                Toast.makeText(ForgotPasswordActivity.this, "Email not found in database", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
