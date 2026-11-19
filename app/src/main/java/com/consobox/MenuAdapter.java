package com.consobox;

import android.content.Context;
import android.view.LayoutInflater;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView != null ? convertView
                : LayoutInflater.from(this.mContext).inflate(R.layout.item_main_menu, parent, false);
        ((ImageView) v.findViewById(R.id.icon)).setImageResource(this.mMenu[position].intValue());
        ((TextView) v.findViewById(R.id.nom)).setText(this.mLabel[position]);
        return v;
    }
}
