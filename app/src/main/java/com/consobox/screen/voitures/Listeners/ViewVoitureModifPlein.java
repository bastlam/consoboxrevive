package com.consobox.screen.voitures.Listeners;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.widget.DatePicker;
import com.consobox.R;
import com.consobox.data.Utils;

/* JADX INFO: loaded from: classes.dex */
public class ViewVoitureModifPlein extends PreferenceActivity implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    Preference mDate;
    double mInitLitre;
    double mInitPrix;
    EditTextPreference mKm;
    EditTextPreference mLitre;
    double mOldKm;
    int mPlein;
    EditTextPreference mPrix;
    String[] mTime;
    int mVoiture;

    @Override // android.preference.PreferenceActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mVoiture = getIntent().getIntExtra("voitureid", 0);
        this.mPlein = getIntent().getIntExtra("pleinid", 0);
        setPreferenceScreen(initContent());
    }

    private PreferenceScreen initContent() {
        SQLiteDatabase sql = Utils.getDatabase(this);
        Cursor c = sql.query("conso", new String[]{"id", "date", "km", "volume", "partiel", "prix"}, "voiture=" + this.mVoiture + " AND id=" + this.mPlein, null, null, null, "date", "10");
        c.moveToFirst();
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
        PreferenceCategory service = new PreferenceCategory(this);
        service.setTitle(getResources().getString(R.string.paramplein));
        root.addPreference(service);
        this.mDate = new Preference(this);
        this.mDate.setTitle("Date");
        this.mTime = c.getString(c.getColumnIndex("date")).split("-");
        this.mDate.setSummary(String.valueOf(this.mTime[2]) + "/" + this.mTime[1] + "/" + this.mTime[0]);
        this.mDate.setOnPreferenceClickListener(this);
        this.mKm = new EditTextPreference(this);
        this.mKm.setTitle(getResources().getString(R.string.nombrekm));
        this.mOldKm = c.getDouble(c.getColumnIndex("km"));
        this.mKm.setSummary(new StringBuilder(String.valueOf(c.getDouble(c.getColumnIndex("km")))).toString());
        this.mKm.setOnPreferenceChangeListener(this);
        this.mLitre = new EditTextPreference(this);
        this.mLitre.setTitle(getResources().getString(R.string.nombrelitre));
        this.mLitre.setSummary(new StringBuilder(String.valueOf(c.getDouble(c.getColumnIndex("volume")))).toString());
        this.mLitre.setOnPreferenceChangeListener(this);
        this.mInitLitre = c.getDouble(c.getColumnIndex("volume"));
        this.mPrix = new EditTextPreference(this);
        this.mPrix.setTitle(getResources().getString(R.string.prix));
        this.mPrix.setSummary(new StringBuilder(String.valueOf(c.getDouble(c.getColumnIndex("prix")))).toString());
        this.mPrix.setOnPreferenceChangeListener(this);
        this.mInitPrix = c.getDouble(c.getColumnIndex("prix"));
        service.addPreference(this.mDate);
        service.addPreference(this.mKm);
        service.addPreference(this.mLitre);
        service.addPreference(this.mPrix);
        c.close();
        Utils.closeDatabase(this, sql);
        return root;
    }

    @Override // android.preference.Preference.OnPreferenceChangeListener
    public boolean onPreferenceChange(Preference arg0, Object arg1) {
        try {
            Double val = Double.valueOf(arg1.toString());
            arg0.setSummary(String.valueOf(val));
            SQLiteDatabase sql = Utils.getDatabase(this);
            if (arg0.equals(this.mLitre)) {
                ContentValues values = new ContentValues();
                values.put("volume", val);
                sql.update("conso", values, "voiture = " + this.mVoiture + " AND id = " + this.mPlein, null);
                ContentValues values2 = new ContentValues();
                values2.put("prix", Double.valueOf(this.mInitPrix * val.doubleValue()));
                this.mInitLitre = val.doubleValue();
                sql.update("depenses", values2, "type=0 AND description=" + this.mPlein, null);
            } else if (arg0.equals(this.mKm)) {
                ContentValues values3 = new ContentValues();
                values3.put("km", val);
                sql.execSQL("UPDATE voiture SET km=(SELECT km FROM voiture WHERE id=" + this.mVoiture + ")-" + this.mOldKm + "+" + val + " WHERE id=" + this.mVoiture);
                sql.update("conso", values3, "voiture = " + this.mVoiture + " AND id = " + this.mPlein, null);
                this.mOldKm = val.doubleValue();
            } else if (arg0.equals(this.mPrix)) {
                ContentValues values4 = new ContentValues();
                values4.put("prix", val);
                sql.update("conso", values4, "voiture = " + this.mVoiture + " AND id = " + this.mPlein, null);
                ContentValues values5 = new ContentValues();
                values5.put("prix", Double.valueOf(val.doubleValue() * this.mInitLitre));
                this.mInitPrix = val.doubleValue();
                sql.update("depenses", values5, "type=0 AND description=" + this.mPlein, null);
            }
            Utils.syncFuelExpenses(sql, this.mVoiture);
            Utils.closeDatabase(this, sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override // android.preference.Preference.OnPreferenceClickListener
    public boolean onPreferenceClick(Preference preference) {
        DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() { // from class: com.consobox.screen.voitures.Listeners.ViewVoitureModifPlein.1
            @Override // android.app.DatePickerDialog.OnDateSetListener
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                ContentValues values = new ContentValues();
                int month = monthOfYear + 1;
                ViewVoitureModifPlein.this.mTime[0] = String.valueOf(year);
                ViewVoitureModifPlein.this.mTime[1] = String.valueOf(month < 10 ? "0" : "") + String.valueOf(month);
                ViewVoitureModifPlein.this.mTime[2] = String.valueOf(dayOfMonth < 10 ? "0" : "") + String.valueOf(dayOfMonth);
                ViewVoitureModifPlein.this.mDate.setSummary(String.valueOf(ViewVoitureModifPlein.this.mTime[2]) + "/" + ViewVoitureModifPlein.this.mTime[1] + "/" + ViewVoitureModifPlein.this.mTime[0]);
                values.put("date", String.valueOf(year) + "-" + (month < 10 ? "0" : "") + month + "-" + dayOfMonth);
                SQLiteDatabase sql = Utils.getDatabase(ViewVoitureModifPlein.this);
                sql.update("conso", values, "voiture = " + ViewVoitureModifPlein.this.mVoiture + " AND id = " + ViewVoitureModifPlein.this.mPlein, null);
                Utils.syncFuelExpenses(sql, ViewVoitureModifPlein.this.mVoiture);
                Utils.closeDatabase(ViewVoitureModifPlein.this, sql);
            }
        };
        new DatePickerDialog(this, mDateSetListener, Integer.valueOf(this.mTime[0]).intValue(), Integer.valueOf(this.mTime[1]).intValue() - 1, Integer.valueOf(this.mTime[2]).intValue()).show();
        return true;
    }
}
