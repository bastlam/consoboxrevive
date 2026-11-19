package com.consobox;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ViewFlipper;
import com.consobox.data.Network;
import com.consobox.data.Utils;
import com.consobox.screen.settings.Settings;
import com.consobox.screen.voitures.ListeVoiture;
import com.consobox.screen.voitures.ViewVoiturePager;
import com.consobox.drive.DriveBackup;
import java.util.HashMap;

/* JADX INFO: loaded from: classes.dex */
public class Splash extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private ProgressDialog mDialog;
    private int mIdMessage;
    private boolean mIsMenu = false;
    private boolean isUpdated = false;
    private Handler showMessage = new Handler() { // from class: com.consobox.Splash.1
        /* JADX WARN: Type inference failed for: r3v3, types: [com.consobox.Splash$1$1] */
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            String message = msg.getData().getString("message");
            String url = null;
            if (message.indexOf("[url") != -1) {
                url = message.substring(message.indexOf("[url") + 5, message.indexOf("]", message.indexOf("[url") + 5));
                message = message.substring(message.indexOf("]") + 2, message.length());
            }
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() { // from class: com.consobox.Splash.1.1
                String mUrl;

                public DialogInterface.OnClickListener setUrl(String url2) {
                    this.mUrl = url2;
                    return this;
                }

                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences pref = Splash.this.getPreferences(0);
                    pref.edit().putInt("messageVersion", Splash.this.mIdMessage).commit();
                    if (which == -1) {
                        dialog.cancel();
                        return;
                    }
                    if (which == -2) {
                        if (this.mUrl != null) {
                            Intent i = new Intent("android.intent.action.VIEW");
                            i.setData(Uri.parse(this.mUrl));
                            Splash.this.startActivity(i);
                            return;
                        }
                        dialog.cancel();
                    }
                }
            }.setUrl(url);
            try {
                com.consobox.data.Message.CustomMessage(message, "OK", url == null ? "Annuler" : "Visiter l'URL", Splash.this, listener);
            } catch (Exception e) {
            }
        }
    };
    private Handler goToMenu = new Handler() { // from class: com.consobox.Splash.2
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            ViewFlipper flipper = (ViewFlipper) Splash.this.findViewById(R.id.phaseflipper);
            flipper.setInAnimation(AnimationUtils.loadAnimation(Splash.this, R.anim.fade_in));
            flipper.setOutAnimation(AnimationUtils.loadAnimation(Splash.this, R.anim.fade_out));
            flipper.showNext();
        }
    };

    /* JADX WARN: Type inference failed for: r10v1, types: [com.consobox.Splash$3] */
    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_mainflipper);
        new Thread() { // from class: com.consobox.Splash.3
            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                Splash.this.checkOnlineMessage();
            }
        }.start();
        SQLiteDatabase sql = Utils.getDatabase(this);
        sql.execSQL(getResources().getString(R.string.DB_CONSO));
        sql.execSQL(getResources().getString(R.string.DB_VOITURE));
        sql.execSQL(getResources().getString(R.string.DB_REVISION));
        Utils.closeDatabase(this, sql);
        Display display = ((WindowManager) getSystemService("window")).getDefaultDisplay();
        View img = findViewById(R.id.phase_image);
        cleanupRemovedFeatures();
        DriveBackup.schedule(this);
        if (android.os.Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(display.getWidth(), display.getHeight());
        img.setLayoutParams(lp);
        GridView gridview = (GridView) findViewById(R.id.mainmenu_grid);
        gridview.setAdapter((ListAdapter) new MenuAdapter(this));
        gridview.setOnItemClickListener(this);
        Thread t = new Thread() { // from class: com.consobox.Splash.4
            @Override // java.lang.Thread, java.lang.Runnable
            public synchronized void run() {
                try {
                    Splash.versionUpdater(Splash.this);
                    Splash.this.isUpdated = true;
                    wait(1000L);
                    if (!Splash.this.mIsMenu) {
                        Splash.this.goToMenu.sendEmptyMessage(0);
                    }
                    Splash.this.mIsMenu = true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
        img.setOnClickListener(new View.OnClickListener() { // from class: com.consobox.Splash.5
            @Override // android.view.View.OnClickListener
            public void onClick(View arg0) {
                if (Splash.this.isUpdated) {
                    Splash.this.mIsMenu = true;
                    Splash.this.goToMenu.sendEmptyMessage(0);
                }
            }
        });
    }

    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        GridView accesrapide = (GridView) findViewById(R.id.mainmenu_accesrapide);
        accesrapide.setVisibility(0);
        accesrapide.setAdapter((ListAdapter) new AccesRapideAdapter(this));
        accesrapide.setOnItemClickListener(this);
        accesrapide.setOnItemLongClickListener(this);
    }

    @Override // android.app.Activity
    public void onDestroy() {
        super.onDestroy();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void checkOnlineMessage() {
        try {
            ConnectivityManager connec = (ConnectivityManager) getSystemService("connectivity");
            NetworkInfo[] ni = connec.getAllNetworkInfo();
            for (NetworkInfo networkInfo : ni) {
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    String rep = Network.makeRequest("http://www.olly.free.fr/consobox/message/version.txt");
                    if (rep.indexOf("[") != -1) {
                        String rep2 = rep.substring(rep.indexOf("[") + 1, rep.indexOf("]") - rep.indexOf("["));
                        SharedPreferences pref = getPreferences(0);
                        if (pref.getInt("messageVersion", 0) < Integer.parseInt(rep2)) {
                            this.mIdMessage = Integer.parseInt(rep2);
                            String rep3 = Network.makeRequest("http://www.olly.free.fr/consobox/message/" + rep2 + ".txt");
                            Message msg = new Message();
                            Bundle data = new Bundle();
                            data.putString("message", rep3);
                            msg.setData(data);
                            this.showMessage.sendMessage(msg);
                            return;
                        }
                        return;
                    }
                    return;
                }
            }
        } catch (Exception e) {
        }
    }

    /** Maintenance tracking and technical sheets were removed in 1.22: drop their leftovers. */
    private void cleanupRemovedFeatures() {
        androidx.work.WorkManager.getInstance(this).cancelUniqueWork("revision_check");
        android.app.NotificationManager nm = (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            nm.deleteNotificationChannel("revision");
        }
        java.io.File dir = getExternalFilesDir(null);
        if (dir != null) {
            new java.io.File(dir, "fiches.sql").delete();
        }
    }

    public static void versionUpdater(Context context) {
        SQLiteDatabase sql = Utils.getDatabase(context);
        sql.execSQL(context.getResources().getString(R.string.DB_CONSO));
        sql.execSQL(context.getResources().getString(R.string.DB_VOITURE));
        sql.execSQL(context.getResources().getString(R.string.DB_REVISION));
        sql.execSQL(context.getResources().getString(R.string.DB_PNEU));
        try {
            sql.query("conso", new String[]{"partiel"}, null, null, null, null, null);
        } catch (SQLiteException e) {
            e.printStackTrace();
            sql.execSQL("ALTER TABLE conso ADD partiel BOOLEAN DEFAULT 'false'");
        }
        try {
            sql.query("conso", new String[]{"prix"}, null, null, null, null, null);
        } catch (SQLiteException e2) {
            e2.printStackTrace();
            sql.execSQL("ALTER TABLE conso ADD prix DOUBLE DEFAULT 0");
        }
        try {
            sql.query("list_of_revision", new String[]{"id"}, null, null, null, null, null);
        } catch (SQLiteException e3) {
            e3.printStackTrace();
            sql.execSQL(context.getResources().getString(R.string.DB_LIST_OF_REVISION));
            int[] order = {-1, 1, 2, 11, 3, -2, 4, 9, 5, 10, -3, 7, -4, 8, 13, -5, 12, 6, 0};
            int categorie = 0;
            for (int j : order) {
                if (j < 0) {
                    categorie = -j;
                } else if (j != 0) {
                    String nom = context.getResources().getStringArray(R.array.noms)[j - 1];
                    int kmdiesel = Integer.valueOf(context.getResources().getStringArray(R.array.defaut)[((j - 1) * 4) + 2]).intValue();
                    int perioddiesel = Integer.valueOf(context.getResources().getStringArray(R.array.defaut)[((j - 1) * 4) + 3]).intValue();
                    int kmessence = Integer.valueOf(context.getResources().getStringArray(R.array.defaut)[(j - 1) * 4]).intValue();
                    int periodessence = Integer.valueOf(context.getResources().getStringArray(R.array.defaut)[((j - 1) * 4) + 1]).intValue();
                    ContentValues values = new ContentValues();
                    values.put("id", Integer.valueOf(j));
                    values.put("carburant", (Integer) 1);
                    values.put("nom", nom);
                    values.put("categorie", Integer.valueOf(categorie));
                    values.put("freqKm", Integer.valueOf(kmdiesel));
                    values.put("freqDate", Integer.valueOf(perioddiesel));
                    values.put("supprimable", (Integer) 0);
                    values.put("visible", (Integer) 1);
                    sql.insert("list_of_revision", "0", values);
                    ContentValues values2 = new ContentValues();
                    values2.put("id", Integer.valueOf(j));
                    values2.put("carburant", (Integer) 0);
                    values2.put("nom", nom);
                    values2.put("categorie", Integer.valueOf(categorie));
                    values2.put("freqKm", Integer.valueOf(kmessence));
                    values2.put("freqDate", Integer.valueOf(periodessence));
                    values2.put("supprimable", (Integer) 0);
                    values2.put("visible", (Integer) 1);
                    sql.insert("list_of_revision", "0", values2);
                    ContentValues values3 = new ContentValues();
                    values3.put("id", Integer.valueOf(j));
                    values3.put("carburant", (Integer) 2);
                    values3.put("nom", nom);
                    values3.put("categorie", Integer.valueOf(categorie));
                    values3.put("freqKm", Integer.valueOf(kmessence));
                    values3.put("freqDate", Integer.valueOf(periodessence));
                    values3.put("supprimable", (Integer) 0);
                    values3.put("visible", (Integer) 1);
                    sql.insert("list_of_revision", "0", values3);
                    ContentValues values4 = new ContentValues();
                    values4.put("id", Integer.valueOf(j));
                    values4.put("carburant", (Integer) 3);
                    values4.put("nom", nom);
                    values4.put("categorie", Integer.valueOf(categorie));
                    values4.put("freqKm", Integer.valueOf(kmessence));
                    values4.put("freqDate", Integer.valueOf(periodessence));
                    values4.put("supprimable", (Integer) 0);
                    values4.put("visible", (Integer) 1);
                    sql.insert("list_of_revision", "0", values4);
                }
            }
        }
        try {
            sql.query("voiture", new String[]{"cv"}, null, null, null, null, null);
        } catch (SQLiteException e4) {
            sql.execSQL("ALTER TABLE voiture ADD cv INT(4) DEFAULT 0");
            sql.execSQL("ALTER TABLE voiture ADD cvf INT(2) DEFAULT 0");
        }
        try {
            sql.query("voiture", new String[]{"photo"}, null, null, null, null, null);
        } catch (SQLiteException e5) {
            sql.execSQL("ALTER TABLE voiture ADD photo VARCHAR DEFAULT \"\"");
        }
        try {
            sql.query("depenses", new String[]{"voiture"}, null, null, null, null, null);
        } catch (SQLiteException e6) {
            e6.printStackTrace();
            sql.execSQL(context.getResources().getString(R.string.DB_DEPENSES));
            Cursor c = sql.query("voiture,conso", new String[]{"voiture.id", "date", "volume", "prix", "voiture.km AS vkm", "conso.km as ckm"}, "voiture.id=conso.voiture", null, null, null, "date desc", null);
            HashMap<Integer, Integer> voitures = new HashMap<>();
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                if (!voitures.containsKey(Integer.valueOf(c.getInt(c.getColumnIndex("id"))))) {
                    voitures.put(Integer.valueOf(c.getInt(c.getColumnIndex("id"))), Integer.valueOf(c.getInt(c.getColumnIndex("vkm"))));
                }
                voitures.put(Integer.valueOf(c.getInt(c.getColumnIndex("id"))), Integer.valueOf(voitures.get(Integer.valueOf(c.getInt(c.getColumnIndex("id")))).intValue() - c.getInt(c.getColumnIndex("ckm"))));
                ContentValues values5 = new ContentValues();
                values5.put("voiture", Integer.valueOf(c.getInt(c.getColumnIndex("id"))));
                values5.put("type", (Integer) 0);
                values5.put("date", c.getString(c.getColumnIndex("date")));
                values5.put("km", voitures.get(Integer.valueOf(c.getInt(c.getColumnIndex("id")))));
                values5.put("prix", Double.valueOf(c.getDouble(c.getColumnIndex("prix")) * c.getDouble(c.getColumnIndex("volume"))));
                values5.put("description", "");
                if (values5.getAsDouble("prix").doubleValue() != 0.0d) {
                    sql.insert("depenses", "0", values5);
                }
            }
            c.close();
        }
        try {
            sql.query("depenses", new String[]{"description"}, null, null, null, null, null);
        } catch (SQLiteException e7) {
            sql.execSQL("ALTER TABLE depenses ADD description VARCHAR DEFAULT \"\"");
        }
        // normalise the "partiel" flag (0/1 written by some migrations instead of false/true)
        sql.execSQL("UPDATE conso SET partiel='true' WHERE partiel='1' OR partiel='TRUE'");
        sql.execSQL("UPDATE conso SET partiel='false' WHERE partiel IS NULL OR partiel<>'true'");
        Cursor cars = sql.query("voiture", new String[]{"id"}, null, null, null, null, null);
        while (cars.moveToNext()) {
            Utils.syncFuelExpenses(sql, cars.getInt(0));
        }
        cars.close();
        Utils.closeDatabase(context, sql);
    }


    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long voiture) {
        if (arg0.getAdapter() instanceof AccesRapideAdapter) {
            if (voiture == -1) {
                ((AccesRapideAdapter) arg0.getAdapter()).addAt(arg2);
                return;
            }
            Intent intent = new Intent(this, (Class<?>) ViewVoiturePager.class);
            intent.putExtra("voitureid", (int) voiture);
            startActivityForResult(intent, 20);
            return;
        }
        Intent intent2 = null;
        if (arg2 == 0) {
            intent2 = new Intent(this, (Class<?>) ListeVoiture.class);
        } else if (arg2 == 1) {
            intent2 = new Intent(this, (Class<?>) Settings.class);
        } else {
            return;
        }
        if (intent2 != null) {
            startActivity(intent2);
        }
    }

    @Override // android.widget.AdapterView.OnItemLongClickListener
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (arg0.getAdapter() instanceof AccesRapideAdapter) {
            ((AccesRapideAdapter) arg0.getAdapter()).deleteWithAcceptance(arg2);
            return false;
        }
        return false;
    }
}
