package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    private EditText usernameField, passwordField;
    private Button signupButton;
    private TextView textLogin;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        databaseHelper = new DatabaseHelper(this);
        usernameField  = findViewById(R.id.UsernameText);
        passwordField  = findViewById(R.id.passwordText);
        signupButton   = findViewById(R.id.signupButton);
        textLogin      = findViewById(R.id.text_Login);

        textLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        });

        signupButton.setOnClickListener(v -> {
            String user = usernameField.getText().toString().trim();
            String pass = passwordField.getText().toString().trim();
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter Username and Password", Toast.LENGTH_SHORT).show();
                return;
            }
            if (databaseHelper.checkUsername(user)) {
                Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!databaseHelper.insertData(user, pass)) {
                Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show();
                return;
            }
            // Save session
            SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
            prefs.edit().putString("username", user).apply();
            // Kick off Home
            Intent i = new Intent(RegisterActivity.this, HomeActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
    }
}
