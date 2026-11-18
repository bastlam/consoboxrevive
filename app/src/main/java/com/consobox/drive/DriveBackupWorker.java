package com.consobox.drive;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.consobox.R;
import com.consobox.screen.settings.Settings;
import com.consobox.service.Notifications;

/** Daily check; uploads a backup to Google Drive when the last one is a month old. */
public class DriveBackupWorker extends Worker {
    private static final String TAG = "DriveBackupWorker";

    public DriveBackupWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        if (!DriveBackup.isAutoEnabled(ctx) || DriveBackup.getEmail(ctx) == null) {
            return Result.success();
        }
        if (!DriveBackup.isBackupDue(ctx)) {
            return Result.success();
        }
        try {
            String token = DriveBackup.getTokenSilently(ctx);
            if (token == null) {
                notifyAuthNeeded(ctx);
                return Result.success();
            }
            DriveBackup.backupNow(ctx, token);
            Log.i(TAG, "monthly backup uploaded");
            return Result.success();
        } catch (Exception e) {
            Log.w(TAG, "monthly backup failed", e);
            // The backup is retried on the next daily run. The user is told (once) only when
            // the last successful backup becomes clearly overdue (40 days).
            long last = DriveBackup.getLastBackup(ctx);
            boolean overdue = last == 0 || System.currentTimeMillis() - last > 40L * 24 * 3600 * 1000;
            if (overdue && !DriveBackup.prefs(ctx).getBoolean(DriveBackup.PREF_AUTH_NOTIFIED, false)) {
                DriveBackup.prefs(ctx).edit().putBoolean(DriveBackup.PREF_AUTH_NOTIFIED, true).apply();
                Notifications.show(ctx, Notifications.CHANNEL_BACKUP, Notifications.ID_BACKUP,
                        ctx.getString(R.string.drive_auth_needed_title),
                        ctx.getString(R.string.drive_auto_failed_text, String.valueOf(e.getMessage())),
                        new Intent(ctx, Settings.class));
            }
            return Result.success();
        }
    }

    private static void notifyAuthNeeded(Context ctx) {
        if (DriveBackup.prefs(ctx).getBoolean(DriveBackup.PREF_AUTH_NOTIFIED, false)) {
            return;
        }
        DriveBackup.prefs(ctx).edit().putBoolean(DriveBackup.PREF_AUTH_NOTIFIED, true).apply();
        Notifications.show(ctx, Notifications.CHANNEL_BACKUP, Notifications.ID_BACKUP,
                ctx.getString(R.string.drive_auth_needed_title),
                ctx.getString(R.string.drive_auth_needed_text),
                new Intent(ctx, Settings.class));
    }
}
