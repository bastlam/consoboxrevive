package com.consobox.service;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.consobox.R;

/** Notification channels and helpers (channels are mandatory since Android 8). */
public final class Notifications {
    public static final String CHANNEL_REVISION = "revision";
    public static final String CHANNEL_BACKUP = "backup";
    public static final int ID_BACKUP = 100000;

    private Notifications() {
    }

    public static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel(CHANNEL_REVISION,
                context.getString(R.string.notif_channel_revision), NotificationManager.IMPORTANCE_DEFAULT));
        nm.createNotificationChannel(new NotificationChannel(CHANNEL_BACKUP,
                context.getString(R.string.notif_channel_backup), NotificationManager.IMPORTANCE_LOW));
    }

    public static boolean canNotify(Context context) {
        return Build.VERSION.SDK_INT < 33
                || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    public static void show(Context context, String channel, int id, String title, String text, Intent intent) {
        if (!canNotify(context)) {
            return;
        }
        createChannels(context);
        PendingIntent pi = PendingIntent.getActivity(context, id, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder b = new NotificationCompat.Builder(context, channel)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(pi)
                .setAutoCancel(true);
        try {
            NotificationManagerCompat.from(context).notify(id, b.build());
        } catch (SecurityException ignored) {
            // permission revoked in the meantime
        }
    }
}
