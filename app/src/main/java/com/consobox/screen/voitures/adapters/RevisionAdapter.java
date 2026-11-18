package com.consobox.screen.voitures.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class RevisionAdapter extends SimpleAdapter {
    ArrayList<Integer> toDo;

    public RevisionAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        this.toDo = new ArrayList<>();
    }

    @Override // android.widget.SimpleAdapter, android.widget.Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View convertView2 = super.getView(position, convertView, parent);
        if (this.toDo.contains(Integer.valueOf(position))) {
            ((LinearLayout) convertView2).setBackgroundColor(Color.argb(160, 255, 0, 0));
        }
        return convertView2;
    }

    public void addRevisionToDo(ArrayList<Integer> pos) {
        this.toDo = pos;
    }
}
