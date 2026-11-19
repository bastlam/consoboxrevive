package com.consobox.screen.voitures;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.consobox.R;
import com.consobox.data.Utils;
import java.text.DecimalFormat;
import java.util.Calendar;

/* JADX INFO: loaded from: classes.dex */
public class StatsGeneral extends Activity {
    private int mVoiture;
    private double totalJour;
    private double totalKm;

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_statsgeneral);
        this.mVoiture = getIntent().getIntExtra("voiture", 0);
        initDistance();
        initEssence();
        initDepenses();
    }

    @Override // android.app.Activity
    public void onDestroy() {
        super.onDestroy();
    }

    private void initEssence() {
        TextView text = (TextView) findViewById(R.id.essence);
        SQLiteDatabase sql = Utils.getDatabase(this);
        Cursor c = sql.query("conso", new String[]{"volume", "km"}, Utils.SQL_PLEIN_COMPLET + " AND km>0 AND voiture=" + this.mVoiture, null, null, null, "date ASC");
        if (c.getCount() == 0) {
            text.setText(getResources().getString(R.string.aucunedonnee));
            Utils.closeDatabase(this, sql);
            return;
        }
        c.moveToFirst();
        double min = 100.0d;
        double max = 0.0d;
        double avg = 0.0d;
        double l100 = 0.0d;
        for (int i = 0; i < c.getCount(); i++) {
            l100 = (c.getDouble(c.getColumnIndex("volume")) / c.getDouble(c.getColumnIndex("km"))) * 100.0d;
            avg += l100;
            if (l100 > max) {
                max = l100;
            }
            if (l100 < min) {
                min = l100;
            }
            c.moveToNext();
        }
        double avg2 = avg / ((double) c.getCount());
        c.close();
        Utils.closeDatabase(this, sql);
        DecimalFormat df = new DecimalFormat("#.##");
        String styledText = String.valueOf(getResources().getString(R.string.statgeneral1)) + df.format(avg2) + getResources().getString(R.string.statgeneral2) + df.format(max) + getResources().getString(R.string.statgeneral3) + df.format(min) + getResources().getString(R.string.statgeneral4) + df.format(l100) + getResources().getString(R.string.statgeneral5);
        text.setText(Html.fromHtml(styledText), TextView.BufferType.SPANNABLE);
    }

    private void initDistance() {
        TextView text = (TextView) findViewById(R.id.distance);
        SQLiteDatabase sql = Utils.getDatabase(this);
        Cursor c = sql.query("conso", new String[]{"date", "km"}, "voiture=" + this.mVoiture, null, null, null, "date ASC");
        if (c.getCount() == 0) {
            text.setText(getResources().getString(R.string.aucunedonnee));
            this.totalKm = 0.0d;
            Utils.closeDatabase(this, sql);
            return;
        }
        c.moveToFirst();
        String[] firstDate = c.getString(c.getColumnIndex("date")).split("-");
        double totalConsobox = 0.0d;
        for (int i = 0; i < c.getCount(); i++) {
            totalConsobox += c.getDouble(c.getColumnIndex("km"));
            c.moveToNext();
        }
        c.close();
        Cursor c2 = sql.query("voiture", new String[]{"km"}, "id=" + this.mVoiture, null, null, null, null);
        c2.moveToFirst();
        long totalKm = Math.round(c2.getDouble(c2.getColumnIndex("km")));
        c2.close();
        Utils.closeDatabase(this, sql);
        Calendar cal = Calendar.getInstance();
        cal.set(Integer.valueOf(firstDate[0]).intValue(), Integer.valueOf(firstDate[1]).intValue() - 1, Integer.valueOf(firstDate[2]).intValue());
        double duree = (System.currentTimeMillis() - cal.getTimeInMillis()) / 100000.0d;
        if (duree < 864.0d) {
            duree = 864.0d;
        }
        long kmParJour = Math.round(totalConsobox / (duree / 864.0d));
        long kmParMois = Math.round(totalConsobox / (duree / 26784.0d));
        long kmParAn = Math.round(totalConsobox / (duree / 315360.0d));
        String styledText = String.valueOf(getResources().getString(R.string.statgeneral6)) + totalKm + getResources().getString(R.string.statgeneral7) + ((long) totalConsobox) + getResources().getString(R.string.statgeneral8) + getResources().getString(R.string.statgeneral9) + kmParJour + getResources().getString(R.string.statgeneral10) + getResources().getString(R.string.statgeneral11) + kmParMois + getResources().getString(R.string.statgeneral12) + getResources().getString(R.string.statgeneral11) + kmParAn + getResources().getString(R.string.statgeneral13);
        text.setText(Html.fromHtml(styledText), TextView.BufferType.SPANNABLE);
        this.totalKm = totalConsobox;
        this.totalJour = duree / 864.0d;
    }

    private void initDepenses() {
        TextView text = (TextView) findViewById(R.id.depenses);
        SQLiteDatabase sql = Utils.getDatabase(this);
        // fuel costs only (repair / maintenance expenses are not shown any more)
        Cursor c = sql.rawQuery("SELECT COUNT(*), TOTAL(prix) FROM depenses WHERE type=0 AND voiture=" + this.mVoiture, null);
        c.moveToFirst();
        int count = c.getInt(0);
        double essence = c.getDouble(1);
        c.close();
        Utils.closeDatabase(this, sql);
        if (count == 0) {
            text.setText(getResources().getString(R.string.aucunedonnee));
            return;
        }
        DecimalFormat df = new DecimalFormat("#.##");
        String unit = Utils.getDeviseUnit(this);
        String styledText = getResources().getString(R.string.statgeneral14) + df.format(essence) + unit
                + getResources().getString(R.string.statgeneral_fuel)
                + getResources().getString(R.string.statgeneral17) + df.format(this.totalKm > 0 ? essence / this.totalKm : 0) + unit
                + getResources().getString(R.string.statgeneral18) + df.format(this.totalJour > 0 ? essence / this.totalJour : 0) + unit
                + getResources().getString(R.string.statgeneral19);
        text.setText(Html.fromHtml(styledText), TextView.BufferType.SPANNABLE);
    }
}
