package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
//import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CalendarActivity extends AppCompatActivity {

    CalendarView calendarView;
    EditText noteInput;
    Button saveNoteButton, deleteNoteButton;

    String selectedDate = "";
    SharedPreferences sharedPreferences;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar); // Make sure this XML name matches!

        calendarView = findViewById(R.id.calendarView);
        noteInput = findViewById(R.id.noteInput);
        saveNoteButton = findViewById(R.id.saveNoteButton);
        deleteNoteButton = findViewById(R.id.deleteNoteButton);

        sharedPreferences = getSharedPreferences("CalendarNotes", MODE_PRIVATE);

        // Set today's date as default
        selectedDate = getDateString(calendarView.getDate());
        loadNoteForDate(selectedDate);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            month += 1; // month is 0-based
            selectedDate = String.format("%04d-%02d-%02d", year, month, dayOfMonth);
            loadNoteForDate(selectedDate);
        });

        saveNoteButton.setOnClickListener(v -> {
            String note = noteInput.getText().toString().trim();
            if (!note.isEmpty()) {
                sharedPreferences.edit().putString(selectedDate, note).apply();
                Toast.makeText(CalendarActivity.this, "Note saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CalendarActivity.this, "Note is empty", Toast.LENGTH_SHORT).show();
            }
        });

        deleteNoteButton.setOnClickListener(v -> {
            if (sharedPreferences.contains(selectedDate)) {
                sharedPreferences.edit().remove(selectedDate).apply();
                noteInput.setText("");
                Toast.makeText(CalendarActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CalendarActivity.this, "No note to delete", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadNoteForDate(String dateKey) {
        String note = sharedPreferences.getString(dateKey, "");
        noteInput.setText(note);
    }

    @SuppressLint("DefaultLocale")
    private String getDateString(long millis) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int year = calendar.get(java.util.Calendar.YEAR);
        int month = calendar.get(java.util.Calendar.MONTH) + 1;
        int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);
        return String.format("%04d-%02d-%02d", year, month, day);
    }
}
