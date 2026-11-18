package com.consobox;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.consobox.data.Message;
import com.consobox.data.Utils;

/* JADX INFO: loaded from: classes.dex */
public class AccesRapideAdapter extends BaseAdapter {
    private Context mContext;
    protected SharedPreferences mPref;
    protected int mSavedEmplacement;
    protected int[] mSavedIds;
    private Integer[] mMenu = new Integer[3];
    private String[] mLabel = new String[3];
    private Integer[] mId = new Integer[3];
    private int count = 0;

    public AccesRapideAdapter(Context c) {
        init(c);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void init(Context c) {
        this.mContext = c;
        this.count = 0;
        this.mPref = c.getSharedPreferences("consobox", 0);
        int[] acc = {this.mPref.getInt("accesrapide1", -1), this.mPref.getInt("accesrapide2", -1), this.mPref.getInt("accesrapide3", -1)};
        for (int i = 0; i < 3; i++) {
            if (acc[i] != -1 && !exists(acc[i])) {
                delete(i + 1);
            }
        }
        int[] acc2 = {this.mPref.getInt("accesrapide1", -1), this.mPref.getInt("accesrapide2", -1), this.mPref.getInt("accesrapide3", -1)};
        for (int i2 = 0; i2 < 3; i2++) {
            if (acc2[i2] != -1) {
                this.count++;
            }
            this.mId[i2] = Integer.valueOf(acc2[i2]);
            if (acc2[i2] == -1) {
                this.mLabel[i2] = c.getResources().getString(R.string.ajouter);
                this.mMenu[i2] = Integer.valueOf(R.drawable.plus);
                break;
            } else {
                this.mLabel[i2] = getVoitureName(acc2[i2]);
                this.mMenu[i2] = Integer.valueOf(R.drawable.avatar_voiture);
            }
        }
        if (this.count < 3) {
            this.count++;
        }
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.count;
    }

    /* JADX WARN: Type inference failed for: r2v0, types: [com.consobox.AccesRapideAdapter$1] */
    public void deleteWithAcceptance(int emplacement) {
        Message.YesNoMessage(this.mContext.getResources().getString(R.string.suppaccesrapide), this.mContext, new View.OnClickListener() { // from class: com.consobox.AccesRapideAdapter.1
            private int mId;

            public View.OnClickListener setId(int i) {
                this.mId = i;
                return this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (v.getId() == R.id.YesNoDetele_yes) {
                    AccesRapideAdapter.this.delete(this.mId);
                    AccesRapideAdapter.this.init(AccesRapideAdapter.this.mContext);
                    AccesRapideAdapter.this.notifyDataSetChanged();
                }
                Message.dialogInProgress.cancel();
            }
        }.setId(emplacement + 1));
    }

    protected void delete(int i) {
        this.mPref.edit().putInt("accesrapide" + i, -1).commit();
        for (int e = i; e < 3; e++) {
            this.mPref.edit().putInt("accesrapide" + e, this.mPref.getInt("accesrapide" + (e + 1), -1)).putInt("accesrapide" + (e + 1), -1).commit();
        }
    }

    private boolean exists(int voiture) {
        SQLiteDatabase sql = Utils.getDatabase(this.mContext);
        Cursor c = sql.query("voiture", new String[]{"nom"}, "id=" + voiture, null, null, null, null);
        boolean exist = c.getCount() > 0;
        c.close();
        Utils.closeDatabase(this.mContext, sql);
        return exist;
    }

    private String getVoitureName(int voiture) {
        SQLiteDatabase sql = Utils.getDatabase(this.mContext);
        Cursor c = sql.query("voiture", new String[]{"nom"}, "id=" + voiture, null, null, null, null);
        if (c.getCount() == 0) {
            c.close();
            Utils.closeDatabase(this.mContext, sql);
            return this.mContext.getResources().getString(R.string.erreur);
        }
        c.moveToFirst();
        String string = c.getString(c.getColumnIndex("nom"));
        c.close();
        Utils.closeDatabase(this.mContext, sql);
        return string;
    }

    public void addAt(int emplacement) {
        this.mSavedEmplacement = emplacement + 1;
        if (this.mPref.getBoolean("accesRapidePopupOk", false)) {
            add();
        } else {
            Message.CustomMessage(this.mContext.getResources().getString(R.string.premieraccesrapide), "Ok", null, this.mContext, new DialogInterface.OnClickListener() { // from class: com.consobox.AccesRapideAdapter.2
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int which) {
                    AccesRapideAdapter.this.add();
                }
            });
            this.mPref.edit().putBoolean("accesRapidePopupOk", true).commit();
        }
    }

    protected void add() {
        SQLiteDatabase sql = Utils.getDatabase(this.mContext);
        Cursor c = sql.query("voiture", new String[]{"id", "nom"}, null, null, null, null, null);
        c.moveToFirst();
        if (c.getCount() != 0) {
            CharSequence[] items = new CharSequence[c.getCount()];
            this.mSavedIds = new int[c.getCount()];
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                items[i] = c.getString(c.getColumnIndex("nom"));
                this.mSavedIds[i] = c.getInt(c.getColumnIndex("id"));
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
            builder.setTitle("Voitures");
            builder.setItems(items, new DialogInterface.OnClickListener() { // from class: com.consobox.AccesRapideAdapter.3
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int item) {
                    AccesRapideAdapter.this.mPref.edit().putInt("accesrapide" + AccesRapideAdapter.this.mSavedEmplacement, AccesRapideAdapter.this.mSavedIds[item]).commit();
                    AccesRapideAdapter.this.init(AccesRapideAdapter.this.mContext);
                    AccesRapideAdapter.this.notifyDataSetChanged();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override // android.widget.Adapter
    public Object getItem(int arg0) {
        return this.mId[arg0];
    }

    @Override // android.widget.Adapter
    public long getItemId(int arg0) {
        return this.mId[arg0].intValue();
    }

    @Override // android.widget.Adapter
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        RelativeLayout relativeLayout = new RelativeLayout(this.mContext);
        ImageView imageView = new ImageView(this.mContext);
        relativeLayout.setLayoutParams(new AbsListView.LayoutParams(new ViewGroup.LayoutParams(-1, Utils.dipToPixel(25, this.mContext))));
        relativeLayout.setGravity(16);
        TextView txt = new TextView(this.mContext);
        RelativeLayout.LayoutParams paramsTxt = new RelativeLayout.LayoutParams((ViewGroup.MarginLayoutParams) new RelativeLayout.LayoutParams(-2, -2));
        paramsTxt.addRule(15, -1);
        if (this.mId[arg0].intValue() == -1) {
            RelativeLayout.LayoutParams paramsImage = new RelativeLayout.LayoutParams((ViewGroup.MarginLayoutParams) new RelativeLayout.LayoutParams(-1, -1));
            paramsImage.setMargins(Utils.dipToPixel(5, this.mContext), 0, 0, 0);
            imageView.setLayoutParams(paramsImage);
            imageView.setScaleType(ImageView.ScaleType.FIT_START);
            imageView.setImageResource(this.mMenu[arg0].intValue());
            relativeLayout.addView(imageView);
            paramsTxt.addRule(1, imageView.getId());
            paramsTxt.setMargins(Utils.dipToPixel(35, this.mContext), 0, 0, 0);
        } else {
            txt.setTypeface(Typeface.DEFAULT_BOLD);
            txt.setTextColor(-1);
            paramsTxt.addRule(14, -1);
        }
        txt.setText(this.mLabel[arg0]);
        txt.setLayoutParams(paramsTxt);
        relativeLayout.addView(txt);
        return relativeLayout;
    }
}
