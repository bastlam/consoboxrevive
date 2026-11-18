package com.consobox.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.consobox.R;
import com.consobox.Splash;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Copy of the SQLite database to / from a stream.
 * The backup format is the raw SQLite file, identical to the original "consobox.bak",
 * so backups made by the old 1.20 version can be restored.
 */
public final class BackupManager {
    private static final byte[] SQLITE_HEADER = "SQLite format 3\u0000".getBytes(StandardCharsets.US_ASCII);
    private static final Object LOCK = new Object();

    private BackupManager() {
    }

    public static File getDatabaseFile(Context context) {
        return context.getDatabasePath(context.getString(R.string.DB_NAME));
    }

    /** Default local backup file: Android/data/com.consobox/files/consobox.bak (no permission needed). */
    public static File getLocalBackupFile(Context context) {
        File dir = context.getExternalFilesDir(null);
        if (dir == null) {
            dir = context.getFilesDir();
        }
        return new File(dir, "consobox.bak");
    }

    /** Returns a consistent snapshot of the database as bytes. */
    public static byte[] snapshot(Context context) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        export(context, bos);
        return bos.toByteArray();
    }

    public static void export(Context context, OutputStream out) throws IOException {
        synchronized (LOCK) {
            SQLiteDatabase sql = Utils.getDatabase(context);
            try {
                // Flush the write-ahead log (enabled by default since Android 9) into the main file.
                Cursor c = sql.rawQuery("PRAGMA wal_checkpoint(TRUNCATE)", null);
                c.moveToFirst();
                c.close();
                sql.execSQL("vacuum");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Utils.closeDatabase(context, sql);
            }
            try (InputStream in = new FileInputStream(getDatabaseFile(context))) {
                copy(in, out);
            }
            out.flush();
        }
    }

    /** Replaces the database with the given backup. A safety copy of the current data is kept. */
    public static void restore(Context context, InputStream in) throws IOException {
        File tmp = new File(context.getCacheDir(), "restore.tmp");
        try (OutputStream out = new FileOutputStream(tmp)) {
            copy(in, out);
        }
        try {
            if (!isValidBackup(tmp)) {
                throw new InvalidBackupException();
            }
            synchronized (LOCK) {
                File db = getDatabaseFile(context);
                // keep a copy of the current data in case the restore was a mistake
                if (db.exists()) {
                    try (OutputStream safety = new FileOutputStream(new File(context.getFilesDir(), "before-restore.bak"))) {
                        export(context, safety);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Utils.closeSharedDatabase();
                new File(db.getPath() + "-wal").delete();
                new File(db.getPath() + "-shm").delete();
                new File(db.getPath() + "-journal").delete();
                File parent = db.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }
                try (InputStream src = new FileInputStream(tmp); OutputStream dst = new FileOutputStream(db)) {
                    copy(src, dst);
                }
            }
            // upgrade the schema if the backup comes from an older version
            Splash.versionUpdater(context);
        } finally {
            tmp.delete();
        }
    }

    public static boolean isValidBackup(File f) {
        byte[] header = new byte[SQLITE_HEADER.length];
        try (InputStream in = new FileInputStream(f)) {
            if (in.read(header) != header.length || !Arrays.equals(header, SQLITE_HEADER)) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
        try (SQLiteDatabase db = SQLiteDatabase.openDatabase(f.getPath(), null, SQLiteDatabase.OPEN_READONLY)) {
            Cursor c = db.rawQuery("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='voiture'", null);
            c.moveToFirst();
            boolean ok = c.getInt(0) > 0;
            c.close();
            return ok;
        } catch (Exception e) {
            return false;
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[16384];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
    }

    public static class InvalidBackupException extends IOException {
        public InvalidBackupException() {
            super("invalid backup");
        }
    }
}
