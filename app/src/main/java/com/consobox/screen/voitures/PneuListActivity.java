package com.consobox.screen.voitures;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.consobox.R;
import com.consobox.data.Message;
import com.consobox.data.Utils;
import com.consobox.models.Pneu;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes.dex */
public class PneuListActivity extends ListActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, View.OnClickListener {
    public static int GET_PNEU_ID = 1;
    private int mCurrentPosition;
    private boolean mIsSelectPneu = false;
    private ArrayList<Pneu> mPneus;
    private int mVoitureId;

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mVoitureId = getIntent().getExtras().getInt("voitureid");
        if (getIntent().hasExtra("requestCode")) {
            if (getIntent().getExtras().getInt("requestCode") == GET_PNEU_ID) {
                getListView().setOnItemClickListener(this);
                this.mIsSelectPneu = true;
            }
        } else {
            getListView().setOnItemLongClickListener(this);
        }
        initListView();
    }

    private void initListView() {
        this.mPneus = new ArrayList<>();
        SQLiteDatabase sql = Utils.getDatabase(this);
        Cursor c = sql.query("pneu", new String[]{"*"}, "voiture=" + this.mVoitureId, null, null, null, "usable DESC");
        if (c.getCount() != 0) {
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                Pneu p = new Pneu(c);
                if ((!this.mIsSelectPneu || p.usable) && (!this.mIsSelectPneu || p.used == Pneu.NON_MONTER)) {
                    this.mPneus.add(p);
                }
            }
            getListView().setAdapter((ListAdapter) new PneuListAdapter(this.mPneus));
            c.close();
            Utils.closeDatabase(this, sql);
        }
    }

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(AdapterView<?> aViewAdapter, View aView, int aPosition, long aId) {
        Intent response = new Intent();
        response.putExtra("pneuId", aId);
        setResult(-1, response);
        finish();
    }

    @Override // android.widget.AdapterView.OnItemLongClickListener
    public boolean onItemLongClick(AdapterView<?> aAdapter, View aView, final int aPosition, long aId) {
        CharSequence[] items = {"Editer", "Supprimer"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Action");
        builder.setItems(items, new DialogInterface.OnClickListener() { // from class: com.consobox.screen.voitures.PneuListActivity.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int item) {
                dialog.cancel();
                if (item == 0) {
                    PneuListActivity.this.mCurrentPosition = aPosition;
                    Message.PneuBox(PneuListActivity.this, PneuListActivity.this, null, true, (Pneu) PneuListActivity.this.mPneus.get(aPosition));
                } else if (item == 1) {
                    PneuListActivity.this.mCurrentPosition = aPosition;
                    Message.YesNoMessage("Etes-vous sur de vouloir supprimer ce pneu ?", PneuListActivity.this, PneuListActivity.this);
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
        return false;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ok) {
                String date = ((TextView) Message.dialogInProgress.findViewById(R.id.date)).getText().toString();
                String marque = ((TextView) Message.dialogInProgress.findViewById(R.id.marque)).getText().toString();
                String modele = ((TextView) Message.dialogInProgress.findViewById(R.id.modele)).getText().toString();
                String description = ((TextView) Message.dialogInProgress.findViewById(R.id.description)).getText().toString();
                float prix = Float.parseFloat(((TextView) Message.dialogInProgress.findViewById(R.id.prix)).getText().toString());
                boolean isHiver = ((CheckBox) Message.dialogInProgress.findViewById(R.id.hiver)).isChecked();
                Pneu pneu = this.mPneus.get(this.mCurrentPosition);
                pneu.dateAchat = date;
                pneu.marque = marque;
                pneu.modele = modele;
                pneu.description = description;
                pneu.prix = prix;
                pneu.isHiver = isHiver;
                ContentValues values = pneu.getDataForInsert();
                SQLiteDatabase sql = Utils.getDatabase(this);
                sql.update("pneu", values, "id=" + pneu.id, null);
                ContentValues values2 = new ContentValues();
                values2.put("prix", Float.valueOf(prix));
                sql.update("depenses", values2, "type=1 AND description='" + (pneu.id + 20) + "'", null);
                Utils.closeDatabase(this, sql);
                initListView();
        }
        if (id == R.id.ok || id == R.id.annuler) {
                if (Message.dialogInProgress.isShowing()) {
                    Message.dialogInProgress.cancel();
                }
        }
        if (id == R.id.YesNoDetele_yes) {
                this.mPneus.get(this.mCurrentPosition).delete(this);
                initListView();
        }
        if (id == R.id.YesNoDetele_yes || id == R.id.YesNoDetele_no) {
                if (Message.dialogInProgress.isShowing()) {
                    Message.dialogInProgress.cancel();
                }
        }
    }

    private class PneuListAdapter extends BaseAdapter {
        ArrayList<Pneu> mList;
        ArrayList<View> mViews = new ArrayList<>();

        public PneuListAdapter(ArrayList<Pneu> list) {
            this.mList = list;
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return this.mList.size();
        }

        @Override // android.widget.Adapter
        public Object getItem(int position) {
            if (position >= this.mList.size()) {
                return null;
            }
            return this.mList.get(position);
        }

        @Override // android.widget.Adapter
        public long getItemId(int position) {
            return Integer.valueOf(this.mList.get(position).id).intValue();
        }

        @Override // android.widget.Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService("layout_inflater");
            View view = inflater.inflate(R.layout.row_pneulist, (ViewGroup) null);
            Pneu pneu = this.mList.get(position);
            ((TextView) view.findViewById(R.id.nom)).setText(String.valueOf(pneu.marque) + " " + pneu.modele + " - " + (pneu.isHiver ? "Hiver" : "Eté"));
            ((TextView) view.findViewById(R.id.description)).setText(pneu.description);
            double kmused = pneu.kmUtilisation;
            if (pneu.usable && pneu.used != Pneu.NON_MONTER) {
                kmused += Utils.getKmSinceDate(PneuListActivity.this, pneu.dateMontage, PneuListActivity.this.mVoitureId);
            }
            if (pneu.used == Pneu.ARRIERE) {
                ((TextView) view.findViewById(R.id.state)).setText("AR");
            } else if (pneu.used == Pneu.AVANT) {
                ((TextView) view.findViewById(R.id.state)).setText("AV");
            } else {
                ((TextView) view.findViewById(R.id.state)).setText("");
            }
            if (!pneu.usable) {
                view.setEnabled(false);
                ((TextView) view.findViewById(R.id.state)).setText("HS");
            }
            ((TextView) view.findViewById(R.id.kmused)).setText(String.valueOf(kmused) + "km");
            return view;
        }
    }
}
