package com.consobox.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.TypedValue;
import android.widget.Toast;
import com.consobox.R;
import com.consobox.Splash;
import com.consobox.models.Voiture;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/* JADX INFO: loaded from: classes.dex */
public class Utils {

    /** App-specific storage directory (replaces the hard-coded /sdcard/consobox). No permission needed. */
    public static File getAppDir(Context context) {
        File dir = context.getExternalFilesDir(null);
        if (dir == null) {
            dir = context.getFilesDir();
        }
        dir.mkdirs();
        return dir;
    }

    public static File getCsvFile(Context context, int voiture) {
        return new File(getAppDir(context), "voiture" + voiture + ".csv");
    }

    public static int dipToPixel(int dip, Context context) {
        return (int) TypedValue.applyDimension(1, dip, context.getResources().getDisplayMetrics());
    }

    public static Bitmap ReduceBitmap(Bitmap original) {
        try {
            int width = original.getWidth();
            int height = original.getHeight();
            float newHeight = height / (width / 200.0f);
            Matrix matrice = new Matrix();
            matrice.postScale(200.0f / width, newHeight / height);
            Bitmap newBmp = Bitmap.createBitmap(original, 0, 0, width, height, matrice, true);
            return newBmp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Voiture getVoiture(Context aContext, int aId) {
        SQLiteDatabase sql = getDatabase(aContext);
        Cursor c = sql.query("voiture", new String[]{"*"}, "id=" + aId, null, null, null, null);
        if (c.getCount() == 0) {
            return null;
        }
        c.moveToFirst();
        Voiture ret = new Voiture(c);
        c.close();
        closeDatabase(aContext, sql);
        return ret;
    }

    public static Double getConsoMoyenne(Context aContext, int aId) {
        SQLiteDatabase sql = getDatabase(aContext);
        Cursor c = sql.query("conso", new String[]{"km", "volume", "partiel"}, "voiture=" + aId, null, null, null, null, null);
        c.moveToFirst();
        Double res = Double.valueOf(0.0d);
        int nbPleinUtil = 0;
        for (int i = 0; i < c.getCount(); i++) {
            if (!isPartiel(c.getString(c.getColumnIndex("partiel"))) && c.getDouble(c.getColumnIndex("km")) > 0) {
                nbPleinUtil++;
                res = Double.valueOf(res.doubleValue() + ((c.getDouble(c.getColumnIndex("volume")) / c.getDouble(c.getColumnIndex("km"))) * 100.0d));
            }
            c.moveToNext();
        }
        c.close();
        Double res2 = Double.valueOf(nbPleinUtil > 0 ? res.doubleValue() / ((double) nbPleinUtil) : 0.0d);
        closeDatabase(aContext, sql);
        return res2;
    }

    public static String getRealPathFromURI(Uri aContentUri, Context aContext) {
        if (aContentUri != null && "file".equals(aContentUri.getScheme())) {
            return aContentUri.getPath();
        }
        String[] proj = {"_data"};
        try {
            Cursor cursor = aContext.getContentResolver().query(aContentUri, proj, null, null, null);
            if (cursor == null) {
                return "";
            }
            int column_index = cursor.getColumnIndexOrThrow("_data");
            cursor.moveToFirst();
            String string = cursor.getString(column_index);
            cursor.close();
            return string;
        } catch (Exception e) {
            return "";
        }
    }

    public static int getPoidsFromIndice(int indice, Context context) {
        int[] poids = context.getResources().getIntArray(R.array.indicepoids);
        if (indice - 68 < 0 || indice - 68 > poids.length - 1) {
            return 0;
        }
        return poids[indice - 68];
    }

    public static int getVitesseFromIndice(String indice) {
        if ("K".equals(indice)) {
            return 110;
        }
        if ("L".equals(indice)) {
            return 120;
        }
        if ("M".equals(indice)) {
            return 130;
        }
        if ("N".equals(indice)) {
            return 140;
        }
        if ("P".equals(indice)) {
            return 150;
        }
        if ("Q".equals(indice)) {
            return 160;
        }
        if ("R".equals(indice)) {
            return 170;
        }
        if ("S".equals(indice)) {
            return 180;
        }
        if ("T".equals(indice)) {
            return 190;
        }
        if ("U".equals(indice)) {
            return 200;
        }
        if ("H".equals(indice)) {
            return 210;
        }
        if ("V".equals(indice)) {
            return 240;
        }
        if ("ZR".equals(indice)) {
            return 260;
        }
        if ("W".equals(indice)) {
            return 270;
        }
        if ("Y".equals(indice)) {
            return 300;
        }
        return 0;
    }

    public static double getKmSinceDate(Context context, String date, int voitureId) {
        if (date.contains("/")) {
            String[] tmp = date.split("/");
            date = String.valueOf(tmp[2]) + "-" + tmp[1] + "-" + tmp[0];
        }
        SQLiteDatabase sql = getDatabase(context);
        Cursor c = sql.query("conso", new String[]{"SUM(km) as kms"}, "voiture=" + voitureId + " AND date>'" + date + "'", null, null, null, null);
        c.moveToFirst();
        double km = c.getDouble(c.getColumnIndex("kms"));
        c.close();
        closeDatabase(context, sql);
        return km;
    }

    public static boolean exportConsoCSV(Context context, int voiture, boolean showToast) throws IOException {
        SQLiteDatabase sql = getDatabase(context);
        Cursor c = sql.query("conso", new String[]{"date", "volume", "km", "prix", "partiel"}, "voiture=" + voiture, null, null, null, null);
        if (c.getCount() == 0) {
            c.close();
            closeDatabase(context, sql);
            Toast.makeText(context, context.getResources().getString(R.string.pasdeplein), 1).show();
            return false;
        }
        File outputFile = getCsvFile(context, voiture);
        BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile));
        c.moveToFirst();
        os.write("date;volume;km;prix;pleinpartiel\n".getBytes());
        for (int i = 0; i < c.getCount(); i++) {
            os.write((String.valueOf(c.getString(c.getColumnIndex("date"))) + ";" + c.getString(c.getColumnIndex("volume")) + ";" + c.getDouble(c.getColumnIndex("km")) + ";" + c.getDouble(c.getColumnIndex("prix")) + ";" + (isPartiel(c.getString(c.getColumnIndex("partiel"))) ? "oui" : "non") + "\n").getBytes());
            c.moveToNext();
        }
        os.close();
        c.close();
        closeDatabase(context, sql);
        if (showToast) {
            Toast.makeText(context, context.getResources().getString(R.string.fichiercsv) + outputFile.getPath(), 1).show();
        }
        return true;
    }

    public static void saveDatabase(Context context) throws IOException {
        try (FileOutputStream out = new FileOutputStream(BackupManager.getLocalBackupFile(context))) {
            BackupManager.export(context, out);
        }
    }

    public static void restoreDatabase(Context context) throws IOException {
        try (FileInputStream in = new FileInputStream(BackupManager.getLocalBackupFile(context))) {
            BackupManager.restore(context, in);
        }
    }

    private static SQLiteDatabase sDatabase;

    /**
     * Shared, process-wide connection. The original code opened a new connection for every query,
     * which causes "database is locked" errors with the WAL mode used by recent Android versions.
     */
    public static synchronized SQLiteDatabase getDatabase(Context context) {
        Constants.context = context;
        if (sDatabase == null || !sDatabase.isOpen()) {
            sDatabase = context.getApplicationContext().openOrCreateDatabase(
                    context.getResources().getString(R.string.DB_NAME), 0, null);
        }
        return sDatabase;
    }

    /** No-op: the shared connection stays open for the lifetime of the process. */
    public static void closeDatabase(Context context, SQLiteDatabase ddb) {
    }

    /** Really closes the shared connection (before replacing the database file). */
    public static synchronized void closeSharedDatabase() {
        if (sDatabase != null) {
            try {
                sDatabase.close();
            } catch (Exception ignored) {
            }
            sDatabase = null;
        }
    }

    public static String getDeviseUnit(Context context) {
        return context.getResources().getStringArray(R.array.deviseunite)[context.getSharedPreferences("consobox", 0).getInt("devise", 0)];
    }

    public static SharedPreferences getWidgetPref(Context context) {
        return context.getSharedPreferences("widget", 0);
    }

    /** A fill-up is "partial" only when explicitly flagged (older data may contain 0/1 instead of false/true). */
    public static boolean isPartiel(String value) {
        return value != null && (value.equalsIgnoreCase("true") || value.equals("1"));
    }

    /** SQL condition selecting the complete (non partial) fill-ups. */
    public static final String SQL_PLEIN_COMPLET = "(partiel IS NULL OR (partiel <> 'true' AND partiel <> '1'))";

    /**
     * Keeps the fuel expenses (depenses.type = 0) in line with the fill-ups (table conso):
     * one expense per fill-up, linked through depenses.description = conso.id,
     * amount = price per litre x litres. Repairs data where the expense was never created
     * (fill-up entered without price, data migrated from old versions) and removes expenses
     * whose fill-up was deleted.
     */
    public static void syncFuelExpenses(SQLiteDatabase sql, int voiture) {
        Cursor c = sql.query("conso", new String[]{"id", "date", "km", "volume", "prix"}, "voiture=" + voiture, null, null, null, null);
        try {
            while (c.moveToNext()) {
                String id = String.valueOf(c.getLong(0));
                String date = c.getString(1);
                double total = c.getDouble(4) * c.getDouble(3);
                android.content.ContentValues values = new android.content.ContentValues();
                values.put("prix", total);
                values.put("date", date);
                values.put("description", id);
                int updated = sql.update("depenses", values, "type=0 AND voiture=? AND description=?",
                        new String[]{String.valueOf(voiture), id});
                if (updated > 0) {
                    continue;
                }
                // expense created by an old version, not linked to its fill-up: link it (same car + date)
                Cursor u = sql.query("depenses", new String[]{"id"},
                        "type=0 AND voiture=? AND date=? AND (description IS NULL OR description='')",
                        new String[]{String.valueOf(voiture), date}, null, null, null, "1");
                try {
                    if (u.moveToFirst()) {
                        sql.update("depenses", values, "id=" + u.getLong(0), null);
                        continue;
                    }
                } finally {
                    u.close();
                }
                if (total > 0) {
                    values.put("voiture", voiture);
                    values.put("type", 0);
                    values.put("km", c.getDouble(2));
                    sql.insert("depenses", null, values);
                }
            }
        } finally {
            c.close();
        }
        sql.execSQL("DELETE FROM depenses WHERE type=0 AND voiture=" + voiture
                + " AND description GLOB '[0-9]*' AND CAST(description AS INTEGER) NOT IN (SELECT id FROM conso WHERE voiture=" + voiture + ")");
    }

    public static void syncFuelExpenses(Context context, int voiture) {
        try {
            syncFuelExpenses(getDatabase(context), voiture);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
