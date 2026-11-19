package com.consobox.screen.voitures;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.consobox.R;
import com.consobox.data.Message;
import com.consobox.data.Utils;
import com.consobox.models.Voiture;
import com.consobox.screen.voitures.adapters.ListeVoitureAdapter;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes.dex */
public class ListeVoiture extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private ListeVoitureAdapter mAdapter;

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_listevoiture);
        setTitle(getResources().getString(R.string.listevoiture));
        ListView listview = (ListView) findViewById(R.id.list);
        this.mAdapter = new ListeVoitureAdapter(this);
        listview.setAdapter((ListAdapter) this.mAdapter);
        initVoitures();
        listview.setOnItemClickListener(this);
        listview.setOnItemLongClickListener(this);
    }

    @Override // android.app.Activity
    public void onDestroy() {
        super.onDestroy();
    }

    @Override // android.app.Activity
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override // android.app.Activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        clearLayout();
        ListView listview = (ListView) findViewById(R.id.list);
        this.mAdapter = new ListeVoitureAdapter(this);
        listview.setAdapter((ListAdapter) this.mAdapter);
        initVoitures();
    }

    private void clearLayout() {
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initVoitures() {
        SQLiteDatabase sql = Utils.getDatabase(this);
        sql.execSQL(getResources().getString(R.string.DB_VOITURE));
        ArrayList<Voiture> mylist = new ArrayList<>();
        Cursor c = sql.query("voiture", new String[]{"*"}, null, null, null, null, null);
        c.moveToFirst();
        for (int i = 0; i < c.getCount(); i++) {
            Voiture v = new Voiture(c);
            mylist.add(v);
            c.moveToNext();
        }
        c.close();
        Utils.closeDatabase(this, sql);
        this.mAdapter.setItems(mylist);
        this.mAdapter.notifyDataSetChanged();
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(this, (Class<?>) AjouterVoiture.class);
        startActivityForResult(intent, 20);
        return true;
    }

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (this.mAdapter.getItemId(arg2) == -1) {
            startActivityForResult(new Intent(this, (Class<?>) AjouterVoiture.class), 20);
            return;
        }
        Intent intent = new Intent(this, (Class<?>) ViewVoiturePager.class);
        intent.putExtra("voitureid", (int) this.mAdapter.getItemId(arg2));
        startActivityForResult(intent, 20);
    }

    /* JADX WARN: Type inference failed for: r4v8, types: [com.consobox.screen.voitures.ListeVoiture$1] */
    @Override // android.widget.AdapterView.OnItemLongClickListener
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        CharSequence[] items = {getResources().getString(R.string.afficher), getResources().getString(R.string.modifier), getResources().getString(R.string.supprimer)};
        int voitureId = (int) this.mAdapter.getItemId(arg2);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Action");
        builder.setItems(items, new DialogInterface.OnClickListener() { // from class: com.consobox.screen.voitures.ListeVoiture.1
            int voitureId;

            public DialogInterface.OnClickListener set(int id) {
                this.voitureId = id;
                return this;
            }

            /* JADX WARN: Type inference failed for: r3v1, types: [com.consobox.screen.voitures.ListeVoiture$1$1] */
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    Intent intent = new Intent(ListeVoiture.this, (Class<?>) ViewVoiturePager.class);
                    intent.putExtra("voitureid", this.voitureId);
                    ListeVoiture.this.startActivityForResult(intent, 20);
                } else if (item == 1) {
                    Intent intent2 = new Intent(ListeVoiture.this, (Class<?>) AjouterVoiture.class);
                    intent2.putExtra("voitureamodifier", this.voitureId);
                    ListeVoiture.this.startActivityForResult(intent2, 20);
                } else if (item == 2) {
                    Message.YesNoMessage(ListeVoiture.this.getResources().getString(R.string.supprimervoiture), ListeVoiture.this, new View.OnClickListener() { // from class: com.consobox.screen.voitures.ListeVoiture.1.1
                        int idVoiture;

                        public View.OnClickListener set(int id) {
                            this.idVoiture = id;
                            return this;
                        }

                        @Override // android.view.View.OnClickListener
                        public void onClick(View v) {
                            if (v.getId() == R.id.YesNoDetele_yes) {
                                SQLiteDatabase sql = Utils.getDatabase(ListeVoiture.this);
                                sql.execSQL("DELETE FROM voiture WHERE id=" + this.idVoiture);
                                sql.execSQL("DELETE FROM conso WHERE voiture=" + this.idVoiture);
                                sql.execSQL("DELETE FROM depenses WHERE voiture=" + this.idVoiture);
                                Utils.closeDatabase(ListeVoiture.this, sql);
                                ListeVoiture.this.initVoitures();
                            }
                            if (Message.dialogInProgress.isShowing()) {
                                Message.dialogInProgress.cancel();
                            }
                            ListeVoiture.this.onActivityResult(0, 0, null);
                        }
                    }.set(Integer.valueOf(this.voitureId).intValue()));
                }
            }
        }.set(voitureId));
        AlertDialog alert = builder.create();
        alert.show();
        return true;
    }
}
