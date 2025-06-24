package com.example.myapplication;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MedicationTrackerActivity extends AppCompatActivity {
    private ListView listMeds;
    private DatabaseHelper dbHelper;
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication);

        listMeds   = findViewById(R.id.listMeds);
        dbHelper   = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        currentUser = prefs.getString("username", null);
        if (currentUser == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Cursor c = dbHelper.getProfile(currentUser);
        String allergyCsv = "";
        if (c.moveToFirst()) {
            allergyCsv = c.getString(1);  // ALLERGY column
        }
        c.close();

        if (allergyCsv == null || allergyCsv.isEmpty()) {
            Toast.makeText(this, "No allergies set in profile", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String[] allergens = allergyCsv.split("\\s*,\\s*");

        List<String> displayLines = new ArrayList<>();
        for (String allergen : allergens) {
            displayLines.add(allergen + ":");
            Cursor m = dbHelper.getMedicationsForAllergy(allergen.trim());
            if (m.moveToFirst()) {
                do {
                    String med    = m.getString(0);
                    String dosage = m.getString(1);
                    displayLines.add("  \u2022 " + med + " — " + dosage);
                } while (m.moveToNext());
            } else {
                displayLines.add("  (no medications found)");
            }
            m.close();
            displayLines.add("");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                displayLines
        );
        listMeds.setAdapter(adapter);
    }
}
