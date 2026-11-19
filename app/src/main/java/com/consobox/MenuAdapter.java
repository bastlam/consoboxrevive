package com.consobox;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.consobox.data.Utils;

/* JADX INFO: loaded from: classes.dex */
public class MenuAdapter extends BaseAdapter {
    private Context mContext;
    private Integer[] mMenu = {Integer.valueOf(R.drawable.menu_car), Integer.valueOf(R.drawable.menu_settings)};
    private String[] mLabel = new String[2];

    public MenuAdapter(Context c) {
        this.mContext = c;
        this.mLabel[0] = c.getResources().getString(R.string.listevoiture);
        this.mLabel[1] = c.getResources().getString(R.string.parametres);
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.mMenu.length;
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
        layout.setLayoutParams(new AbsListView.LayoutParams(new ViewGroup.LayoutParams(-1, -2)));
        layout.setOrientation(1);
        layout.setGravity(17);
        layout.setPadding(10, 10, 10, 10);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(Utils.dipToPixel(150, this.mContext), Utils.dipToPixel(150, this.mContext))));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setImageResource(this.mMenu[arg0].intValue());
        TextView txt = new TextView(this.mContext);
        txt.setLayoutParams(new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(-2, -2)));
        txt.setGravity(17);
        txt.setText(this.mLabel[arg0]);
        layout.addView(imageView);
        layout.addView(txt);
        return layout;
    }
}
