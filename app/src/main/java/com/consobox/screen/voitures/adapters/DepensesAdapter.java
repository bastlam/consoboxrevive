package com.consobox.screen.voitures.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.consobox.R;
import com.consobox.data.Utils;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/* JADX INFO: loaded from: classes.dex */
public class DepensesAdapter extends BaseAdapter {
    ArrayList<HashMap<String, String>> mList;
    ArrayList<View> mViews = new ArrayList<>();

    public DepensesAdapter(ArrayList<HashMap<String, String>> list) {
        this.mList = list;
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.mList.size();
    }

    @Override // android.widget.Adapter
    public Object getItem(int position) {
        if (position >= this.mList.size()) {
            return null;
        }
        return this.mList.get(position);
    }

    @Override // android.widget.Adapter
    public long getItemId(int position) {
        return Integer.valueOf(this.mList.get(position).get("id")).intValue();
    }

    public int getCountWithoutSections() {
        int count = 0;
        for (int i = 0; i < this.mList.size(); i++) {
            HashMap<String, String> item = this.mList.get(i);
            if (item.get("isYear").equals("false") && item.get("isMonth").equals("false")) {
                count++;
            }
        }
        return count;
    }

    @Override // android.widget.Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        DecimalFormat df = new DecimalFormat("#");
        HashMap<String, String> item = this.mList.get(position);
        if (this.mViews.size() - 1 >= position) {
            try {
                if (item.get("id").equals("-1")) {
                    ((TextView) this.mViews.get(position).findViewById(R.id.prix)).setText(String.valueOf(df.format(Double.valueOf(item.get("prix")))) + Utils.getDeviseUnit(this.mViews.get(position).getContext()));
                }
            } catch (Exception e) {
            }
            return this.mViews.get(position);
        }
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService("layout_inflater");
        if (item.get("isYear").equals("true")) {
            v = inflater.inflate(R.layout.row_depensesyear, (ViewGroup) null);
            ((TextView) v.findViewById(R.id.type)).setText(item.get("type"));
            try {
                ((TextView) v.findViewById(R.id.prix)).setText(String.valueOf(df.format(Double.valueOf(item.get("prix")))) + Utils.getDeviseUnit(v.getContext()));
            } catch (Exception e2) {
            }
        } else if (item.get("isMonth").equals("true")) {
            v = inflater.inflate(R.layout.row_depensesmonth, (ViewGroup) null);
            ((TextView) v.findViewById(R.id.type)).setText(item.get("type"));
            try {
                ((TextView) v.findViewById(R.id.prix)).setText(String.valueOf(df.format(Double.valueOf(item.get("prix")))) + Utils.getDeviseUnit(v.getContext()));
            } catch (Exception e3) {
            }
        } else {
            v = inflater.inflate(R.layout.row_depenseslist, (ViewGroup) null);
            ((TextView) v.findViewById(R.id.type)).setText(item.get("type"));
            ((TextView) v.findViewById(R.id.prix)).setText(item.get("prix"));
            ((TextView) v.findViewById(R.id.date)).setText(item.get("date"));
            ((ImageView) v.findViewById(R.id.icon)).setImageResource(Integer.valueOf(item.get("icon")).intValue());
        }
        this.mViews.add(v);
        return v;
    }
}
