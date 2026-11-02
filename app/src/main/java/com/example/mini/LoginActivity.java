package com.example.mini;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLoginSubmit = findViewById(R.id.btnLoginSubmit);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);
        ImageView btnBackLogin = findViewById(R.id.btnBackLogin);

        btnBackLogin.setOnClickListener(v -> finish());

        View layoutRegisterPrompt = findViewById(R.id.layoutRegisterPrompt);

        // If launched from admin portal button, prefill admin credentials
        if (getIntent().getBooleanExtra("isAdminPortal", false)) {
            etEmail.setText("admin@showtime.com");
            etPassword.setText("admin123");
            etPassword.setHint(R.string.admin_passkey);
            TextView tvLoginTitle = findViewById(R.id.tvLoginTitle);
            if (tvLoginTitle != null) {
                tvLoginTitle.setText(R.string.admin_login);
            }
            if (layoutRegisterPrompt != null) {
                layoutRegisterPrompt.setVisibility(View.GONE);
            }
            if (tvForgotPassword != null) {
                tvForgotPassword.setVisibility(View.GONE);
            }
        }

        btnLoginSubmit.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate email format
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Enter a valid email address");
                etEmail.requestFocus();
                return;
            }

            Cursor cursor = dbHelper.checkUser(email, password);
            if (cursor != null && cursor.moveToFirst()) {
                int isAdminIndex = cursor.getColumnIndex("is_admin");
                int isAdmin = isAdminIndex != -1 ? cursor.getInt(isAdminIndex) : 0;
                cursor.close();

                Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                Intent intent;
                if (isAdmin == 1) {
                    intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, UserHomeActivity.class);
                }
                intent.putExtra("user_email", email);
                startActivity(intent);
                finish();
            } else {
                if (cursor != null) cursor.close();
                Toast.makeText(LoginActivity.this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
            }
        });

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
        }

        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }
}
