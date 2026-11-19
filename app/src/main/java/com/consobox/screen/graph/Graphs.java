package com.consobox.screen.graph;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.consobox.R;
import com.consobox.data.Message;
import com.consobox.data.Utils;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LineGraphView;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/* JADX INFO: loaded from: classes.dex */
public class Graphs extends Activity implements View.OnClickListener {
    private static final int CONSOMMATION = 0;
    private static final int DEPENSES = 2;
    private static final int PRIX = 1;
    private final GraphView[] mGraphViews = new GraphView[3];
    private GraphView.GraphViewSeries[] mSeries;
    private int mVoiture;
    private boolean[] mGraphState = {true, true, true};
    private final CharSequence[] mLegends = {"Consommation (l/100 km)", "Prix au litre (€)", "Dépenses carburant (€)"};

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mSeries = new GraphView.GraphViewSeries[this.mGraphState.length];
        initGraph();
    }

    private void initGraph() {
        RelativeLayout layout = new RelativeLayout(this);
        SQLiteDatabase sql = Utils.getDatabase(this);
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        this.mVoiture = getIntent().getIntExtra("voiture", 0);
        Cursor c = sql.query("conso", new String[]{"date", "km", "volume", "prix"}, Utils.SQL_PLEIN_COMPLET + " AND km>0 AND voiture=" + this.mVoiture, null, null, null, "date", null);
        c.moveToFirst();
        GraphView.GraphViewData[] datasConso = new GraphView.GraphViewData[c.getCount()];
        GraphView.GraphViewData[] datasPrix = new GraphView.GraphViewData[c.getCount()];
        if (c.getCount() == 0) {
            c.close();
            Utils.closeDatabase(this, sql);
            Message.ErrorMessage(getResources().getString(R.string.nodata), this);
            return;
        }
        long timeZero = 0;
        long time = 0;
        try {
            timeZero = formatter.parse(c.getString(c.getColumnIndex("date"))).getTime() / 86400000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (c.getCount() != 0) {
            c.moveToFirst();
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                try {
                    time = formatter.parse(c.getString(c.getColumnIndex("date"))).getTime() / 86400000;
                } catch (ParseException e2) {
                    e2.printStackTrace();
                }
                double volume = c.getDouble(c.getColumnIndex("volume"));
                double prix = c.getDouble(c.getColumnIndex("prix"));
                double km = c.getDouble(c.getColumnIndex("km"));
                datasConso[i] = new GraphView.GraphViewData(time - timeZero, (volume / km) * 100.0d);
                datasPrix[i] = new GraphView.GraphViewData(time - timeZero, prix);
            }
        }
        c.close();
        Cursor c2 = sql.query("depenses", new String[]{"date", "sum(prix) as prix"}, "type=0 AND voiture=" + this.mVoiture, null, "date", null, "date", null);
        c2.moveToFirst();
        GraphView.GraphViewData[] depensesPrix = new GraphView.GraphViewData[c2.getCount()];
        if (c2.getCount() != 0) {
            c2.moveToFirst();
            for (int i2 = 0; i2 < c2.getCount(); i2++) {
                c2.moveToPosition(i2);
                try {
                    time = formatter.parse(c2.getString(c2.getColumnIndex("date"))).getTime() / 86400000;
                } catch (ParseException e3) {
                    e3.printStackTrace();
                }
                double prix2 = c2.getDouble(c2.getColumnIndex("prix"));
                depensesPrix[i2] = new GraphView.GraphViewData(time - timeZero, prix2);
            }
        }
        c2.close();
        Utils.closeDatabase(this, sql);
        this.mSeries[0] = new GraphView.GraphViewSeries(null, Integer.valueOf(Color.rgb(0xF2, 0x8C, 0x28)), datasConso);
        this.mSeries[1] = new GraphView.GraphViewSeries(null, Integer.valueOf(Color.rgb(0x3B, 0x82, 0xF6)), datasPrix);
        this.mSeries[2] = new GraphView.GraphViewSeries(null, Integer.valueOf(getColor(R.color.cb_primary)), depensesPrix);

        // One chart per curve, stacked: each one has its own vertical scale, so that small
        // variations (e.g. the price per litre) stay visible. Scrolling / zooming is shared.
        LinearLayout charts = new LinearLayout(this);
        charts.setOrientation(LinearLayout.VERTICAL);
        int margin = Utils.dipToPixel(4, this);
        charts.setPadding(margin, margin, margin, margin);
        GraphView.ViewportListener sync = (source, start, size) -> {
            for (GraphView g : this.mGraphViews) {
                if (g != null && g != source) {
                    g.syncViewport(start, size);
                }
            }
        };
        for (int i = 0; i < this.mSeries.length; i++) {
            GraphView g = new LineGraphView(this, this.mLegends[i].toString());
            g.addSeries(this.mSeries[i]);
            g.baseDate = timeZero;
            g.setViewPort(0.0d, time - timeZero);
            g.setScrollable(true);
            g.setScalable(true);
            g.labelColor = getColor(R.color.cb_on_surface);
            g.gridColor = getColor(R.color.cb_outline);
            g.setViewportListener(sync);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, 0, 1.0f);
            lp.bottomMargin = margin;
            g.setLayoutParams(lp);
            this.mGraphViews[i] = g;
            if (this.mSeries[i].isEmpty()) {
                this.mGraphState[i] = false;
                g.setVisibility(View.GONE);
            }
            charts.addView(g);
        }
        ImageView listIcon = new ImageView(this);
        listIcon.setId(R.id.listicon);
        listIcon.setImageResource(R.drawable.list);
        listIcon.setContentDescription("Courbes");
        RelativeLayout.LayoutParams listIconParams = new RelativeLayout.LayoutParams(-2, -2);
        listIconParams.addRule(10);
        listIconParams.addRule(11);
        listIcon.setOnClickListener(this);
        int pad = Utils.dipToPixel(12, this);
        listIcon.setPadding(pad, pad, pad, pad);
        listIcon.setBackgroundResource(android.R.drawable.list_selector_background);
        layout.addView(charts, new RelativeLayout.LayoutParams(-1, -1));
        layout.addView(listIcon, listIconParams);
        addContentView(layout, new ViewGroup.LayoutParams(-1, -1));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void invalidate(int position) {
        if (this.mGraphViews[position] != null) {
            this.mGraphViews[position].setVisibility(this.mGraphState[position] ? View.VISIBLE : View.GONE);
        }
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        if (v.getId() == R.id.listicon) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Données à afficher");
            builder.setMultiChoiceItems(this.mLegends, this.mGraphState, new DialogInterface.OnMultiChoiceClickListener() {
                @Override // android.content.DialogInterface.OnMultiChoiceClickListener
                public void onClick(DialogInterface dialogInterface, int item, boolean state) {
                    Graphs.this.mGraphState[item] = state;
                    Graphs.this.invalidate(item);
                }
            });
            builder.create().show();
        }
    }
}
