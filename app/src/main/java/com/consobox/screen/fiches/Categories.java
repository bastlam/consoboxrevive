package com.consobox.screen.fiches;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.consobox.R;
import com.consobox.data.Message;
import com.consobox.data.Network;
import com.consobox.data.Utils;
import java.io.File;

/* JADX INFO: loaded from: classes.dex */
public class Categories extends ListActivity {
    String path = "";

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (false) {
            Dialog d = Message.ErrorMessage(getResources().getString(R.string.nosdcard), this);
            d.setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: com.consobox.screen.fiches.Categories.1
                @Override // android.content.DialogInterface.OnCancelListener
                public void onCancel(DialogInterface dialog) {
                    Categories.this.finish();
                }
            });
            return;
        }
        try {
            Utils.getAppDir(this);
            if (new File(Utils.fichesPath()).exists()) {
                verifMAJ(false);
                initCategories();
            } else {
                verifMAJ(true);
            }
        } catch (Exception e) {
            Dialog d2 = Message.ErrorMessage(getResources().getString(R.string.nosdcard), this);
            d2.setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: com.consobox.screen.fiches.Categories.2
                @Override // android.content.DialogInterface.OnCancelListener
                public void onCancel(DialogInterface dialog) {
                    Categories.this.finish();
                }
            });
        }
    }

    /* JADX WARN: Type inference failed for: r2v3, types: [com.consobox.screen.fiches.Categories$3] */
    /* JADX WARN: Type inference failed for: r2v4, types: [com.consobox.screen.fiches.Categories$4] */
    private void verifMAJ(boolean first) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getResources().getString(R.string.versiondonnee));
        dialog.setIndeterminate(true);
        if (first) {
            dialog.show();
        }
        Handler ha = new Handler() { // from class: com.consobox.screen.fiches.Categories.3
            ProgressDialog d;

            public Handler setDialog(ProgressDialog d) {
                this.d = d;
                return this;
            }

            @Override // android.os.Handler
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 4) {
                    Dialog d = Message.ErrorMessage(Categories.this.getResources().getString(R.string.nonetwork), Categories.this);
                    d.setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: com.consobox.screen.fiches.Categories.3.1
                        @Override // android.content.DialogInterface.OnCancelListener
                        public void onCancel(DialogInterface dialog2) {
                            Categories.this.finish();
                        }
                    });
                    d.show();
                } else {
                    if (msg.what == 3) {
                        this.d.setMessage(Categories.this.getResources().getString(R.string.miseajourbdd));
                        this.d.show();
                        return;
                    }
                    if (this.d != null) {
                        this.d.cancel();
                    }
                    if (msg.what != 1 || new File(Utils.fichesPath()).exists()) {
                        if (msg.what == 2) {
                            Categories.this.path = "";
                            Categories.this.initCategories();
                            return;
                        }
                        return;
                    }
                    Categories.this.finish();
                }
            }
        }.setDialog(dialog);
        new Thread() { // from class: com.consobox.screen.fiches.Categories.4
            Dialog dialog;
            Handler h;
            boolean mFirst;

            public Thread setParam(Handler h, boolean first2, Dialog dialog2) {
                this.h = h;
                this.dialog = dialog2;
                this.mFirst = first2;
                return this;
            }

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                if (new File(Utils.fichesPath()).exists()) {
                    SQLiteDatabase bdd = SQLiteDatabase.openDatabase(Utils.fichesPath(), null, 0);
                    try {
                        this.mFirst = true;
                        Cursor c = bdd.query("info", new String[]{"version"}, null, null, null, null, null);
                        if (c.getCount() == 0) {
                            performMAJ();
                        }
                        c.moveToFirst();
                        int versionMobile = c.getInt(c.getColumnIndex("version"));
                        c.close();
                        bdd.close();
                        String rep = Network.makeRequest("http://www.olly.free.fr/consobox/fiches/version.txt");
                        if (rep.indexOf("[") == -1) {
                            this.h.sendEmptyMessage(1);
                            return;
                        } else {
                            int versionCloud = Integer.valueOf(rep.substring(rep.indexOf("[") + 1, rep.indexOf("]"))).intValue();
                            if (versionCloud > versionMobile) {
                                performMAJ();
                            }
                        }
                    } catch (Exception e) {
                        performMAJ();
                        return;
                    }
                } else {
                    performMAJ();
                }
                this.h.sendEmptyMessage(0);
            }

            private void performMAJ() {
                this.h.sendEmptyMessage(3);
                File f = new File(Utils.fichesPath());
                boolean res = Network.makeRequest("http://www.olly.free.fr/consobox/fiches/fiches", f);
                if (!res && this.mFirst) {
                    this.dialog.cancel();
                    this.h.sendEmptyMessage(4);
                } else {
                    this.h.sendEmptyMessage(2);
                }
            }
        }.setParam(ha, first, dialog).start();
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4 || event.getAction() != 0) {
            return false;
        }
        if (this.path.equals("")) {
            finish();
            return true;
        }
        int pos = this.path.lastIndexOf(".");
        if (pos == -1) {
            this.path = "";
        } else {
            this.path = this.path.substring(0, pos);
        }
        goTo();
        return true;
    }

    @Override // android.app.ListActivity
    protected void onListItemClick(ListView l, View v, int position, long id) {
        this.path = String.valueOf(this.path) + (this.path.equals("") ? "" : ".") + l.getAdapter().getItem(position);
        goTo();
    }

    private void goTo() {
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(this, R.anim.cat_anim_out);
        getListView().setLayoutAnimation(controller);
        SQLiteDatabase bdd = SQLiteDatabase.openDatabase(Utils.fichesPath(), null, 0);
        Cursor c = bdd.query("fiches", new String[]{"contenu"}, "nom=\"" + (this.path.equals("") ? "root" : this.path).replace(" ", "_") + "\"", null, null, null, null);
        if (c.getCount() == 0) {
            int pos = this.path.lastIndexOf(".");
            if (pos != -1) {
                this.path = this.path.substring(0, pos);
                return;
            } else {
                this.path = "";
                return;
            }
        }
        c.moveToFirst();
        String[] cat = c.getString(c.getColumnIndex("contenu")).split(";");
        c.close();
        bdd.close();
        if (cat.length > 1) {
            if (cat[0].indexOf("liste") >= 0) {
                String[] affiche = new String[cat.length - 1];
                System.arraycopy(cat, 1, affiche, 0, cat.length - 1);
                setListAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, affiche));
            } else if (cat[0].indexOf("fiche") >= 0) {
                Intent intent = new Intent(this, (Class<?>) FicheView.class);
                intent.putExtra("text", cat[1]);
                startActivity(intent);
                int pos2 = this.path.lastIndexOf(".");
                if (pos2 == -1) {
                    this.path = "";
                } else {
                    this.path = this.path.substring(0, pos2);
                }
            }
            LayoutAnimationController controller2 = AnimationUtils.loadLayoutAnimation(this, R.anim.cat_anim_in);
            getListView().setLayoutAnimation(controller2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initCategories() {
        try {
            SQLiteDatabase bdd = SQLiteDatabase.openDatabase(Utils.fichesPath(), null, 0);
            Cursor c = bdd.query("fiches", new String[]{"contenu"}, "nom=\"root\"", null, null, null, null);
            if (c.getCount() != 0) {
                c.moveToFirst();
                String[] cat = c.getString(c.getColumnIndex("contenu")).split(";");
                if (cat[0].equals("liste")) {
                    String[] affiche = new String[cat.length - 1];
                    System.arraycopy(cat, 1, affiche, 0, cat.length - 1);
                    setListAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, affiche));
                }
                c.close();
                bdd.close();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Une erreur est survenue", 0);
            finish();
        }
    }
}
