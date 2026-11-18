package com.consobox.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.consobox.R;
import com.consobox.data.Utils;
import com.consobox.screen.voitures.ViewVoiturePager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Periodic check of due maintenance ("révisions").
 * Replaces the original BackgroundRevision service, which relied on a never-ending
 * background service that modern Android versions kill / forbid.
 */
public class RevisionWorker extends Worker {
    private static final String WORK_NAME = "revision_check";
    public static final long DEFAULT_DELAY_MS = 86400000L;

    public RevisionWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    /** (Re)schedules or cancels the periodic check according to the user preferences. */
    public static void schedule(Context context) {
        SharedPreferences pref = context.getSharedPreferences("consobox", 0);
        WorkManager wm = WorkManager.getInstance(context);
        if (!pref.getBoolean("service", true)) {
            wm.cancelUniqueWork(WORK_NAME);
            return;
        }
        long interval = DEFAULT_DELAY_MS;
        try {
            interval = pref.getLong("service_delai", DEFAULT_DELAY_MS);
        } catch (ClassCastException ignored) {
        }
        if (interval <= 0) {
            interval = DEFAULT_DELAY_MS;
        }
        interval = Math.max(interval, PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS);
        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(RevisionWorker.class, interval, TimeUnit.MILLISECONDS).build();
        wm.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, req);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        SQLiteDatabase sql = Utils.getDatabase(ctx);
        try {
            Cursor c = sql.query("voiture", new String[]{"id", "km", "nom"}, null, null, null, null, null);
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                int voiture = c.getInt(c.getColumnIndex("id"));
                double km = c.getDouble(c.getColumnIndex("km"));
                if (hasDueRevision(sql, voiture, km)) {
                    Intent intent = new Intent(ctx, ViewVoiturePager.class);
                    intent.putExtra("voitureid", voiture);
                    intent.setAction("revision");
                    Notifications.show(ctx, Notifications.CHANNEL_REVISION, voiture,
                            ctx.getString(R.string.notif_revision_title),
                            ctx.getString(R.string.notif_revision_text, c.getString(c.getColumnIndex("nom"))),
                            intent);
                }
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Utils.closeDatabase(ctx, sql);
        }
        return Result.success();
    }

    private static boolean hasDueRevision(SQLiteDatabase sql, int voiture, double km) {
        Cursor rev = sql.query("revisions", new String[]{"valide", "dateLast", "kmLast", "freqKm", "freqDate", "type"},
                "voiture=" + voiture, null, null, null, null);
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);
            for (int f = 0; f < rev.getCount(); f++) {
                rev.moveToPosition(f);
                if (rev.getInt(rev.getColumnIndex("valide")) != 1) {
                    continue;
                }
                int kmLast = rev.getInt(rev.getColumnIndex("kmLast"));
                int freqKm = rev.getInt(rev.getColumnIndex("freqKm"));
                if (kmLast > 0 && freqKm != 0 && kmLast + freqKm <= km) {
                    return true;
                }
                int freqDate = rev.getInt(rev.getColumnIndex("freqDate"));
                try {
                    Date date = sdf.parse(rev.getString(rev.getColumnIndex("dateLast")));
                    // months elapsed (30.5 days), same formula as the original application
                    long months = (long) ((new Date().getTime() - date.getTime()) / 2.6352E9d);
                    if (freqDate != 0 && months > 0 && months >= freqDate) {
                        return true;
                    }
                } catch (ParseException | NullPointerException ignored) {
                }
            }
            return false;
        } finally {
            rev.close();
        }
    }
}
