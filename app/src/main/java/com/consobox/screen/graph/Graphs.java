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
    private GraphView mGraphView;
    private GraphView.GraphViewSeries[] mSeries;
    private int mVoiture;
    private boolean[] mGraphState = {true, true, true};
    private final CharSequence[] mLegends = {"Consommation", "Prix/litre", "Dépenses"};

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
        Cursor c = sql.query("conso", new String[]{"date", "km", "volume", "prix"}, "partiel='false' AND voiture=" + this.mVoiture, null, null, null, "date", null);
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
        Cursor c2 = sql.query("depenses", new String[]{"date", "sum(prix) as prix"}, "voiture=" + this.mVoiture, null, "date", null, "date", null);
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
        this.mSeries[0] = new GraphView.GraphViewSeries("Consommation", Integer.valueOf(Color.rgb(250, 90, 0)), datasConso);
        this.mSeries[1] = new GraphView.GraphViewSeries("Prix/litre", Integer.valueOf(Color.rgb(0, 90, 250)), datasPrix);
        this.mSeries[2] = new GraphView.GraphViewSeries("Dépenses", Integer.valueOf(Color.rgb(90, 250, 0)), depensesPrix);
        this.mGraphView = new LineGraphView(this, "ConsoBox");
        this.mGraphView.addSeries(this.mSeries[0]);
        this.mGraphView.addSeries(this.mSeries[1]);
        this.mGraphView.addSeries(this.mSeries[2]);
        this.mGraphView.baseDate = timeZero;
        this.mGraphView.setViewPort(0.0d, time - timeZero);
        this.mGraphView.setScrollable(true);
        this.mGraphView.setScalable(true);
        this.mGraphView.setShowLegend(true);
        this.mGraphView.setOnClickListener(this);
        ImageView listIcon = new ImageView(this);
        listIcon.setId(R.id.listicon);
        listIcon.setImageResource(R.drawable.list);
        RelativeLayout.LayoutParams listIconParams = new RelativeLayout.LayoutParams(-2, -2);
        listIconParams.addRule(10);
        listIconParams.addRule(11);
        listIcon.setOnClickListener(this);
        layout.addView(this.mGraphView);
        layout.addView(listIcon, listIconParams);
        addContentView(layout, new ViewGroup.LayoutParams(-1, -1));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void invalidate(int position) {
        if (this.mGraphState[position]) {
            this.mGraphView.addSeries(this.mSeries[position]);
        } else {
            this.mGraphView.removeSeries(this.mSeries[position]);
        }
        this.mGraphView.invalidate();
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
        if (v.getId() == R.id.listicon) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Données à afficher");
            builder.setMultiChoiceItems(this.mLegends, this.mGraphState, new DialogInterface.OnMultiChoiceClickListener() { // from class: com.consobox.screen.graph.Graphs.1
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
