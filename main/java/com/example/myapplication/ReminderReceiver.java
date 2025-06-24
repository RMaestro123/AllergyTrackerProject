package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID   = "meds_channel";
    private static final String CHANNEL_NAME = "Medication Reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && nm != null) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Reminders to take your medications");
            nm.createNotificationChannel(channel);
        }

        String medText = intent.getStringExtra("medText");
        if (medText == null) {
            medText = "Time to take your medication";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // or your R.drawable.ic_pill
                .setContentTitle("Medication Reminder")
                .setContentText(medText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(medText))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        int notifId = medText.hashCode();
        if (nm != null) {
            nm.notify(notifId, builder.build());
        }
    }
}
