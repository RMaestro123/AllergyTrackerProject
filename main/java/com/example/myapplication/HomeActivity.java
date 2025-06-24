package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    private TextView    textUserName;
    private Button      btnProfile,
            calendarButton,
            btnSymptoms,
            btnMedication,
            btnReminders,
            btnLogout;
    private DatabaseHelper db;
    private String currentUser;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        SharedPreferences session = getSharedPreferences("session", MODE_PRIVATE);
        currentUser = session.getString("username", null);
        if (currentUser == null) {
            // not logged in → go to MainActivity
            startActivity(new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }
        db = new DatabaseHelper(this);


        textUserName     = findViewById(R.id.textUserName);
        btnProfile       = findViewById(R.id.btnProfile);
        calendarButton   = findViewById(R.id.calendarButton);
        btnSymptoms      = findViewById(R.id.btnSymptoms);
        btnMedication    = findViewById(R.id.btnMedication);
        btnReminders     = findViewById(R.id.btnReminders);
        btnLogout        = findViewById(R.id.logOutButton);


        textUserName.setText("Welcome, " + currentUser + "!");



        // Profile screen
        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );

        calendarButton.setOnClickListener(v ->
                startActivity(new Intent(this, CalendarActivity.class))
        );

        btnSymptoms.setOnClickListener(v ->
                startActivity(new Intent(this, SymptomsActivity.class))
        );

        btnMedication.setOnClickListener(v ->
                startActivity(new Intent(this, MedicationTrackerActivity.class))
        );

        btnReminders.setOnClickListener(v ->
                startActivity(new Intent(this, DailyRemindersActivity.class))
        );
        // Logout
        btnLogout.setOnClickListener(v -> {
            session.edit().remove("username").apply();
            startActivity(new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });


    }

    @Override
    public void onBackPressed() {
        // Pressing back also logs out
        super.onBackPressed();
        btnLogout.performClick();
    }
}
