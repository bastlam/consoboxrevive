package com.consobox.screen.voitures;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.consobox.R;
import com.consobox.data.Message;
import com.consobox.data.Utils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/* JADX INFO: loaded from: classes.dex */
public class Revision extends Activity implements CompoundButton.OnCheckedChangeListener, AdapterView.OnItemClickListener, View.OnClickListener {
    public static boolean active;
    private String date;
    private int kmage;
    public String modifying;
    private ArrayList<HashMap<String, String>> mylist;

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getIntent().getStringExtra("nom"));
        setContentView(R.layout.layout_revision);
        initValues();
        ((Button) findViewById(R.id.revisionbouton)).setOnClickListener(this);
    }

    private void initValues() {
        CheckBox cBox;
        this.mylist = new ArrayList<>();
        int carburant = getIntent().getIntExtra("typeofcar", 0);
        SQLiteDatabase sql = Utils.getDatabase(this);
        Cursor c = sql.query("revisions", new String[]{"valide", "dateLast", "kmLast", "freqKm", "freqDate"}, "voiture=" + getIntent().getIntExtra("voitureid", 0) + " AND type=" + getIntent().getStringExtra("type"), null, null, null, null);
        c.moveToFirst();
        Cursor revision = sql.query("list_of_revision", new String[]{"id", "nom", "freqKm", "freqDate"}, "id=" + getIntent().getStringExtra("type") + " AND carburant=" + carburant, null, null, null, null);
        revision.moveToFirst();
        if (c.getCount() == 0) {
            active = false;
            HashMap<String, String> map = new HashMap<>();
            map.put("id", "1");
            map.put("nom", getResources().getString(R.string.datederniererevision));
            map.put("value", getResources().getString(R.string.jamais));
            this.mylist.add(map);
            HashMap<String, String> map2 = new HashMap<>();
            map2.put("id", "2");
            map2.put("nom", getResources().getString(R.string.compteurderniererevision));
            this.kmage = 0;
            map2.put("value", "0 Km");
            this.mylist.add(map2);
            HashMap<String, String> map3 = new HashMap<>();
            map3.put("id", "3");
            map3.put("nom", getResources().getString(R.string.periodicitemois));
            map3.put("value", String.valueOf(revision.getInt(revision.getColumnIndex("freqDate"))) + " Mois");
            this.mylist.add(map3);
            HashMap<String, String> map4 = new HashMap<>();
            map4.put("id", "4");
            map4.put("nom", getResources().getString(R.string.periodicitekm));
            map4.put("value", String.valueOf(revision.getInt(revision.getColumnIndex("freqKm"))) + " Km");
            this.mylist.add(map4);
            cBox = (CheckBox) findViewById(R.id.revision_active);
            cBox.setText(getResources().getString(R.string.revisiondesactivee));
        } else {
            active = c.getInt(c.getColumnIndex("valide")) == 1;
            HashMap<String, String> map5 = new HashMap<>();
            map5.put("id", "1");
            map5.put("nom", getResources().getString(R.string.datederniererevision));
            Date date = new Date();
            long sub = 0;
            if (c.getString(c.getColumnIndex("dateLast")) != "0") {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);
                try {
                    date = sdf.parse(c.getString(c.getColumnIndex("dateLast")));
                } catch (ParseException e) {
                }
                long sub2 = new Date().getTime() - (date.getTime() + ((long) date.getTimezoneOffset()));
                sub = (long) (sub2 / 2.6352E9d);
            }
            map5.put("value", c.getString(c.getColumnIndex("dateLast")) != "0" ? "Il y a " + sub + " Mois" : "Jamais");
            this.mylist.add(map5);
            HashMap<String, String> map6 = new HashMap<>();
            map6.put("id", "2");
            map6.put("nom", getResources().getString(R.string.compteurderniererevision));
            map6.put("value", String.valueOf(c.getString(c.getColumnIndex("kmLast"))) + " Km");
            this.kmage = c.getInt(c.getColumnIndex("kmLast"));
            this.mylist.add(map6);
            HashMap<String, String> map7 = new HashMap<>();
            map7.put("id", "3");
            map7.put("nom", getResources().getString(R.string.periodicitemois));
            map7.put("value", String.valueOf(c.getString(c.getColumnIndex("freqDate"))) + " Mois");
            this.mylist.add(map7);
            HashMap<String, String> map8 = new HashMap<>();
            map8.put("id", "4");
            map8.put("nom", getResources().getString(R.string.periodicitekm));
            map8.put("value", String.valueOf(c.getString(c.getColumnIndex("freqKm"))) + " Km");
            this.mylist.add(map8);
            cBox = (CheckBox) findViewById(R.id.revision_active);
            cBox.setChecked(active);
            cBox.setText(active ? getResources().getString(R.string.revisionactivee) : getResources().getString(R.string.revisiondesactivee));
        }
        c.close();
        revision.close();
        Utils.closeDatabase(this, sql);
        findViewById(R.id.revisionbouton).setEnabled(active);
        cBox.setOnCheckedChangeListener(this);
        SimpleAdapter listAdapter = new SimpleAdapter(this, this.mylist, R.layout.row_revisionparam, new String[]{"nom", "value"}, new int[]{R.id.rev_list_nom, R.id.rev_list_value});
        ((ListView) findViewById(R.id.listrevparam)).setAdapter((ListAdapter) listAdapter);
        ((ListView) findViewById(R.id.listrevparam)).setOnItemClickListener(this);
    }

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
        HashMap<String, String> hashMap = (HashMap) ((SimpleAdapter) parent.getAdapter()).getItem(position);
        if (hashMap.get("id") != "1" && hashMap.get("id") != "2") {
            if (!active) {
                Message.ErrorMessage(getResources().getString(R.string.vousdevezactiver), this);
                return;
            }
            SQLiteDatabase sql = Utils.getDatabase(this);
            Cursor c = sql.query("revisions", new String[]{"dateLast", "kmLast", "freqDate", "freqKm"}, "voiture=" + getIntent().getIntExtra("voitureid", 0) + " AND type=" + getIntent().getStringExtra("type"), null, null, null, null);
            c.moveToFirst();
            this.modifying = c.getColumnName(Integer.valueOf(hashMap.get("id")).intValue() - 1);
            Message.ChangeDoubleDialog(getResources().getString(R.string.modifierparametre), this, this, c.getInt(Integer.valueOf(hashMap.get("id")).intValue() - 1));
            c.close();
            Utils.closeDatabase(this, sql);
        }
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        if (v.getId() == R.id.ok) {
            try {
                int val = Integer.valueOf(((EditText) Message.dialogInProgress.findViewById(R.id.value)).getText().toString()).intValue();
                if (this.modifying.equals("prix")) {
                    Message.dialogInProgress.cancel();
                    addDepenseWithLastRevision(val);
                    return;
                }
                if (this.modifying.equals("kmLast")) {
                    this.modifying = "prix";
                    this.kmage = val;
                    Message.dialogInProgress.cancel();
                    Message.ChangeDoubleDialog(getResources().getString(R.string.prix), this, this, 0.0d);
                    return;
                }
                ContentValues values = new ContentValues();
                values.put(this.modifying, Integer.valueOf(val));
                SQLiteDatabase sql = Utils.getDatabase(this);
                sql.update("revisions", values, "voiture = " + getIntent().getIntExtra("voitureid", 0) + " AND type = " + getIntent().getStringExtra("type"), null);
                Utils.closeDatabase(this, sql);
                initValues();
                if (Message.dialogInProgress.isShowing()) {
                    Message.dialogInProgress.cancel();
                }
            } catch (Exception e) {
            }
        } else if (v.getId() == R.id.revisionbouton) {
            faireLaRevision();
        } else if (v.getId() == R.id.annuler) {
            Message.dialogInProgress.cancel();
        }
    }

    private void addDepenseWithLastRevision(int prix) {
        ContentValues values = new ContentValues();
        values.put("kmLast", Integer.valueOf(this.kmage));
        values.put("dateLast", this.date);
        SQLiteDatabase sql = Utils.getDatabase(this);
        sql.update("revisions", values, "voiture = " + getIntent().getIntExtra("voitureid", 0) + " AND type = " + getIntent().getStringExtra("type"), null);
        ContentValues values2 = new ContentValues();
        values2.put("voiture", Integer.valueOf(getIntent().getIntExtra("voitureid", 0)));
        values2.put("type", Integer.valueOf(Integer.parseInt(getIntent().getStringExtra("type")) + 10));
        values2.put("date", this.date);
        values2.put("km", Integer.valueOf(this.kmage));
        values2.put("prix", Integer.valueOf(prix));
        sql.insert("depenses", null, values2);
        Utils.closeDatabase(this, sql);
        initValues();
        Toast.makeText(this, "Revision ajoutée avec succes", 1).show();
    }

    private void faireLaRevision() {
        DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() { // from class: com.consobox.screen.voitures.Revision.1
            @Override // android.app.DatePickerDialog.OnDateSetListener
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Revision.this.date = String.valueOf(year) + "-" + (monthOfYear + 1 < 10 ? "0" : "") + (monthOfYear + 1) + "-" + (dayOfMonth < 10 ? "0" : "") + dayOfMonth;
                Revision.this.modifying = "kmLast";
                Message.ChangeDoubleDialog(Revision.this.getResources().getString(R.string.kmcompteur), Revision.this, Revision.this, Revision.this.kmage);
            }
        };
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, mDateSetListener, cal.get(1), cal.get(2), cal.get(5)).show();
    }

    @Override // android.widget.CompoundButton.OnCheckedChangeListener
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SQLiteDatabase sql = Utils.getDatabase(this);
        Cursor c = sql.query("revisions", new String[]{"valide", "dateLast", "kmLast", "freqKm", "freqDate"}, "voiture=" + getIntent().getIntExtra("voitureid", 0) + " AND type=" + getIntent().getStringExtra("type"), null, null, null, null);
        c.moveToFirst();
        if (c.getCount() == 0) {
            int carburant = getIntent().getIntExtra("typeofcar", 0);
            ContentValues values = new ContentValues();
            values.put("voiture", Integer.valueOf(getIntent().getIntExtra("voitureid", 0)));
            values.put("type", getIntent().getStringExtra("type"));
            values.put("dateLast", (Integer) 0);
            values.put("kmLast", (Integer) 0);
            values.put("valide", Integer.valueOf(isChecked ? 1 : 0));
            c = sql.query("list_of_revision", new String[]{"freqKm", "freqDate"}, "id=" + getIntent().getStringExtra("type") + " AND carburant=" + carburant, null, null, null, null);
            c.moveToFirst();
            values.put("freqKm", c.getString(c.getColumnIndex("freqKm")));
            values.put("freqDate", c.getString(c.getColumnIndex("freqDate")));
            sql.insert("revisions", "0", values);
        } else {
            ContentValues values2 = new ContentValues();
            values2.put("valide", Integer.valueOf(isChecked ? 1 : 0));
            sql.update("revisions", values2, "voiture = " + getIntent().getIntExtra("voitureid", 0) + " AND type = " + getIntent().getStringExtra("type"), null);
        }
        c.close();
        Utils.closeDatabase(this, sql);
        initValues();
        active = isChecked;
        findViewById(R.id.revisionbouton).setEnabled(active);
    }
}
