package com.consobox.drive;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

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
                // the user has to open Settings and authorise Google Drive again
                Log.w(TAG, "Google authorisation needed");
                return Result.success();
            }
            DriveBackup.backupNow(ctx, token);
            Log.i(TAG, "monthly backup uploaded");
            return Result.success();
        } catch (Exception e) {
            // retried on the next daily run; the last backup date is visible in Settings
            Log.w(TAG, "monthly backup failed", e);
            return Result.success();
        }
    }

}
