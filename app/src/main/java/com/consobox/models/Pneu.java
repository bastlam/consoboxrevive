package com.consobox.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.consobox.data.Utils;

/* JADX INFO: loaded from: classes.dex */
public class Pneu {
    public String dateAchat;
    public String dateMontage;
    public String description;
    public int id;
    public boolean isHiver;
    public long kmUtilisation;
    public String marque;
    public String modele;
    public double prix;
    public long tempsUtilisation;
    public boolean usable;
    public int used;
    public static byte NON_MONTER = 0;
    public static byte AVANT = 1;
    public static byte ARRIERE = 2;

    public Pneu(Cursor c) {
        this.id = c.getInt(c.getColumnIndex("id"));
        this.marque = c.getString(c.getColumnIndex("marque"));
        this.modele = c.getString(c.getColumnIndex("modele"));
        this.description = c.getString(c.getColumnIndex("description"));
        this.usable = c.getString(c.getColumnIndex("usable")).equals("true");
        this.used = c.getInt(c.getColumnIndex("used"));
        this.isHiver = c.getString(c.getColumnIndex("hiver")).equals("true");
        String[] date = c.getString(c.getColumnIndex("date_montage")).split("-");
        this.dateMontage = String.valueOf(date[2]) + "/" + date[1] + "/" + date[0];
        String[] date2 = c.getString(c.getColumnIndex("date_achat")).split("-");
        this.dateAchat = String.valueOf(date2[2]) + "/" + date2[1] + "/" + date2[0];
        this.tempsUtilisation = c.getInt(c.getColumnIndex("temps_monte"));
        this.kmUtilisation = c.getInt(c.getColumnIndex("km"));
        this.prix = c.getDouble(c.getColumnIndex("prix"));
    }

    public Pneu() {
    }

    public ContentValues getDataForInsert() {
        ContentValues values = new ContentValues();
        values.put("marque", this.marque);
        values.put("modele", this.modele);
        values.put("description", this.description);
        values.put("used", Integer.valueOf(this.used));
        values.put("usable", String.valueOf(this.usable));
        String[] date = this.dateAchat.split("/");
        values.put("date_achat", String.valueOf(date[2]) + "-" + (Integer.parseInt(date[1]) < 10 ? "0" : "") + Integer.parseInt(date[1]) + "-" + (Integer.parseInt(date[0]) < 10 ? "0" : "") + Integer.parseInt(date[0]));
        String[] date2 = this.dateMontage.split("/");
        values.put("date_montage", String.valueOf(date2[2]) + "-" + (Integer.parseInt(date2[1]) < 10 ? "0" : "") + Integer.parseInt(date2[1]) + "-" + (Integer.parseInt(date2[0]) < 10 ? "0" : "") + Integer.parseInt(date2[0]));
        values.put("hiver", String.valueOf(this.isHiver));
        values.put("temps_monte", Long.valueOf(this.tempsUtilisation));
        values.put("km", Long.valueOf(this.kmUtilisation));
        values.put("prix", Double.valueOf(this.prix));
        return values;
    }

    public void delete(Context aContext) {
        SQLiteDatabase sql = Utils.getDatabase(aContext);
        sql.execSQL("DELETE FROM pneu WHERE id=" + this.id);
        Utils.closeDatabase(aContext, sql);
    }
}
