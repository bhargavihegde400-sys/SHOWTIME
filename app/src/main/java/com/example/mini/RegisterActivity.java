package com.example.mini;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etPassword;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        Button btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);
        ImageView btnBackRegister = findViewById(R.id.btnBackRegister);

        btnBackRegister.setOnClickListener(v -> finish());

        btnRegisterSubmit.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate email format
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Enter a valid email address");
                etEmail.requestFocus();
                return;
            }

            // Validate phone number: exactly 10 digits
            if (!phone.matches("[0-9]{10}")) {
                etPhone.setError("Phone number must contain exactly 10 digits");
                etPhone.requestFocus();
                return;
            }

            // Validate password strength
            if (password.length() < 8
                    || !password.matches(".*[A-Z].*")
                    || !password.matches(".*[a-z].*")
                    || !password.matches(".*[0-9].*")
                    || !password.matches(".*[^A-Za-z0-9].*")) {

                Toast.makeText(RegisterActivity.this,
                        "Password must have 8+ characters, uppercase, lowercase, number and special character",
                        Toast.LENGTH_LONG).show();
                return;
            }


            boolean success = dbHelper.registerUser(name, email, phone, password);
            if (success) {
                Toast.makeText(RegisterActivity.this, "Registration Successful! Please login.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, "Registration Failed. Email may already exist.", Toast.LENGTH_SHORT).show();
            }
        });

        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }
}
