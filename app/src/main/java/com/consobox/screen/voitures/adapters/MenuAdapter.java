package com.consobox.screen.voitures.adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.consobox.R;
import com.consobox.data.Utils;
import java.util.ArrayList;
import java.util.HashMap;

/* JADX INFO: loaded from: classes.dex */
public class MenuAdapter extends BaseAdapter {
    private Context mContext;
    private String[] mLabel = new String[4];
    private Uri[] mMenu;
    private int[] mVoitureId;

    public MenuAdapter(Context c) {
        this.mContext = c;
    }

    public void setItems(ArrayList<HashMap<String, String>> items) {
        this.mLabel = new String[items.size()];
        this.mVoitureId = new int[items.size()];
        this.mMenu = new Uri[items.size()];
        int i = 0;
        for (HashMap<String, String> item : items) {
            this.mLabel[i] = item.get("nom");
            this.mVoitureId[i] = Integer.valueOf(item.get("id")).intValue();
            try {
                Uri uri = Uri.parse(item.get("photo"));
                if (this.mContext.getContentResolver().getType(uri) == null) {
                    this.mMenu[i] = null;
                } else {
                    this.mMenu[i] = uri;
                }
            } catch (Exception e) {
                this.mMenu[i] = null;
            }
            i++;
        }
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.mLabel.length;
    }

    @Override // android.widget.Adapter
    public Object getItem(int arg0) {
        return this.mMenu[arg0];
    }

    @Override // android.widget.Adapter
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override // android.widget.Adapter
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        LinearLayout layout = new LinearLayout(this.mContext);
        ImageView imageView = new ImageView(this.mContext);
        layout.setLayoutParams(new AbsListView.LayoutParams(new ViewGroup.LayoutParams(-1, Utils.dipToPixel(130, this.mContext))));
        layout.setOrientation(1);
        layout.setGravity(17);
        layout.setPadding(10, 10, 10, 10);
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(Utils.getRealPathFromURI(this.mMenu[arg0], this.mContext), opts);
            opts.inJustDecodeBounds = false;
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            if (this.mVoitureId[arg0] == -1) {
                imageView.setLayoutParams(new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(-2, -2)));
                imageView.setImageResource(R.drawable.plus);
            } else if (this.mMenu[arg0] == null || opts.outWidth > 200) {
                imageView.setLayoutParams(new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(-2, -2)));
                imageView.setImageResource(R.drawable.avatar_voiture);
            } else {
                imageView.setLayoutParams(new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(Utils.dipToPixel(120, this.mContext), Utils.dipToPixel(90, this.mContext))));
                imageView.setImageURI(this.mMenu[arg0]);
            }
        } catch (Exception e) {
            imageView.setLayoutParams(new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(-2, -2)));
            imageView.setImageResource(R.drawable.avatar_voiture);
            e.printStackTrace();
        }
        TextView txt = new TextView(this.mContext);
        txt.setLayoutParams(new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(-2, -2)));
        txt.setGravity(17);
        txt.setText(this.mLabel[arg0]);
        layout.addView(imageView);
        layout.addView(txt);
        return layout;
    }

    public int getVoitureId(int index) {
        return this.mVoitureId[index];
    }
}
