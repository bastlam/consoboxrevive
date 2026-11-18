package com.consobox.screen.voitures.viewvoiture;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.consobox.R;
import com.consobox.data.Message;
import com.consobox.data.Utils;
import com.consobox.lib.popup.QuickAction;
import com.consobox.models.Pneu;
import com.consobox.models.Voiture;
import com.consobox.screen.voitures.PneuListActivity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* JADX INFO: loaded from: classes.dex */
public class ViewVoiturePneus extends LinearLayout implements OnVisible, View.OnClickListener, DialogInterface.OnClickListener {
    private Pneu mArriere;
    private Pneu mAvant;
    private Context mContext;
    private int mCurrentPosition;
    private ImageView mListIcon;
    private Activity mParent;
    private ImageView mPneuDroiteBas;
    private ImageView mPneuDroiteHaut;
    private ImageView mPneuGaucheBas;
    private ImageView mPneuGaucheHaut;
    private ArrayList<Pneu> mPneus;
    private TextView mPneusArText;
    private TextView mPneusAvText;
    private QuickAction mShowingQuickAction;
    private Pneu mTempPneu;
    private Voiture mVoiture;

    public ViewVoiturePneus(Context context, Voiture voiture, Activity parent) {
        super(context);
        this.mVoiture = voiture;
        this.mContext = context;
        this.mParent = parent;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService("layout_inflater");
        inflater.inflate(R.layout.layout_viewvoiturepneu, this);
        this.mPneusAvText = (TextView) findViewById(R.id.pneusavtext);
        this.mPneusArText = (TextView) findViewById(R.id.pneusartext);
        this.mPneuDroiteHaut = (ImageView) findViewById(R.id.pneudroitehaut);
        this.mPneuDroiteBas = (ImageView) findViewById(R.id.pneudroitebas);
        this.mPneuGaucheBas = (ImageView) findViewById(R.id.pneugauchebas);
        this.mPneuGaucheHaut = (ImageView) findViewById(R.id.pneugauchehaut);
        this.mListIcon = (ImageView) findViewById(R.id.listicon);
        this.mPneuDroiteHaut.setOnClickListener(this);
        this.mPneuDroiteBas.setOnClickListener(this);
        this.mPneuGaucheHaut.setOnClickListener(this);
        this.mPneuGaucheBas.setOnClickListener(this);
        this.mListIcon.setOnClickListener(this);
        init();
    }

    public void init() {
        getUsablePneu();
        initPneuText();
    }

    private void checkIntegrity() {
        SQLiteDatabase sql = Utils.getDatabase(this.mContext);
        ContentValues values = new ContentValues();
        values.put("used", (Integer) 0);
        int idAvant = this.mAvant != null ? this.mAvant.id : -1;
        int idArriere = this.mArriere != null ? this.mArriere.id : -1;
        sql.update("pneu", values, "id!=" + idAvant + " AND id!=" + idArriere, null);
        Utils.closeDatabase(this.mContext, sql);
    }

    private void initPneuText() {
        String arriere;
        String avant;
        String avant2 = "Pas de pneus enregistrés";
        String arriere2 = "Pas de pneus enregistrés";
        if (this.mAvant != null) {
            if (this.mAvant.isHiver) {
                avant = String.valueOf("Pneus ") + "hiver ";
            } else {
                avant = String.valueOf("Pneus ") + "été ";
            }
            avant2 = String.valueOf(avant) + this.mAvant.description;
        }
        if (this.mArriere != null) {
            if (this.mArriere.isHiver) {
                arriere = String.valueOf("Pneus ") + "hiver ";
            } else {
                arriere = String.valueOf("Pneus ") + "été ";
            }
            arriere2 = String.valueOf(arriere) + this.mArriere.description;
        }
        this.mPneusAvText.setText(avant2);
        this.mPneusArText.setText(arriere2);
    }

    private void getUsablePneu() {
        this.mPneus = new ArrayList<>();
        this.mAvant = null;
        this.mArriere = null;
        SQLiteDatabase sql = Utils.getDatabase(this.mContext);
        Cursor c = sql.query("pneu", new String[]{"*"}, "voiture=" + this.mVoiture.id, null, null, null, null);
        if (c.getCount() != 0) {
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                Pneu p = new Pneu(c);
                if (p.usable && p.used == Pneu.ARRIERE) {
                    this.mArriere = p;
                } else if (p.usable && p.used == Pneu.AVANT) {
                    this.mAvant = p;
                }
                this.mPneus.add(p);
            }
        }
        c.close();
        Utils.closeDatabase(this.mContext, sql);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ok) {
                if (Message.dialogInProgress.isShowing()) {
                    Message.dialogInProgress.cancel();
                }
                String date = ((TextView) Message.dialogInProgress.findViewById(R.id.date)).getText().toString();
                String marque = ((TextView) Message.dialogInProgress.findViewById(R.id.marque)).getText().toString();
                String modele = ((TextView) Message.dialogInProgress.findViewById(R.id.modele)).getText().toString();
                String description = ((TextView) Message.dialogInProgress.findViewById(R.id.description)).getText().toString();
                float prix = Float.parseFloat(((TextView) Message.dialogInProgress.findViewById(R.id.prix)).getText().toString());
                boolean isHiver = ((CheckBox) Message.dialogInProgress.findViewById(R.id.hiver)).isChecked();
                createPneu(date, marque, modele, description, isHiver, this.mCurrentPosition, prix);
        } else if (id == R.id.annuler) {
                if (Message.dialogInProgress.isShowing()) {
                    Message.dialogInProgress.cancel();
                }
        } else if (id == R.id.pneugauchehaut || id == R.id.pneudroitehaut) {
                inflatePneuPopup(this.mAvant, v.getId());
                this.mCurrentPosition = Pneu.AVANT;
        } else if (id == R.id.pneugauchebas || id == R.id.pneudroitebas) {
                inflatePneuPopup(this.mArriere, v.getId());
                this.mCurrentPosition = Pneu.ARRIERE;
        } else if (id == R.id.listicon) {
                Intent intent = new Intent(this.mParent, (Class<?>) PneuListActivity.class);
                intent.putExtra("voitureid", this.mVoiture.id);
                this.mParent.startActivity(intent);
        } else if (id == R.id.changer) {
                this.mShowingQuickAction.dismiss();
                changerPneu();
        }
    }

    private void inflatePneuPopup(Pneu pneu, int viewId) {
        if (pneu != null) {
            LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
            View view = inflater.inflate(R.layout.popup_pneu, (ViewGroup) null);
            ((TextView) view.findViewById(R.id.description)).setText("Pneu " + pneu.marque + " " + pneu.modele);
            String desc = pneu.description;
            String[] tmp = desc.split(" ");
            String[] dimension = tmp[0].split("/");
            if (dimension.length == 2) {
                ((TextView) view.findViewById(R.id.largeur)).setText("Largeur : " + dimension[0] + "mm");
                ((TextView) view.findViewById(R.id.hauteur)).setText("Hauteur : " + ((int) ((Float.parseFloat(dimension[1]) / 100.0f) * Float.parseFloat(dimension[0]))) + "mm");
            }
            if (tmp.length >= 2) {
                ((TextView) view.findViewById(R.id.diametre)).setText("Diametre : " + tmp[1].toUpperCase().replace("R", "") + "pouces");
            }
            if (tmp.length == 3) {
                Pattern p = Pattern.compile("(\\d+)(.+)");
                Matcher m = p.matcher(tmp[2]);
                if (m.matches()) {
                    ((TextView) view.findViewById(R.id.indicepoids)).setText("Indice poids : " + Utils.getPoidsFromIndice(Integer.parseInt(m.group(1)), this.mContext) + "kg");
                    ((TextView) view.findViewById(R.id.indicevitesse)).setText("Indice vitesse : " + Utils.getVitesseFromIndice(m.group(2).toUpperCase()) + "km/h");
                }
            }
            this.mShowingQuickAction = new QuickAction(this.mContext, 1);
            ((Button) view.findViewById(R.id.changer)).setOnClickListener(this);
            this.mShowingQuickAction.setView(view);
            this.mShowingQuickAction.show(findViewById(viewId));
            return;
        }
        changerPneu();
    }

    private void createPneu(String date, String marque, String modele, String description, boolean isHiver, int position, float prix) {
        Pneu p = new Pneu();
        p.dateMontage = date;
        p.dateAchat = date;
        p.marque = marque;
        p.modele = modele;
        p.description = description;
        p.isHiver = isHiver;
        p.used = position;
        p.usable = true;
        p.prix = prix;
        ContentValues values = p.getDataForInsert();
        values.put("voiture", Integer.valueOf(this.mVoiture.id));
        SQLiteDatabase sql = Utils.getDatabase(this.mContext);
        p.id = (int) sql.insert("pneu", "0", values);
        ContentValues depense = new ContentValues();
        depense.put("voiture", Integer.valueOf(this.mVoiture.id));
        depense.put("type", (Integer) 1);
        String[] dates = date.split("/");
        depense.put("date", String.valueOf(dates[2]) + "-" + (Integer.parseInt(dates[1]) < 10 ? "0" : "") + Integer.parseInt(dates[1]) + "-" + (Integer.parseInt(dates[0]) < 10 ? "0" : "") + Integer.parseInt(dates[0]));
        depense.put("km", Float.valueOf(this.mVoiture.km));
        depense.put("prix", Float.valueOf(prix));
        depense.put("description", Integer.valueOf(p.id + 20));
        sql.insert("depenses", "0", depense);
        Utils.closeDatabase(this.mContext, sql);
        switchPneu(p, position, true);
    }

    private void switchPneu(Pneu nouveauPneu, int position) {
        switchPneu(nouveauPneu, position, false);
    }

    private void switchPneu(Pneu nouveauPneu, int position, boolean isNew) {
        Pneu vieuxPneu;
        if (position == Pneu.AVANT) {
            vieuxPneu = this.mAvant;
            this.mAvant = nouveauPneu;
        } else {
            vieuxPneu = this.mArriere;
            this.mArriere = nouveauPneu;
        }
        SQLiteDatabase sql = Utils.getDatabase(this.mContext);
        nouveauPneu.used = position;
        if (!isNew) {
            Calendar today = Calendar.getInstance();
            int month = today.get(2) + 1;
            int day = today.get(5);
            int year = today.get(1);
            nouveauPneu.dateMontage = String.valueOf(day) + "/" + month + "/" + year;
        }
        ContentValues values = nouveauPneu.getDataForInsert();
        values.put("voiture", Integer.valueOf(this.mVoiture.id));
        sql.update("pneu", values, "id=" + nouveauPneu.id, null);
        this.mCurrentPosition = position;
        this.mTempPneu = vieuxPneu;
        if (vieuxPneu != null) {
            vieuxPneu.used = Pneu.NON_MONTER;
            sql.update("pneu", nouveauPneu.getDataForInsert(), "id=" + vieuxPneu.id, null);
            Message.CustomMessage("Conservez-vous vos anciens pneus ?", "Oui", "Non", this.mContext, this);
        } else {
            checkIntegrity();
            initPneuText();
        }
        Utils.closeDatabase(this.mContext, sql);
    }

    private void changerPneu() {
        CharSequence[] items = {"Nouveaux pneus", "Pneu en stock"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
        builder.setTitle("Changer de pneus");
        builder.setItems(items, new DialogInterface.OnClickListener() { // from class: com.consobox.screen.voitures.viewvoiture.ViewVoiturePneus.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int item) {
                dialog.cancel();
                if (item != 0) {
                    if (item != 1) {
                        return;
                    }
                    ViewVoiturePneus.this.ChangerPneusEnStock();
                    return;
                }
                ViewVoiturePneus.this.changerNewPneus();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void changerNewPneus() {
        Calendar today = Calendar.getInstance();
        int month = today.get(2) + 1;
        int day = today.get(5);
        String date = String.valueOf(day < 10 ? "0" : "") + day + "/" + (month < 10 ? "0" : "") + month + "/" + today.get(1);
        Message.PneuBox(this.mContext, this, date);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void ChangerPneusEnStock() {
        Intent intent = new Intent(this.mParent, (Class<?>) PneuListActivity.class);
        intent.putExtra("voitureid", this.mVoiture.id);
        intent.putExtra("requestCode", PneuListActivity.GET_PNEU_ID);
        this.mParent.startActivityForResult(intent, PneuListActivity.GET_PNEU_ID);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PneuListActivity.GET_PNEU_ID && resultCode == -1) {
            long id = data.getExtras().getLong("pneuId");
            for (int i = 0; i < this.mPneus.size(); i++) {
                if (this.mPneus.get(i).id == id) {
                    switchPneu(this.mPneus.get(i), this.mCurrentPosition);
                    break;
                }
            }
        }
        initPneuText();
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialog, int which) {
        Pneu oldPneu = this.mTempPneu;
        if (oldPneu != null) {
            double kms = Utils.getKmSinceDate(this.mContext, oldPneu.dateMontage, this.mVoiture.id);
            oldPneu.kmUtilisation = (long) (oldPneu.kmUtilisation + kms);
            oldPneu.usable = which == -1;
            SQLiteDatabase sql = Utils.getDatabase(this.mContext);
            ContentValues values = oldPneu.getDataForInsert();
            sql.update("pneu", values, "id=" + oldPneu.id, null);
            Utils.closeDatabase(this.mContext, sql);
        }
        checkIntegrity();
        initPneuText();
    }

    @Override // com.consobox.screen.voitures.viewvoiture.OnVisible
    public void onVisible() {
    }
}
