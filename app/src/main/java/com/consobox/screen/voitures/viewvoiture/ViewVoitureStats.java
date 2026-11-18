package com.consobox.screen.voitures.viewvoiture;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.consobox.R;
import com.consobox.screen.voitures.Listeners.StatsListListener;

/* JADX INFO: loaded from: classes.dex */
public class ViewVoitureStats extends LinearLayout implements OnVisible {
    private Context mContext;
    private Activity mParent;

    public ViewVoitureStats(Context context, Activity parent) {
        super(context);
        this.mContext = context;
        this.mParent = parent;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService("layout_inflater");
        inflater.inflate(R.layout.layout_viewvoiturestats, this);
    }

    public void init() {
        String[] liste = getResources().getStringArray(R.array.menustats);
        ((ListView) findViewById(R.id.view_voiture_liststats)).setAdapter((ListAdapter) new ArrayAdapter(this.mContext, R.layout.row_viewvoiturestatslist, R.id.viewvoiturestatslistrow_nom, liste));
        ((ListView) findViewById(R.id.view_voiture_liststats)).setOnItemClickListener(new StatsListListener(this.mParent));
    }

    @Override // com.consobox.screen.voitures.viewvoiture.OnVisible
    public void onVisible() {
        init();
    }
}
