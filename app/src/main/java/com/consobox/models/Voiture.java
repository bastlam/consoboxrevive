package com.consobox.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.consobox.data.Message;
import com.consobox.data.Utils;

/* JADX INFO: loaded from: classes.dex */
public class Voiture {
    public int cv;
    public int cvf;
    public int id;
    public float km;
    public float kmbase;
    public String nom;
    public String photo;
    public Uri photoUri;
    public String type;

    public Voiture(Cursor c) {
        init(c);
    }

    public Voiture() {
    }

    private void init(Cursor c) {
        this.id = c.getInt(c.getColumnIndex("id"));
        this.nom = c.getString(c.getColumnIndex("nom"));
        this.type = c.getString(c.getColumnIndex("type"));
        this.km = c.getFloat(c.getColumnIndex("km"));
        this.kmbase = c.getFloat(c.getColumnIndex("kmbase"));
        this.cv = c.getInt(c.getColumnIndex("cv"));
        this.cvf = c.getInt(c.getColumnIndex("cvf"));
        this.photo = c.getString(c.getColumnIndex("photo"));
    }

    public ContentValues getDataForInsert() {
        ContentValues values = new ContentValues();
        values.put("nom", this.nom);
        values.put("type", this.type);
        values.put("km", Float.valueOf(this.km));
        values.put("cv", Integer.valueOf(this.cv));
        values.put("cvf", Integer.valueOf(this.cvf));
        values.put("photo", this.photo);
        return values;
    }

    public void delete(Context aContext) {
        SQLiteDatabase sql = Utils.getDatabase(aContext);
        sql.execSQL("DELETE FROM voiture WHERE id=" + this.id);
        Utils.closeDatabase(aContext, sql);
    }

    public void updatsValues(Context aContext) {
        SQLiteDatabase sql = Utils.getDatabase(aContext);
        Cursor c = sql.query("voiture", new String[]{"*"}, "id=" + this.id, null, null, null, null);
        if (c.getCount() != 0) {
            c.moveToFirst();
            init(c);
        }
        c.close();
        Utils.closeDatabase(aContext, sql);
    }

    public static void newPlein(Context context, double km, double volume, double prix, String date, int voiture, boolean partiel, boolean relatif) {
        double kmage;
        if (km > 0.0d && volume > 0.0d) {
            SQLiteDatabase sql = Utils.getDatabase(context);
            if (relatif) {
                Cursor c = sql.query("voiture", new String[]{"km"}, "id=" + voiture, null, null, null, null);
                c.moveToFirst();
                if (c.getCount() != 0) {
                    kmage = c.getDouble(c.getColumnIndex("km"));
                } else {
                    kmage = 0.0d;
                }
                km -= kmage;
                c.close();
                if (km < 0.0d) {
                    Message.OKMessage("Vous avez cocher la case 'Km relevés au compteur', vous devez donc indiquer un kilomètrage superieur à celui de la voiture à votre dernier plein", context);
                    return;
                }
            }
            String[] time = date.split("/");
            ContentValues conso = new ContentValues();
            conso.put("voiture", Integer.valueOf(voiture));
            conso.put("date", String.valueOf(time[2]) + "-" + time[1] + "-" + time[0]);
            conso.put("volume", Double.valueOf(volume));
            conso.put("km", Double.valueOf(km));
            conso.put("partiel", String.valueOf(partiel));
            conso.put("prix", Double.valueOf(prix));
            long id = sql.insert("conso", "0", conso);
            ContentValues depense = new ContentValues();
            depense.put("voiture", Integer.valueOf(voiture));
            depense.put("type", (Integer) 0);
            depense.put("date", String.valueOf(time[2]) + "-" + time[1] + "-" + time[0]);
            depense.put("km", Double.valueOf(km));
            depense.put("prix", Double.valueOf(prix * volume));
            depense.put("description", Long.valueOf(id));
            sql.insert("depenses", "0", depense);
            sql.execSQL("UPDATE voiture SET km=(SELECT km FROM voiture WHERE id=" + voiture + ")+" + km + " WHERE id=" + voiture);
            Utils.closeDatabase(context, sql);
        }
    }
}
