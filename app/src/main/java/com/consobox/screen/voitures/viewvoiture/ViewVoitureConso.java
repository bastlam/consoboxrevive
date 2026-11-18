package com.consobox.screen.voitures.viewvoiture;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.consobox.R;
import com.consobox.data.Message;
import com.consobox.data.Utils;
import com.consobox.models.Voiture;
import com.consobox.screen.voitures.Listeners.ViewVoitureOnLongClickPlein;
import com.consobox.screen.voitures.Listeners.ViewVoitureOnScrollPlein;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/* JADX INFO: loaded from: classes.dex */
public class ViewVoitureConso extends LinearLayout implements OnVisible, ViewVoitureOnScrollPlein.BackConso, View.OnClickListener {
    private ArrayList<HashMap<String, String>> mConsoList;
    private SimpleAdapter mConsoListAdapter;
    private Context mContext;
    private OnDataChangeListener mListener;
    private Voiture mVoiture;

    public ViewVoitureConso(Context context, Voiture voiture, OnDataChangeListener listener) {
        super(context);
        this.mVoiture = voiture;
        this.mContext = context;
        this.mListener = listener;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService("layout_inflater");
        View v = inflater.inflate(R.layout.layout_viewvoitureconso, this);
        v.findViewById(R.id.view_voiture_ajouter).setOnClickListener(this);
    }

    @Override // com.consobox.screen.voitures.Listeners.ViewVoitureOnScrollPlein.BackConso
    public void initConsoList(boolean ajouterAExistant, int offset) {
        String labelconso;
        SQLiteDatabase sql = Utils.getDatabase(this.mContext);
        sql.execSQL(getResources().getString(R.string.DB_CONSO));
        ListView list = (ListView) findViewById(R.id.view_voiture_listconso);
        list.setOnScrollListener(null);
        if (!ajouterAExistant) {
            this.mConsoList = new ArrayList<>();
        }
        DecimalFormat df = new DecimalFormat("#.##");
        Cursor c = sql.query("conso", new String[]{"id", "date", "km", "volume", "partiel", "prix"}, "voiture=" + this.mVoiture.id, null, null, null, "date DESC", String.valueOf(offset + 15));
        c.moveToFirst();
        for (int i = 0; i < c.getCount(); i++) {
            c.moveToPosition(i);
            if (i >= offset) {
                HashMap<String, String> map = new HashMap<>();
                String[] dat = c.getString(c.getColumnIndex("date")).split("-");
                map.put("date", String.valueOf(getResources().getString(R.string.pleindu)) + dat[2] + "/" + dat[1] + "/" + dat[0]);
                map.put("id", c.getString(c.getColumnIndex("id")));
                map.put("volume", String.valueOf(c.getString(c.getColumnIndex("volume"))) + "l");
                map.put("km", String.valueOf(c.getString(c.getColumnIndex("km"))) + "km");
                if (c.getDouble(c.getColumnIndex("prix")) != 0.0d) {
                    map.put("prix", String.valueOf(c.getString(c.getColumnIndex("prix"))) + Utils.getDeviseUnit(this.mContext) + "/" + getResources().getString(R.string.litre));
                    map.put("prixtotal", String.valueOf(getResources().getString(R.string.soit)) + (((double) ((int) ((c.getDouble(c.getColumnIndex("prix")) * c.getDouble(c.getColumnIndex("volume"))) * 10.0d))) / 10.0d) + Utils.getDeviseUnit(this.mContext));
                } else {
                    map.put("prix", "");
                    map.put("prixtotal", "");
                }
                if (c.getString(c.getColumnIndex("partiel")).equals("false")) {
                    labelconso = String.valueOf(df.format((c.getDouble(c.getColumnIndex("volume")) / c.getDouble(c.getColumnIndex("km"))) * 100.0d)) + "l/100km";
                } else {
                    labelconso = "";
                }
                map.put("conso", labelconso);
                this.mConsoList.add(map);
            }
        }
        c.close();
        Utils.closeDatabase(this.mContext, sql);
        if (!ajouterAExistant) {
            this.mConsoListAdapter = new SimpleAdapter(this.mContext, this.mConsoList, R.layout.row_pleinlist, new String[]{"date", "volume", "km", "conso", "prix", "prixtotal"}, new int[]{R.id.kmlistrow_date, R.id.kmlistrow_vol, R.id.kmlistrow_km, R.id.kmlistrow_conso, R.id.kmlistrow_prix, R.id.kmlistrow_prixtotal});
            list.setAdapter((ListAdapter) this.mConsoListAdapter);
        } else {
            this.mConsoListAdapter.notifyDataSetChanged();
            list.invalidate();
        }
        if (offset != list.getCount()) {
            list.setOnScrollListener(new ViewVoitureOnScrollPlein(this));
        }
        list.setOnItemLongClickListener(new ViewVoitureOnLongClickPlein(this.mContext, this.mVoiture.id));
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        if (v.getId() == R.id.view_voiture_ajouter) {
            Message.EssenceBox(this.mContext, this, "0");
            return;
        }
        if (v.getId() == R.id.essencebox_ok) {
            try {
                double km = Double.valueOf(((TextView) Message.dialogInProgress.findViewById(R.id.essencebox_km)).getText().toString().replace(",", ".")).doubleValue();
                double litre = Double.valueOf(((TextView) Message.dialogInProgress.findViewById(R.id.essencebox_litre)).getText().toString().replace(",", ".")).doubleValue();
                double prix = Double.valueOf(((TextView) Message.dialogInProgress.findViewById(R.id.essencebox_prix)).getText().toString().replace(",", ".")).doubleValue();
                String date = ((TextView) Message.dialogInProgress.findViewById(R.id.essencebox_date)).getText().toString();
                boolean isPartiel = ((CheckBox) Message.dialogInProgress.findViewById(R.id.essence_box_partiel)).isChecked();
                boolean isRelatif = ((CheckBox) Message.dialogInProgress.findViewById(R.id.essencebox_kmrelatif)).isChecked();
                Voiture.newPlein(this.mContext, km, litre, prix, date, this.mVoiture.id, isPartiel, isRelatif);
                initConsoList(false, 0);
                this.mListener.OnDataChange();
            } catch (NumberFormatException e) {
            }
            if (Message.dialogInProgress.isShowing()) {
                Message.dialogInProgress.cancel();
                return;
            }
            return;
        }
        if (v.getId() != R.id.essencebox_annuler || !Message.dialogInProgress.isShowing()) {
            return;
        }
        Message.dialogInProgress.cancel();
    }

    @Override // com.consobox.screen.voitures.viewvoiture.OnVisible
    public void onVisible() {
        initConsoList(false, 0);
    }
}
