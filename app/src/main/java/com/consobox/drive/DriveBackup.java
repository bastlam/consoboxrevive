package com.consobox.drive;

import android.accounts.Account;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.consobox.data.BackupManager;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.identity.AuthorizationRequest;
import com.google.android.gms.auth.api.identity.AuthorizationResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/** Google Drive backup: authorization, preferences and scheduling. */
public final class DriveBackup {
    public static final String SCOPE_DRIVE_FILE = "https://www.googleapis.com/auth/drive.file";

    static final String PREFS = "drive_backup";
    public static final String PREF_EMAIL = "email";
    public static final String PREF_AUTO = "auto";
    public static final String PREF_LAST = "last_backup";
    static final String PREF_FOLDER_ID = "folder_id";
    static final String PREF_AUTH_NOTIFIED = "auth_notified";

    private static final String WORK_NAME = "drive_monthly_backup";

    private DriveBackup() {
    }

    public static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, 0);
    }

    public static String getEmail(Context context) {
        return prefs(context).getString(PREF_EMAIL, null);
    }

    public static boolean isAutoEnabled(Context context) {
        return prefs(context).getBoolean(PREF_AUTO, false);
    }

    public static long getLastBackup(Context context) {
        return prefs(context).getLong(PREF_LAST, 0);
    }

    /** Authorization request for the drive.file scope (opens an account picker / consent screen if needed). */
    public static Task<AuthorizationResult> authorize(Context context) {
        AuthorizationRequest.Builder b = AuthorizationRequest.builder()
                .setRequestedScopes(Collections.singletonList(new Scope(SCOPE_DRIVE_FILE)));
        String email = getEmail(context);
        if (email != null) {
            b.setAccount(new Account(email, "com.google"));
        }
        return Identity.getAuthorizationClient(context).authorize(b.build());
    }

    public static void onConnected(Context context, String email) {
        prefs(context).edit().putString(PREF_EMAIL, email).remove(PREF_AUTH_NOTIFIED).apply();
    }

    public static void disconnect(Context context) {
        prefs(context).edit().remove(PREF_EMAIL).remove(PREF_FOLDER_ID).putBoolean(PREF_AUTO, false).apply();
        schedule(context);
    }

    public static void setAutoEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(PREF_AUTO, enabled).apply();
        schedule(context);
    }

    /**
     * A worker runs once a day (when a network is available) and uploads a backup when the last
     * successful one is at least one month old. This is more robust than a strict 30-day period:
     * a missed run (phone off, no network…) is simply retried the next day.
     */
    public static void schedule(Context context) {
        WorkManager wm = WorkManager.getInstance(context);
        if (!isAutoEnabled(context) || getEmail(context) == null) {
            wm.cancelUniqueWork(WORK_NAME);
            return;
        }
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();
        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(DriveBackupWorker.class, 1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build();
        wm.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, req);
    }

    public static boolean isBackupDue(Context context) {
        long last = getLastBackup(context);
        if (last == 0) {
            return true;
        }
        Calendar next = Calendar.getInstance();
        next.setTimeInMillis(last);
        next.add(Calendar.MONTH, 1);
        // small tolerance so that a backup made at 10:00 is redone at 08:00 one month later
        next.add(Calendar.HOUR_OF_DAY, -12);
        return System.currentTimeMillis() >= next.getTimeInMillis();
    }

    /**
     * Blocking: uploads a snapshot of the database. Must not run on the main thread.
     * Retries once with a fresh token if the cached one was rejected.
     */
    public static DriveClient.DriveFile backupNow(Context context, String token) throws IOException {
        byte[] data = BackupManager.snapshot(context);
        DriveClient.DriveFile f;
        try {
            f = new DriveClient(context, token).uploadBackup(data);
        } catch (DriveClient.DriveException e) {
            if (e.code != 401) {
                throw e;
            }
            String fresh = refreshToken(context, token);
            if (fresh == null) {
                throw e;
            }
            f = new DriveClient(context, fresh).uploadBackup(data);
        }
        prefs(context).edit().putLong(PREF_LAST, System.currentTimeMillis()).remove(PREF_AUTH_NOTIFIED).apply();
        return f;
    }

    /**
     * Blocking: obtains a token without user interaction.
     * Returns null when the user has to grant the access again (interactive resolution needed).
     */
    public static String getTokenSilently(Context context) throws IOException {
        try {
            AuthorizationResult r = Tasks.await(authorize(context), 60, TimeUnit.SECONDS);
            if (r.hasResolution()) {
                return null;
            }
            return r.getAccessToken();
        } catch (ExecutionException | java.util.concurrent.TimeoutException e) {
            throw new IOException(e.getCause() != null ? e.getCause() : e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
    }

    /** Blocking: invalidates a rejected token and requests a new one. */
    public static String refreshToken(Context context, String oldToken) throws IOException {
        try {
            GoogleAuthUtil.clearToken(context, oldToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getTokenSilently(context);
    }
}
