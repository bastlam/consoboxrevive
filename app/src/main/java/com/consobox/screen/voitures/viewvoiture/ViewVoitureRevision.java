package com.consobox.screen.voitures.viewvoiture;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.consobox.R;
import com.consobox.data.Utils;
import com.consobox.models.Voiture;
import com.consobox.screen.voitures.Revision;
import com.consobox.screen.voitures.adapters.RevisionAdapter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/* JADX INFO: loaded from: classes.dex */
public class ViewVoitureRevision extends LinearLayout implements OnVisible, AdapterView.OnItemClickListener {
    private Context mContext;
    private DisplayMetrics mDisplayMetrics;
    private Activity mParent;
    private Voiture mVoiture;

    public ViewVoitureRevision(Context context, Voiture voiture, DisplayMetrics metrics, Activity parent) {
        super(context);
        this.mVoiture = voiture;
        this.mContext = context;
        this.mDisplayMetrics = metrics;
        this.mParent = parent;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService("layout_inflater");
        inflater.inflate(R.layout.layout_viewvoiturerevision, this);
    }

    public void initRevisions() {
        SQLiteDatabase sql = Utils.getDatabase(this.mContext);
        int[] order = {-1, -2, -3, -4, -5, 0};
        LinearLayout tab = (LinearLayout) findViewById(R.id.viewvoiture_contentrevision);
        tab.removeAllViews();
        ListView lv = null;
        int nbItems = 0;
        ArrayList<HashMap<String, Object>> mylist = null;
        ArrayList<Integer> toDo = new ArrayList<>();
        int listId = 0;
        int carburant = getIntFromCarburant(this.mVoiture.type);
        float km = this.mVoiture.km;
        for (int j : order) {
            if (j == 0 || j < -1) {
                listId = 0;
                RevisionAdapter listAdapter = new RevisionAdapter(this.mContext, mylist, R.layout.row_revisionlist, new String[]{"nom", "text", "icon"}, new int[]{R.id.revisionlistrow_nom, R.id.revisionlistrow_temps, R.id.revisionlistrow_icon});
                lv.setAdapter((ListAdapter) listAdapter);
                listAdapter.addRevisionToDo(toDo);
                toDo = new ArrayList<>();
                lv.setLayoutParams(new LinearLayout.LayoutParams(-1, (int) (((nbItems * 50) + 5) * this.mDisplayMetrics.density)));
                tab.addView(lv);
            }
            if (j < 0) {
                int categorie = -j;
                TextView t = new TextView(this.mContext);
                t.setText(getResources().getStringArray(R.array.parties)[(-j) - 1]);
                t.setTextColor(getResources().getColor(android.R.color.primary_text_dark));
                t.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.title_bar));
                t.setGravity(3);
                t.setTypeface(null, 1);
                tab.addView(t);
                lv = new ListView(this.mContext);
                lv.setOnItemClickListener(this);
                mylist = new ArrayList<>();
                nbItems = 0;
                Cursor cur = sql.query("list_of_revision", new String[]{"id", "nom"}, "categorie=" + categorie + " AND carburant=" + carburant + " AND visible=1", null, null, null, null);
                cur.moveToFirst();
                for (int k = 0; k < cur.getCount(); k++) {
                    Cursor c = sql.query("revisions", new String[]{"valide", "dateLast", "kmLast", "freqKm", "freqDate"}, "voiture=" + this.mVoiture.id + " AND type=" + cur.getString(cur.getColumnIndex("id")), null, null, null, null);
                    nbItems++;
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("id", cur.getString(cur.getColumnIndex("id")));
                    if (c.getCount() == 0) {
                        map.put("text", getResources().getString(R.string.clickpouractiver));
                    } else {
                        c.moveToFirst();
                        if (c.getInt(c.getColumnIndex("valide")) == 1) {
                            String txt = null;
                            if (c.getInt(c.getColumnIndex("freqDate")) != 0) {
                                txt = String.valueOf(getResources().getString(R.string.tousles)) + c.getInt(c.getColumnIndex("freqDate")) + "Mois";
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);
                                try {
                                    Date date = sdf.parse(c.getString(c.getColumnIndex("dateLast")));
                                    long sub = (long) ((new Date().getTime() - (date.getTime() + ((long) date.getTimezoneOffset()))) / 2.6352E9d);
                                    if (sub >= c.getInt(c.getColumnIndex("freqDate")) && sub > 0 && c.getInt(c.getColumnIndex("freqDate")) != 0) {
                                        toDo.add(Integer.valueOf(listId));
                                    }
                                } catch (ParseException e) {
                                }
                            }
                            if (c.getInt(c.getColumnIndex("freqKm")) != 0) {
                                if (txt != null) {
                                    txt = String.valueOf(txt) + getResources().getString(R.string.ou) + c.getInt(c.getColumnIndex("freqKm")) + "Km";
                                } else {
                                    txt = String.valueOf(getResources().getString(R.string.tousles)) + c.getInt(c.getColumnIndex("freqKm")) + "Km";
                                }
                                float kmtmp = km - c.getInt(c.getColumnIndex("kmLast"));
                                if (c.getInt(c.getColumnIndex("kmLast")) > 0 && kmtmp >= c.getInt(c.getColumnIndex("freqKm")) && kmtmp > 0.0f && c.getInt(c.getColumnIndex("freqKm")) != 0) {
                                    toDo.add(Integer.valueOf(listId));
                                }
                            }
                            map.put("text", txt);
                        } else {
                            map.put("text", getResources().getString(R.string.clickpouractiver));
                        }
                    }
                    c.close();
                    listId++;
                    switch (categorie) {
                        case 1:
                            map.put("icon", Integer.valueOf(R.drawable.pipe));
                            break;
                        case 2:
                            map.put("icon", Integer.valueOf(R.drawable.vidange));
                            break;
                        case 3:
                            map.put("icon", Integer.valueOf(R.drawable.lampe));
                            break;
                        case 4:
                            map.put("icon", Integer.valueOf(R.drawable.frein));
                            break;
                        case 5:
                            map.put("icon", Integer.valueOf(R.drawable.divers));
                            break;
                    }
                    map.put("nom", cur.getString(cur.getColumnIndex("nom")));
                    mylist.add(map);
                    if (cur.isLast()) {
                        cur.close();
                    } else {
                        cur.moveToNext();
                    }
                }
            }
        }
        Utils.closeDatabase(this.mContext, sql);
    }

    private int getIntFromCarburant(String carbu) {
        String[] values = this.mContext.getResources().getStringArray(R.array.type_of_vehicule);
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(carbu)) {
                return i;
            }
        }
        return 0;
    }

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HashMap<String, String> hashMap = (HashMap) ((SimpleAdapter) parent.getAdapter()).getItem(position);
        Intent intent = new Intent(this.mParent, (Class<?>) Revision.class);
        intent.putExtra("voitureid", this.mVoiture.id);
        intent.putExtra("type", hashMap.get("id"));
        intent.putExtra("nom", hashMap.get("nom"));
        intent.putExtra("typeofcar", getIntFromCarburant(this.mVoiture.type));
        this.mParent.startActivityForResult(intent, 20);
    }

    @Override // com.consobox.screen.voitures.viewvoiture.OnVisible
    public void onVisible() {
        initRevisions();
    }
}
