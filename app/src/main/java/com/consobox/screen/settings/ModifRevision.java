package com.consobox.screen.settings;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.consobox.R;
import com.consobox.data.Utils;

/* JADX INFO: loaded from: classes.dex */
public class ModifRevision extends Activity implements View.OnClickListener {
    CheckBox[] mChecks;
    EditText[] mDates;
    EditText[] mKms;
    LinearLayout mLayout;
    EditText nom;

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_modifrevision);
        this.mLayout = (LinearLayout) findViewById(R.id.modifrevision_layout);
        ((Button) findViewById(R.id.modifrevision_annuler)).setOnClickListener(this);
        ((Button) findViewById(R.id.modifrevision_enregistrer)).setOnClickListener(this);
        initView();
    }

    @Override // android.app.Activity
    protected void onPause() {
        super.onPause();
    }

    private void initView() {
        String kmtext;
        String datetext;
        boolean exist;
        int type = getIntent().getExtras().getInt("type", -1);
        deleteContentOnClickListener deleteOnClick = new deleteContentOnClickListener();
        TextView title = new TextView(this);
        title.setText("Nom");
        title.setTextColor(getResources().getColor(android.R.color.primary_text_dark));
        title.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.title_bar));
        title.setGravity(3);
        title.setTypeface(null, 1);
        this.mLayout.addView(title, new ViewGroup.LayoutParams(-1, -2));
        this.nom = new EditText(this);
        this.mLayout.addView(this.nom, new ViewGroup.LayoutParams(-1, -2));
        String[] carburants = getApplicationContext().getResources().getStringArray(R.array.type_of_vehicule);
        this.mDates = new EditText[carburants.length];
        this.mKms = new EditText[carburants.length];
        this.mChecks = new CheckBox[carburants.length];
        for (int i = 0; i < carburants.length; i++) {
            TextView t = new TextView(this);
            t.setText(carburants[i]);
            t.setTextColor(getResources().getColor(android.R.color.primary_text_dark));
            t.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.title_bar));
            t.setGravity(3);
            t.setTypeface(null, 1);
            this.mLayout.addView(t);
            SQLiteDatabase sql = Utils.getDatabase(this);
            Cursor c = sql.query("list_of_revision", new String[]{"nom", "freqKm", "freqDate"}, "id=" + type + " AND carburant=" + i, null, null, null, null);
            c.moveToFirst();
            if (c.getCount() == 0) {
                kmtext = getResources().getString(R.string.nbkm);
                datetext = getResources().getString(R.string.nbmois);
                exist = false;
            } else {
                this.nom.setText(c.getString(c.getColumnIndex("nom")));
                kmtext = c.getString(c.getColumnIndex("freqKm"));
                datetext = c.getString(c.getColumnIndex("freqDate"));
                exist = true;
            }
            c.close();
            Utils.closeDatabase(this, sql);
            EditText km = new EditText(this);
            km.setOnFocusChangeListener(deleteOnClick);
            km.setText(kmtext);
            km.setInputType(2);
            this.mKms[i] = km;
            this.mLayout.addView(km, new ViewGroup.LayoutParams(-1, -2));
            EditText date = new EditText(this);
            date.setText(datetext);
            date.setOnFocusChangeListener(deleteOnClick);
            date.setInputType(2);
            this.mDates[i] = date;
            this.mLayout.addView(date, new ViewGroup.LayoutParams(-1, -2));
            CheckBox valide = new CheckBox(this);
            valide.setChecked(exist);
            valide.setText("Valide");
            this.mChecks[i] = valide;
            this.mLayout.addView(valide, new ViewGroup.LayoutParams(-1, -2));
        }
    }

    class deleteContentOnClickListener implements View.OnFocusChangeListener {
        deleteContentOnClickListener() {
        }

        @Override // android.view.View.OnFocusChangeListener
        public void onFocusChange(View arg0, boolean arg1) {
            if (arg1) {
                ((EditText) arg0).setText("");
            }
        }
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        if (v.equals(findViewById(R.id.modifrevision_annuler))) {
            finish();
        } else if (v.equals(findViewById(R.id.modifrevision_enregistrer))) {
            enregistrer();
            finish();
        }
    }

    private void enregistrer() {
        int categorie;
        int date;
        int km;
        int type = getIntent().getExtras().getInt("type", -1);
        if (type == -1) {
            type = getNewTypeInt();
            categorie = getIntent().getExtras().getInt("categorie", -1);
        } else {
            categorie = getCategorie(type);
        }
        int nbOfCarburant = getApplicationContext().getResources().getStringArray(R.array.type_of_vehicule).length;
        for (int i = 0; i < nbOfCarburant; i++) {
            if (!this.mChecks[i].isChecked()) {
                SQLiteDatabase sql = Utils.getDatabase(this);
                sql.execSQL("DELETE FROM list_of_revision WHERE id=" + type + " AND carburant=" + i);
                Utils.closeDatabase(this, sql);
            } else {
                SQLiteDatabase sql2 = Utils.getDatabase(this);
                sql2.execSQL("DELETE FROM list_of_revision WHERE id=" + type + " AND carburant=" + i);
                ContentValues values = new ContentValues();
                values.put("id", Integer.valueOf(type));
                values.put("categorie", Integer.valueOf(categorie));
                values.put("nom", this.nom.getText().toString());
                values.put("carburant", Integer.valueOf(i));
                values.put("supprimable", (Integer) 1);
                values.put("visible", (Integer) 1);
                try {
                    date = Integer.valueOf(this.mDates[i].getText().toString()).intValue();
                } catch (Exception e) {
                    date = 0;
                }
                values.put("freqDate", Integer.valueOf(date));
                try {
                    km = Integer.valueOf(this.mKms[i].getText().toString()).intValue();
                } catch (Exception e2) {
                    km = 0;
                }
                values.put("freqKm", Integer.valueOf(km));
                sql2.insert("list_of_revision", "0", values);
                Utils.closeDatabase(this, sql2);
            }
        }
    }

    private int getCategorie(int type) {
        SQLiteDatabase sql = Utils.getDatabase(this);
        Cursor c = sql.query("list_of_revision", new String[]{"categorie"}, "id=" + type, null, null, null, null);
        c.moveToFirst();
        int categorie = c.getInt(c.getColumnIndex("categorie"));
        c.close();
        Utils.closeDatabase(this, sql);
        return categorie;
    }

    private int getNewTypeInt() {
        SQLiteDatabase sql = Utils.getDatabase(this);
        Cursor c = sql.query("list_of_revision", new String[]{"max(id) AS last"}, null, null, null, null, null);
        c.moveToFirst();
        int last = c.getInt(c.getColumnIndex("last"));
        c.close();
        Utils.closeDatabase(this, sql);
        return last + 1;
    }
}
