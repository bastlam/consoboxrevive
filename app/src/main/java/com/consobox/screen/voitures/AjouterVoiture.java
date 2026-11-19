package com.consobox.screen.voitures;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import com.consobox.R;
import com.consobox.data.CarPhoto;
import com.consobox.data.Message;
import com.consobox.data.Utils;

/* JADX INFO: loaded from: classes.dex */
public class AjouterVoiture extends Activity implements View.OnClickListener {
    private int idVoiture = -1;
    private String photo = "";

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ajouter);
        this.idVoiture = getIntent().getIntExtra("voitureamodifier", -1);
        if (this.idVoiture != -1) {
            modifierVue();
        } else {
            showPhoto();
        }
        Spinner type = (Spinner) findViewById(R.id.ajouter_type);
        type.setAdapter((SpinnerAdapter) new ArrayAdapter(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.type_of_vehicule)));
        ((Button) findViewById(R.id.ajouter_ok)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.ajouter_photo)).setOnClickListener(this);
    }

    private void modifierVue() {
        ((Button) findViewById(R.id.ajouter_ok)).setText(getResources().getString(R.string.modifier));
        SQLiteDatabase sql = Utils.getDatabase(this);
        Cursor c = sql.query("voiture", new String[]{"nom", "km", "type", "cv", "cvf", "photo"}, "id=" + this.idVoiture, null, null, null, null);
        c.moveToFirst();
        ((EditText) findViewById(R.id.ajouter_nom)).setText(c.getString(c.getColumnIndex("nom")));
        ((EditText) findViewById(R.id.ajouter_km)).setText(new StringBuilder().append(c.getInt(c.getColumnIndex("km"))).toString());
        ((Spinner) findViewById(R.id.ajouter_type)).setEnabled(false);
        ((EditText) findViewById(R.id.ajouter_cv)).setText(new StringBuilder().append(c.getInt(c.getColumnIndex("cv"))).toString());
        ((EditText) findViewById(R.id.ajouter_cvf)).setText(new StringBuilder().append(c.getInt(c.getColumnIndex("cvf"))).toString());
        this.photo = c.getString(c.getColumnIndex("photo"));
        if (this.photo == null) {
            this.photo = "";
        }
        c.close();
        Utils.closeDatabase(this, sql);
        showPhoto();
    }

    /** Car picture stretched over the whole square button, rounded corners like the card. */
    private void showPhoto() {
        ImageButton button = (ImageButton) findViewById(R.id.ajouter_photo);
        int pad = CarPhoto.show(button, this.photo, 20) ? 0 : Utils.dipToPixel(8, this);
        button.setPadding(pad, pad, pad, pad);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View obj) {
        if (obj.equals(findViewById(R.id.ajouter_ok))) {
            SQLiteDatabase database = Utils.getDatabase(this);
            database.execSQL(getResources().getString(R.string.DB_VOITURE));
            if (this.idVoiture == -1) {
                ajouter(database);
                return;
            } else {
                modifier(database);
                return;
            }
        }
        if (obj.equals(findViewById(R.id.ajouter_photo))) {
            CharSequence[] items = {getResources().getString(R.string.photodisque), getResources().getString(R.string.appphoto)};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setItems(items, new DialogInterface.OnClickListener() { // from class: com.consobox.screen.voitures.AjouterVoiture.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int item) {
                    if (item == 0) {
                        AjouterVoiture.this.addPhotoViaGalerie();
                    } else if (item == 1) {
                        AjouterVoiture.this.addPhotoViaApp();
                    }
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    protected void addPhotoViaGalerie() {
        Intent mediaChooser = new Intent("android.intent.action.GET_CONTENT");
        mediaChooser.setType("image/*");
        startActivityForResult(mediaChooser, 1);
    }

    protected void addPhotoViaApp() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(intent, 1);
    }

    @Override // android.app.Activity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != -1 || data == null) {
            return;
        }
        try {
            Bitmap source = null;
            if (data.getData() != null) {
                source = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
            } else if (data.getExtras() != null && data.getExtras().get("data") instanceof Bitmap) {
                // camera apps return a thumbnail when no output file is given
                source = (Bitmap) data.getExtras().get("data");
            }
            if (source == null) {
                return;
            }
            // stored in the app's own storage: content:// URIs from the gallery are not readable after a restart
            this.photo = CarPhoto.save(this, source);
            showPhoto();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void modifier(SQLiteDatabase db) {
        int km;
        int cv;
        int cvf;
        String nom = ((EditText) findViewById(R.id.ajouter_nom)).getText().toString().replace("'", " ");
        try {
            km = Integer.valueOf(((EditText) findViewById(R.id.ajouter_km)).getText().toString()).intValue();
        } catch (Exception e) {
            km = 0;
        }
        try {
            cv = Integer.valueOf(((EditText) findViewById(R.id.ajouter_cv)).getText().toString()).intValue();
        } catch (Exception e2) {
            cv = 0;
        }
        try {
            cvf = Integer.valueOf(((EditText) findViewById(R.id.ajouter_cvf)).getText().toString()).intValue();
        } catch (Exception e3) {
            cvf = 0;
        }
        if (nom.equals("")) {
            Message.ErrorMessage(getResources().getString(R.string.aucunnom), this);
            return;
        }
        db.execSQL("UPDATE `voiture` set `nom`='" + nom + "',`km`=" + km + ",`cv`=" + cv + ",`cvf`=" + cvf + ",`photo`='" + this.photo + "' WHERE id=" + this.idVoiture);
        setResult(1, getIntent());
        finish();
    }

    private void ajouter(SQLiteDatabase db) {
        int km;
        int cv;
        int cvf;
        String nom = ((EditText) findViewById(R.id.ajouter_nom)).getText().toString().replace("'", " ");
        try {
            km = Integer.valueOf(((EditText) findViewById(R.id.ajouter_km)).getText().toString()).intValue();
        } catch (Exception e) {
            km = 0;
        }
        try {
            cv = Integer.valueOf(((EditText) findViewById(R.id.ajouter_cv)).getText().toString()).intValue();
        } catch (Exception e2) {
            cv = 0;
        }
        try {
            cvf = Integer.valueOf(((EditText) findViewById(R.id.ajouter_cvf)).getText().toString()).intValue();
        } catch (Exception e3) {
            cvf = 0;
        }
        String type = ((Spinner) findViewById(R.id.ajouter_type)).getSelectedItem().toString();
        if (nom.equals("")) {
            Message.ErrorMessage(getResources().getString(R.string.aucunnom), this);
            return;
        }
        db.execSQL("INSERT INTO `voiture` (`nom`,`type`,`km`,`cv`,`cvf`,`photo`) values ('" + nom + "','" + type + "'," + km + "," + cv + "," + cvf + ",'" + this.photo + "')");
        getIntent().putExtra("nom", "ajouter");
        setResult(1, getIntent());
        finish();
    }
}
