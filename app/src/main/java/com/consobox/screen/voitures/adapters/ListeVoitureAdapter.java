package com.consobox.screen.voitures.adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.consobox.R;
import com.consobox.data.Utils;
import com.consobox.models.Voiture;
import java.text.DecimalFormat;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes.dex */
public class ListeVoitureAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Voiture> mVoitures;

    public ListeVoitureAdapter(Context c) {
        this.mContext = c;
    }

    public void setItems(ArrayList<Voiture> items) {
        this.mVoitures = items;
        int i = 0;
        for (Voiture voiture : items) {
            try {
                Uri uri = Uri.parse(voiture.photo);
                if (this.mContext.getContentResolver().getType(uri) == null) {
                    voiture.photoUri = null;
                } else {
                    voiture.photoUri = uri;
                }
            } catch (Exception e) {
                voiture.photoUri = null;
            }
            i++;
        }
    }

    @Override // android.widget.Adapter
    public int getCount() {
        if (this.mVoitures != null) {
            return this.mVoitures.size() + 1;
        }
        return 1;
    }

    @Override // android.widget.Adapter
    public Object getItem(int arg0) {
        if (arg0 == getCount() - 1) {
            return -1;
        }
        return this.mVoitures.get(arg0);
    }

    @Override // android.widget.Adapter
    public long getItemId(int arg0) {
        if (arg0 == getCount() - 1) {
            return -1L;
        }
        return this.mVoitures.get(arg0).id;
    }

    @Override // android.widget.Adapter
    public View getView(int id, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService("layout_inflater");
        View v = inflater.inflate(R.layout.row_voiturelist, (ViewGroup) null);
        ImageView imageView = (ImageView) v.findViewById(R.id.icon);
        TextView nom = (TextView) v.findViewById(R.id.nom);
        TextView conso = (TextView) v.findViewById(R.id.conso);
        TextView kmage = (TextView) v.findViewById(R.id.kmage);
        if (getItemId(id) == -1) {
            imageView.setImageResource(R.drawable.plus);
            nom.setText(parent.getContext().getResources().getString(R.string.ajouter));
            conso.setText("");
            kmage.setText("");
        } else {
            try {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(Utils.getRealPathFromURI(this.mVoitures.get(id).photoUri, this.mContext), opts);
                opts.inJustDecodeBounds = false;
                if (this.mVoitures.get(id).photoUri == null || opts.outWidth > 200) {
                    imageView.setImageResource(R.drawable.avatar_voiture);
                } else {
                    imageView.setImageURI(this.mVoitures.get(id).photoUri);
                }
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.avatar_voiture);
                e.printStackTrace();
            }
            nom.setText(this.mVoitures.get(id).nom);
            DecimalFormat df = new DecimalFormat("##.##");
            conso.setText(String.valueOf(df.format(Utils.getConsoMoyenne(this.mContext, this.mVoitures.get(id).id))) + "l/100km");
            kmage.setText(String.valueOf(this.mVoitures.get(id).km) + "km");
        }
        return v;
    }
}
