package com.consobox.widget;

import android.app.ListActivity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RemoteViews;
import com.consobox.R;
import com.consobox.Splash;
import com.consobox.data.Utils;

/* JADX INFO: loaded from: classes.dex */
public class AddPleinConfigure extends ListActivity {
    int mAppWidgetId;
    int[] mId;
    String text;
    int voiture;

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        String[] items;
        super.onCreate(savedInstanceState);
        Splash.versionUpdater(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.mAppWidgetId = extras.getInt("appWidgetId", 0);
        }
        this.voiture = Utils.getWidgetPref(this).getInt("voitureof" + this.mAppWidgetId, -2);
        this.text = "guy";
        if (this.voiture == -2) {
            SQLiteDatabase sql = Utils.getDatabase(this);
            sql.execSQL(getResources().getString(R.string.DB_VOITURE));
            Cursor c = sql.query("voiture", new String[]{"id", "nom"}, null, null, null, null, null);
            if (c.getCount() == 0) {
                items = new String[]{getResources().getString(R.string.laisserlechoix)};
            } else {
                c.moveToFirst();
                items = new String[c.getCount() + 1];
                this.mId = new int[c.getCount() + 1];
                items[0] = getResources().getString(R.string.laisserlechoix);
                this.mId[0] = -1;
                for (int j = 0; j < c.getCount(); j++) {
                    this.mId[j + 1] = c.getInt(c.getColumnIndex("id"));
                    items[j + 1] = c.getString(c.getColumnIndex("nom"));
                    c.moveToNext();
                }
            }
            c.close();
            Utils.closeDatabase(this, sql);
            setListAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, items));
        }
    }

    @Override // android.app.ListActivity
    protected void onListItemClick(ListView l, View v, int position, long id) {
        this.voiture = this.mId[position];
        Utils.getWidgetPref(this).edit().putInt("voitureof" + this.mAppWidgetId, this.voiture).commit();
        if (this.voiture == -1) {
            this.text = getString(R.string.app_name);
        } else {
            this.voiture = Utils.getWidgetPref(this).getInt("voitureof" + this.mAppWidgetId, 0);
            SQLiteDatabase sql = Utils.getDatabase(this);
            sql.execSQL(getResources().getString(R.string.DB_VOITURE));
            Cursor c = sql.query("voiture", new String[]{"nom"}, "id=" + this.voiture, null, null, null, null);
            if (c.getCount() == 0) {
                this.text = "Introuvable";
            } else {
                c.moveToFirst();
                this.text = c.getString(c.getColumnIndex("nom"));
            }
            c.close();
            Utils.closeDatabase(this, sql);
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        Intent intent = new Intent(this, (Class<?>) AddPleinIntent.class);
        intent.putExtra("voiture", this.voiture);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_addplein);
        views.setOnClickPendingIntent(R.id.addplein, pendingIntent);
        views.setCharSequence(R.id.widgetaddplein_label, "setText", this.text);
        appWidgetManager.updateAppWidget(this.mAppWidgetId, views);
        Intent resultValue = new Intent();
        resultValue.putExtra("appWidgetId", this.mAppWidgetId);
        setResult(-1, resultValue);
        finish();
    }
}
