package com.consobox.widget;

import android.app.ListActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import com.consobox.R;
import com.consobox.Splash;
import com.consobox.data.Message;
import com.consobox.data.Utils;
import com.consobox.models.Voiture;

/* JADX INFO: loaded from: classes.dex */
public class AddPleinIntent extends ListActivity implements View.OnClickListener {
    private int[] mId = null;
    private int mIdVoiture;

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        String[] listOfCar;
        super.onCreate(savedInstanceState);
        Splash.versionUpdater(this);
        try {
            SQLiteDatabase sql = Utils.getDatabase(this);
            sql.execSQL(getResources().getString(R.string.DB_VOITURE));
            Cursor c = sql.query("voiture", new String[]{"id", "nom"}, null, null, null, null, null);
            if (c.getCount() == 0) {
                this.mId = new int[1];
                this.mId[0] = -2;
                listOfCar = new String[]{"Pas de voiture"};
            } else {
                c.moveToFirst();
                listOfCar = new String[c.getCount()];
                this.mId = new int[c.getCount()];
                for (int i = 0; i < c.getCount(); i++) {
                    this.mId[i] = c.getInt(c.getColumnIndex("id"));
                    listOfCar[i] = c.getString(c.getColumnIndex("nom"));
                    c.moveToNext();
                }
                if (c.getCount() == 1) {
                    this.mIdVoiture = this.mId[0];
                    Message.EssenceBox(this, this, "0");
                }
            }
            c.close();
            Utils.closeDatabase(this, sql);
        } catch (Exception e) {
            this.mId = new int[1];
            this.mId[0] = -2;
            listOfCar = new String[]{getResources().getString(R.string.pasdevoiture)};
        }
        setListAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, listOfCar));
    }

    @Override // android.app.ListActivity
    protected void onListItemClick(ListView l, View v, int position, long id) {
        this.mIdVoiture = this.mId[position];
        if (this.mIdVoiture == -2) {
            System.exit(-1);
        }
        Message.EssenceBox(this, this, "0");
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View arg0) {
        if (arg0.equals(Message.dialogInProgress.findViewById(R.id.essencebox_ok))) {
            try {
                Voiture.newPlein(this, Double.valueOf(((TextView) Message.dialogInProgress.findViewById(R.id.essencebox_km)).getText().toString().replace(",", ".")).doubleValue(), Double.valueOf(((TextView) Message.dialogInProgress.findViewById(R.id.essencebox_litre)).getText().toString().replace(",", ".")).doubleValue(), Double.valueOf(((TextView) Message.dialogInProgress.findViewById(R.id.essencebox_prix)).getText().toString().replace(",", ".")).doubleValue(), ((TextView) Message.dialogInProgress.findViewById(R.id.essencebox_date)).getText().toString(), this.mIdVoiture, ((CheckBox) Message.dialogInProgress.findViewById(R.id.essence_box_partiel)).isChecked(), ((CheckBox) Message.dialogInProgress.findViewById(R.id.essencebox_kmrelatif)).isChecked());
            } catch (NumberFormatException e) {
            }
            if (Message.dialogInProgress.isShowing()) {
                Message.dialogInProgress.cancel();
            }
        } else if (arg0.equals(Message.dialogInProgress.findViewById(R.id.essencebox_annuler)) && Message.dialogInProgress.isShowing()) {
            Message.dialogInProgress.cancel();
        }
        System.exit(-1);
    }
}
