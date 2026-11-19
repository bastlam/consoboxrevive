package com.consobox.screen.voitures.viewvoiture;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.consobox.R;
import com.consobox.data.Message;
import com.consobox.data.Utils;
import com.consobox.models.Voiture;
import com.consobox.screen.voitures.Listeners.ViewVoitureOnScrollDepenses;
import com.consobox.screen.voitures.adapters.DepensesAdapter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/* JADX INFO: loaded from: classes.dex */
public class ViewVoitureDepense extends LinearLayout implements OnVisible, ViewVoitureOnScrollDepenses.BackDepense, AdapterView.OnItemLongClickListener {
    private int depenseMonth;
    private int depenseYear;
    private Context mContext;
    private ArrayList<HashMap<String, String>> mDepensesList;
    private DepensesAdapter mDepensesListAdapter;
    private RelativeLayout mPinnedDepenseHeaderView;
    private ViewVoitureOnScrollDepenses mViewVoitureOnScrollDepenses;
    private Voiture mVoiture;
    private static HashMap<String, String> yearInProgress = null;
    private static HashMap<String, String> monthInProgress = null;

    public ViewVoitureDepense(Context context, Voiture voiture) {
        super(context);
        this.mVoiture = voiture;
        this.mContext = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService("layout_inflater");
        inflater.inflate(R.layout.layout_viewvoituredepenses, this);
    }

    @Override // com.consobox.screen.voitures.Listeners.ViewVoitureOnScrollDepenses.BackDepense
    public void initDepenses(boolean ajouterAExistant) {
        SQLiteDatabase sql = Utils.getDatabase(this.mContext);
        sql.execSQL(getResources().getString(R.string.DB_DEPENSES));
        ListView list = (ListView) findViewById(R.id.view_voiture_depenses);
        int offset = 0;
        if (!ajouterAExistant) {
            this.mDepensesList = new ArrayList<>();
            this.depenseYear = 0;
            this.depenseMonth = 0;
            this.mViewVoitureOnScrollDepenses = new ViewVoitureOnScrollDepenses(this);
            this.mViewVoitureOnScrollDepenses.isEnable = true;
            this.mPinnedDepenseHeaderView = (RelativeLayout) findViewById(R.id.depensesmonthpinned);
            this.mPinnedDepenseHeaderView.setVisibility(4);
        } else {
            offset = this.mDepensesListAdapter.getCountWithoutSections();
        }
        DecimalFormat df = new DecimalFormat("#.##");
        Cursor c = sql.query("depenses", new String[]{"id", "voiture", "type", "date", "km", "prix", "description"}, "voiture=" + this.mVoiture.id, null, null, null, "date DESC");
        c.moveToFirst();
        int i = offset;
        while (true) {
            if (i >= (offset + 15 > c.getCount() ? c.getCount() : offset + 15)) {
                break;
            }
            c.moveToPosition(i);
            HashMap<String, String> map = new HashMap<>();
            String[] date = c.getString(c.getColumnIndex("date")).split("-");
            if (Integer.valueOf(date[0]).intValue() != this.depenseYear) {
                map.put("id", "-1");
                map.put("type", date[0]);
                map.put("isYear", "true");
                map.put("isMonth", "false");
                map.put("originalDate", c.getString(c.getColumnIndex("date")));
                this.mDepensesList.add(map);
                this.depenseYear = Integer.valueOf(date[0]).intValue();
                yearInProgress = map;
                yearInProgress.put("prix", "0");
                map = new HashMap<>();
            }
            if (Integer.valueOf(date[1]).intValue() != this.depenseMonth) {
                map.put("id", "-1");
                map.put("type", getResources().getStringArray(R.array.mois)[Integer.valueOf(date[1]).intValue() - 1]);
                map.put("isYear", "false");
                map.put("isMonth", "true");
                map.put("originalDate", c.getString(c.getColumnIndex("date")));
                this.mDepensesList.add(map);
                this.depenseMonth = Integer.valueOf(date[1]).intValue();
                monthInProgress = map;
                monthInProgress.put("prix", "0");
                map = new HashMap<>();
            }
            String typeText = "";
            map.put("originalDate", c.getString(c.getColumnIndex("date")));
            int type = c.getInt(c.getColumnIndex("type"));
            if (type == 0) {
                typeText = getResources().getStringArray(R.array.type_depenses)[type];
                map.put("icon", Integer.toString(R.drawable.pompe));
            } else if (type == 2) {
                typeText = c.getString(c.getColumnIndex("description"));
                map.put("icon", Integer.toString(R.drawable.repair));
            } else if (type == 1) {
                int action = c.getInt(c.getColumnIndex("description"));
                if (action == 1) {
                    typeText = getResources().getString(R.string.montagepneu);
                } else if (action >= 20) {
                    typeText = getResources().getString(R.string.nouveaupneu);
                }
                map.put("icon", Integer.toString(R.drawable.pneu));
            } else if (type >= 10) {
                Cursor revision = sql.query("list_of_revision", new String[]{"nom"}, "id=" + (type - 10), null, null, null, null);
                revision.moveToFirst();
                typeText = revision.getString(revision.getColumnIndex("nom"));
                revision.close();
                map.put("icon", Integer.toString(R.drawable.divers));
            }
            map.put("date", "le " + date[2] + "/" + date[1] + "/" + date[0]);
            map.put("id", c.getString(c.getColumnIndex("id")));
            map.put("prix", String.valueOf(df.format(c.getDouble(c.getColumnIndex("prix")))) + Utils.getDeviseUnit(this.mContext));
            try {
                double tmp = Double.valueOf(yearInProgress.get("prix")).doubleValue();
                yearInProgress.put("prix", String.valueOf(c.getDouble(c.getColumnIndex("prix")) + tmp));
                double tmp2 = Double.valueOf(monthInProgress.get("prix")).doubleValue();
                monthInProgress.put("prix", String.valueOf(c.getDouble(c.getColumnIndex("prix")) + tmp2));
            } catch (Exception e) {
            }
            map.put("type", typeText);
            map.put("isYear", "false");
            map.put("isMonth", "false");
            this.mDepensesList.add(map);
            c.moveToNext();
            i++;
        }
        if (!ajouterAExistant) {
            this.mDepensesListAdapter = new DepensesAdapter(this.mDepensesList);
            list.setAdapter((ListAdapter) this.mDepensesListAdapter);
        } else {
            this.mDepensesListAdapter.notifyDataSetChanged();
            list.invalidate();
        }
        if (c.getCount() == this.mDepensesListAdapter.getCountWithoutSections()) {
            this.mViewVoitureOnScrollDepenses.isEnable = false;
        }
        if (c.getCount() != 0) {
            list.setOnScrollListener(this.mViewVoitureOnScrollDepenses);
        }
        c.close();
        Utils.closeDatabase(this.mContext, sql);
        list.setOnItemLongClickListener(this);
    }

    @Override // com.consobox.screen.voitures.Listeners.ViewVoitureOnScrollDepenses.BackDepense
    public void setDepensePinnedText(int first) {
        HashMap<String, String> item = (HashMap) this.mDepensesListAdapter.getItem(first);
        if (item != null) {
            if (first < 1) {
                this.mPinnedDepenseHeaderView.setVisibility(4);
                return;
            }
            String[] date = item.get("originalDate").split("-");
            this.mPinnedDepenseHeaderView.setVisibility(0);
            ((TextView) this.mPinnedDepenseHeaderView.findViewById(R.id.type)).setText(getResources().getStringArray(R.array.mois)[Integer.valueOf(date[1]).intValue() - 1]);
            ((TextView) this.mPinnedDepenseHeaderView.findViewById(R.id.prix)).setText(date[0]);
            this.mPinnedDepenseHeaderView.invalidate();
        }
    }

    /* JADX WARN: Type inference failed for: r2v0, types: [com.consobox.screen.voitures.viewvoiture.ViewVoitureDepense$1] */
    @Override // android.widget.AdapterView.OnItemLongClickListener
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (arg3 == -1) {
            return false;
        }
        Message.YesNoMessage(getResources().getString(R.string.suppdepense), this.mContext, new View.OnClickListener() { // from class: com.consobox.screen.voitures.viewvoiture.ViewVoitureDepense.1
            private long mId;

            public View.OnClickListener setId(long i) {
                this.mId = i;
                return this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (v.getId() == R.id.YesNoDetele_yes) {
                    SQLiteDatabase sql = Utils.getDatabase(ViewVoitureDepense.this.mContext);
                    sql.execSQL("DELETE FROM depenses WHERE id=" + this.mId);
                    Utils.closeDatabase(ViewVoitureDepense.this.mContext, sql);
                    ViewVoitureDepense.this.initDepenses(false);
                }
                Message.dialogInProgress.cancel();
            }
        }.setId(arg3));
        return true;
    }

    @Override // com.consobox.screen.voitures.viewvoiture.OnVisible
    public void onVisible() {
        initDepenses(false);
    }

}
