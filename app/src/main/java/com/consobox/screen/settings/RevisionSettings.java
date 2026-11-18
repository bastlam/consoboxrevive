package com.consobox.screen.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.consobox.R;
import com.consobox.data.Utils;
import com.consobox.screen.voitures.adapters.RevisionAdapter;
import java.util.ArrayList;
import java.util.HashMap;

/* JADX INFO: loaded from: classes.dex */
public class RevisionSettings extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    LinearLayout layout;

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScrollView s = new ScrollView(this);
        this.layout = new LinearLayout(this);
        this.layout.setOrientation(1);
        s.addView(this.layout, new LinearLayout.LayoutParams(-1, -1));
        addContentView(s, new FrameLayout.LayoutParams(-1, -1));
        initList();
    }

    public void initList() {
        int[] order = {-1, -2, -3, -4, -5};
        this.layout.removeAllViews();
        for (int i : order) {
            int categorie = -i;
            int nbItems = 0;
            TextView t = new TextView(this);
            t.setText(getResources().getStringArray(R.array.parties)[categorie - 1]);
            t.setTextColor(getResources().getColor(android.R.color.primary_text_dark));
            t.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.title_bar));
            t.setGravity(3);
            t.setTypeface(null, 1);
            this.layout.addView(t);
            ArrayList<HashMap<String, Object>> mylist = new ArrayList<>();
            ListView lv = new ListView(this);
            lv.setOnItemClickListener(this);
            lv.setOnItemLongClickListener(this);
            SQLiteDatabase sql = Utils.getDatabase(this);
            Cursor cur = sql.query("list_of_revision", new String[]{"id", "nom", "visible", "supprimable"}, "categorie=" + categorie, null, "id", null, null);
            cur.moveToFirst();
            for (int k = 0; k < cur.getCount(); k++) {
                nbItems++;
                HashMap<String, Object> map = new HashMap<>();
                map.put("id", cur.getString(cur.getColumnIndex("id")));
                map.put("visible", cur.getString(cur.getColumnIndex("visible")));
                map.put("supprimable", cur.getString(cur.getColumnIndex("supprimable")));
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
                cur.moveToNext();
            }
            cur.close();
            Utils.closeDatabase(this, sql);
            HashMap<String, Object> map2 = new HashMap<>();
            map2.put("id", new StringBuilder().append(-categorie).toString());
            map2.put("icon", Integer.valueOf(R.drawable.plus));
            map2.put("nom", getResources().getString(R.string.addrevision));
            mylist.add(map2);
            RevisionAdapter listAdapter = new RevisionAdapter(this, mylist, R.layout.row_revisionlist, new String[]{"nom", "text", "icon"}, new int[]{R.id.revisionlistrow_nom, R.id.revisionlistrow_temps, R.id.revisionlistrow_icon});
            lv.setAdapter((ListAdapter) listAdapter);
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            lv.setLayoutParams(new LinearLayout.LayoutParams(-1, (int) ((((nbItems + 1) * 50) + 5) * dm.density)));
            this.layout.addView(lv);
        }
    }

    @Override // android.app.Activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        initList();
    }

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HashMap<String, String> hashMap = (HashMap) ((SimpleAdapter) parent.getAdapter()).getItem(position);
        int type = Integer.valueOf(hashMap.get("id")).intValue();
        Intent intent = new Intent(this, (Class<?>) ModifRevision.class);
        if (type < 0) {
            intent.putExtra("categorie", -type);
        } else {
            if (hashMap.get("supprimable").equals("0")) {
                Toast.makeText(this, getResources().getString(R.string.nonmodifiable), 0).show();
                return;
            }
            intent.putExtra("type", type);
        }
        startActivityForResult(intent, 20);
    }

    /* JADX WARN: Type inference failed for: r7v6, types: [com.consobox.screen.settings.RevisionSettings$2] */
    @Override // android.widget.AdapterView.OnItemLongClickListener
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        CharSequence[] items;
        HashMap<String, String> hashMap = (HashMap) ((SimpleAdapter) parent.getAdapter()).getItem(position);
        int type = Integer.valueOf(hashMap.get("id")).intValue();
        if (type > 0) {
            if (hashMap.get("supprimable").equals("0")) {
                items = new CharSequence[1];
                items[0] = String.valueOf(getResources().getString(R.string.rendre)) + (hashMap.get("visible").equals("1") ? "in" : "") + "visible";
            } else {
                items = new CharSequence[3];
                items[0] = String.valueOf(getResources().getString(R.string.rendre)) + (hashMap.get("visible").equals("1") ? "in" : "") + "visible";
                items[1] = getResources().getString(R.string.supprimer);
                items[2] = getResources().getString(R.string.modifier);
            }
            Handler reinit = new Handler() { // from class: com.consobox.screen.settings.RevisionSettings.1
                @Override // android.os.Handler
                public void handleMessage(Message msg) {
                    RevisionSettings.this.initList();
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Action");
            builder.setItems(items, new DialogInterface.OnClickListener() { // from class: com.consobox.screen.settings.RevisionSettings.2
                Handler mReinit;
                private int mType = 0;
                private int mVisible = 0;

                public DialogInterface.OnClickListener setParams(int type2, int visible, Handler reinit2) {
                    this.mType = type2;
                    this.mVisible = visible;
                    this.mReinit = reinit2;
                    return this;
                }

                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int item) {
                    switch (item) {
                        case 0:
                            SQLiteDatabase sql1 = Utils.getDatabase(RevisionSettings.this);
                            sql1.execSQL("UPDATE list_of_revision SET visible=" + Math.abs(this.mVisible - 1) + " WHERE id=" + this.mType);
                            Utils.closeDatabase(RevisionSettings.this, sql1);
                            this.mReinit.sendEmptyMessage(0);
                            break;
                        case 1:
                            SQLiteDatabase sql2 = Utils.getDatabase(RevisionSettings.this);
                            sql2.execSQL("DELETE FROM list_of_revision WHERE id=" + this.mType);
                            sql2.execSQL("DELETE FROM revisions WHERE type=" + this.mType);
                            Utils.closeDatabase(RevisionSettings.this, sql2);
                            this.mReinit.sendEmptyMessage(0);
                            break;
                        case 2:
                            Intent intent = new Intent(RevisionSettings.this, (Class<?>) ModifRevision.class);
                            intent.putExtra("type", this.mType);
                            RevisionSettings.this.startActivityForResult(intent, 20);
                            break;
                    }
                }
            }.setParams(type, Integer.valueOf(hashMap.get("visible")).intValue(), reinit));
            AlertDialog alert = builder.create();
            alert.show();
            return true;
        }
        return true;
    }
}
