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

    public static String fichesPath() {
        return new File(getAppDir(com.consobox.ConsoBoxApp.get()), "fiches.sql").getPath();
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
            if (c.getString(c.getColumnIndex("partiel")).equals("false")) {
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

    public static Double getRejet(String type) {
        if (type.equals("ESSENCE")) {
            return Constants.ESSENCE_CO2;
        }
        if (type.equals("DIESEL")) {
            return Constants.DIESEL_CO2;
        }
        if (type.equals("GPL")) {
            return Constants.GPL_CO2;
        }
        if (type.equals("ETHANOL")) {
            return Constants.E95_CO2;
        }
        return Double.valueOf(0.0d);
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
            os.write((String.valueOf(c.getString(c.getColumnIndex("date"))) + ";" + c.getString(c.getColumnIndex("volume")) + ";" + c.getDouble(c.getColumnIndex("km")) + ";" + c.getDouble(c.getColumnIndex("prix")) + ";" + (c.getString(c.getColumnIndex("partiel")).equals("true") ? "oui" : "non") + "\n").getBytes());
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

    public static String classEcologique(Double co2) {
        String ret = co2.doubleValue() <= 100.0d ? "A" : "";
        if (co2.doubleValue() > 100.0d) {
            ret = "B";
        }
        if (co2.doubleValue() > 120.0d) {
            ret = "C";
        }
        if (co2.doubleValue() > 140.0d) {
            ret = "D";
        }
        if (co2.doubleValue() > 160.0d) {
            ret = "E";
        }
        if (co2.doubleValue() > 200.0d) {
            ret = "F";
        }
        return co2.doubleValue() > 250.0d ? "G" : ret;
    }

    public static int classEcologiqueInt(Double co2) {
        int ret = co2.doubleValue() <= 100.0d ? 0 : 0;
        if (co2.doubleValue() > 100.0d) {
            ret = 1;
        }
        if (co2.doubleValue() > 120.0d) {
            ret = 2;
        }
        if (co2.doubleValue() > 140.0d) {
            ret = 3;
        }
        if (co2.doubleValue() > 160.0d) {
            ret = 4;
        }
        if (co2.doubleValue() > 200.0d) {
            ret = 5;
        }
        if (co2.doubleValue() > 250.0d) {
            return 6;
        }
        return ret;
    }

    public static int colorEcologique(Double co2) {
        int ret = co2.doubleValue() <= 100.0d ? R.color.Classe_A : 0;
        if (co2.doubleValue() > 100.0d) {
            ret = R.color.Classe_B;
        }
        if (co2.doubleValue() > 120.0d) {
            ret = R.color.Classe_C;
        }
        if (co2.doubleValue() > 140.0d) {
            ret = R.color.Classe_D;
        }
        if (co2.doubleValue() > 160.0d) {
            ret = R.color.Classe_E;
        }
        if (co2.doubleValue() > 200.0d) {
            ret = R.color.Classe_F;
        }
        return co2.doubleValue() > 250.0d ? R.color.Classe_G : ret;
    }
}
