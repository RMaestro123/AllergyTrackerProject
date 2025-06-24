package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private EditText username, password;
    private TextView signupText;
    private Button loginButton;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        databaseHelper = new DatabaseHelper(this);

        username    = findViewById(R.id.UsernameText);
        password    = findViewById(R.id.passwordText);
        loginButton = findViewById(R.id.loginButton);
        signupText  = findViewById(R.id.textSignup);

        // go to register
        signupText.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RegisterActivity.class))
        );

        loginButton.setOnClickListener(v -> {
            String user = username.getText().toString().trim();
            String pass = password.getText().toString().trim();
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter Username and Password", Toast.LENGTH_SHORT).show();
                return;
            }
            String loggedIn = databaseHelper.checkLogin(user, pass);
            if (loggedIn != null) {
                // Save session
                getSharedPreferences("session", MODE_PRIVATE)
                        .edit()
                        .putString("username", loggedIn)
                        .apply();
                Intent i = new Intent(MainActivity.this, HomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            } else {
                Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
