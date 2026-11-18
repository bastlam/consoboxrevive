package com.consobox.screen.voitures.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.consobox.R;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class VoitureContentAdapter extends SimpleAdapter {
    private List<? extends Map<String, ?>> mData;

    public VoitureContentAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        this.mData = data;
    }

    @Override // android.widget.SimpleAdapter, android.widget.Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        if (this.mData.get(position).containsKey("moyenne")) {
            if (((String) this.mData.get(position).get("moyenne")).substring(0, 1).equals("+")) {
                ((TextView) view.findViewById(R.id.revisionlistrow_average)).setTextColor(view.getContext().getResources().getColor(R.color.bad));
            } else {
                ((TextView) view.findViewById(R.id.revisionlistrow_average)).setTextColor(view.getContext().getResources().getColor(R.color.good));
            }
        }
        return view;
    }
}
