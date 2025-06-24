package com.example.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DailyRemindersActivity extends AppCompatActivity {
    private ListView listReminders;
    private Button   btnSetup;
    private DatabaseHelper db;
    private String currentUser;


    private Map<String, ReminderItem> reminderMap = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        listReminders = findViewById(R.id.listReminders);
        btnSetup      = findViewById(R.id.btnSetupReminders);
        db            = new DatabaseHelper(this);

        currentUser = getSharedPreferences("session", MODE_PRIVATE)
                .getString("username", null);
        if (currentUser == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        buildReminders();


        List<String> display = new ArrayList<>();
        for (ReminderItem ri : reminderMap.values()) {
            display.add(ri.medName + " — " + ri.dosage + " @ " + ri.timesAsString());
        }
        listReminders.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, display
        ));

        btnSetup.setOnClickListener(v -> {
            scheduleAlarms();
            Toast.makeText(this, "Reminders scheduled", Toast.LENGTH_SHORT).show();
        });
    }

    /** Load all meds for each allergy, dedupe by medName */
    private void buildReminders() {
        // 1) get allergy CSV
        String csv = "";
        try (Cursor c = db.getProfile(currentUser)) {
            if (c.moveToFirst()) csv = c.getString(1);
        }
        if (csv == null || csv.trim().isEmpty()) {
            Toast.makeText(this,
                    "No allergies set; cannot schedule meds",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // 2) for each allergen, query meds
        for (String allergen : csv.split("\\s*,\\s*")) {
            try (Cursor meds = db.getMedicationsForAllergy(allergen)) {
                while (meds.moveToNext()) {
                    String med    = meds.getString(0);
                    String dosage = meds.getString(1);

                    // skip if already added
                    if (reminderMap.containsKey(med)) continue;

                    // decide times
                    List<Calendar> times = computeTimes(med, dosage);
                    if (!times.isEmpty()) {
                        reminderMap.put(med, new ReminderItem(med, dosage, times));
                    }
                }
            }
        }
    }


    private List<Calendar> computeTimes(String med, String dosage) {
        List<Calendar> list = new ArrayList<>();
        String m = med.toLowerCase();

        if (m.contains("epinephrine")) {
            return list;
        }

        if (m.contains("diphenhydramine")) {
            // every 6h
            list.add(at(6, 0));
            list.add(at(12, 0));
            list.add(at(18, 0));
            list.add(at(0, 0));
        } else {
            list.add(at(8, 0));
        }

        return list;
    }

    /** Helper: returns a Calendar instance for today@h:m (or tomorrow if past) */
    private Calendar at(int hour, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        if (c.getTimeInMillis() < System.currentTimeMillis()) {
            c.add(Calendar.DATE, 1);
        }
        return c;
    }

    /** Schedule all of these alarms in AlarmManager */
    private void scheduleAlarms() {
        AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        int reqCode = 0;
        for (ReminderItem ri : reminderMap.values()) {
            for (Calendar cal : ri.times) {
                Intent i = new Intent(this, ReminderReceiver.class);
                i.putExtra("medText", ri.medName + " — " + ri.dosage);
                PendingIntent pi = PendingIntent.getBroadcast(
                        this, reqCode++, i,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
                am.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        cal.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY,
                        pi
                );
            }
        }
    }

    private static class ReminderItem {
        final String medName, dosage;
        final List<Calendar> times;
        ReminderItem(String m, String d, List<Calendar> t) {
            medName = m; dosage = d; times = t;
        }
        String timesAsString() {
            StringBuilder sb = new StringBuilder();
            for (Calendar c : times) {
                if (sb.length()>0) sb.append(", ");
                sb.append(String.format("%02d:%02d",
                        c.get(Calendar.HOUR_OF_DAY),
                        c.get(Calendar.MINUTE)));
            }
            return sb.toString();
        }
    }
}
