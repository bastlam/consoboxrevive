package com.consobox.screen.voitures.viewvoiture;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.consobox.R;
import com.consobox.data.Message;
import com.consobox.data.Utils;
import com.consobox.models.Voiture;
import com.consobox.screen.voitures.adapters.VoitureContentAdapter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/* JADX INFO: loaded from: classes.dex */
public class ViewVoitureContent extends LinearLayout implements OnVisible, View.OnClickListener {
    private Context mContext;
    private OnDataChangeListener mListener;
    private Voiture mVoiture;

    public ViewVoitureContent(Context context, Voiture voiture, OnDataChangeListener listener) {
        super(context);
        this.mVoiture = voiture;
        this.mContext = context;
        this.mListener = listener;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService("layout_inflater");
        inflater.inflate(R.layout.layout_viewvoiturecontent, this);
    }

    public void init() {
        ListView lv = (ListView) findViewById(R.id.view_voiture_listdetails);
        ArrayList<HashMap<String, Object>> mylist = new ArrayList<>();
        SimpleAdapter listAdapter = new VoitureContentAdapter(this.mContext, mylist, R.layout.row_revisionlist, new String[]{"nom", "text", "icon", "moyenne"}, new int[]{R.id.revisionlistrow_nom, R.id.revisionlistrow_temps, R.id.revisionlistrow_icon, R.id.revisionlistrow_average});
        HashMap<String, Object> map = new HashMap<>();
        map.put("nom", getResources().getString(R.string.kilometrage));
        map.put("text", String.valueOf(this.mVoiture.km) + "km");
        map.put("icon", Integer.valueOf(R.drawable.borne));
        mylist.add(map);
        HashMap<String, Object> map2 = new HashMap<>();
        map2.put("nom", getResources().getString(R.string.typedecarburant));
        map2.put("text", this.mVoiture.type);
        map2.put("icon", Integer.valueOf(R.drawable.tanker));
        mylist.add(map2);
        Double res = Utils.getConsoMoyenne(this.mContext, this.mVoiture.id);
        DecimalFormat df = new DecimalFormat("##.##");
        HashMap<String, Object> map3 = new HashMap<>();
        map3.put("nom", getResources().getString(R.string.consomoyenne));
        map3.put("text", String.valueOf(df.format(res)) + " l/100Km");
        map3.put("icon", Integer.valueOf(R.drawable.leaf));
        mylist.add(map3);
        lv.setAdapter((ListAdapter) listAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.consobox.screen.voitures.viewvoiture.ViewVoitureContent.1
            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    SQLiteDatabase sql = Utils.getDatabase(ViewVoitureContent.this.mContext);
                    Cursor c = sql.query("voiture", new String[]{"nom", "km", "type"}, "id=" + ViewVoitureContent.this.mVoiture.id, null, null, null, null);
                    c.moveToFirst();
                    Message.ChangeDoubleDialog(ViewVoitureContent.this.getResources().getString(R.string.editkmage), ViewVoitureContent.this, ViewVoitureContent.this.mContext, Double.valueOf(c.getString(1)).doubleValue());
                    c.close();
                    Utils.closeDatabase(ViewVoitureContent.this.mContext, sql);
                }
            }
        });
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        if (v.getId() == R.id.ok) {
            try {
                EditKmage(Double.valueOf(((TextView) Message.dialogInProgress.findViewById(R.id.value)).getText().toString()).doubleValue());
                this.mListener.OnDataChange();
            } catch (NumberFormatException e) {
            }
            if (Message.dialogInProgress.isShowing()) {
                Message.dialogInProgress.cancel();
                return;
            }
            return;
        }
        if (v.getId() != R.id.annuler || !Message.dialogInProgress.isShowing()) {
            return;
        }
        Message.dialogInProgress.cancel();
    }

    private void EditKmage(double km) {
        if (km > 0.0d) {
            SQLiteDatabase sql = Utils.getDatabase(this.mContext);
            sql.execSQL("UPDATE voiture SET km=" + km + " WHERE id=" + this.mVoiture.id);
            Utils.closeDatabase(this.mContext, sql);
            init();
        }
    }

    @Override // com.consobox.screen.voitures.viewvoiture.OnVisible
    public void onVisible() {
        init();
    }
}
