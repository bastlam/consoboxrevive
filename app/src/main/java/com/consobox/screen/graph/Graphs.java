package com.consobox.screen.graph;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import com.consobox.R;
import com.consobox.data.Utils;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LineGraphView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Consumption / price per litre / fuel spending charts over a chosen period
 * (current year by default). One stacked chart per curve, each with its own Y scale;
 * horizontal scroll and zoom are shared.
 */
public class Graphs extends Activity implements View.OnClickListener {
    private static final long DAY_MS = 86400000L;
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private final GraphView[] mGraphViews = new GraphView[3];
    private final GraphView.GraphViewSeries[] mSeries = new GraphView.GraphViewSeries[3];
    private final boolean[] mGraphState = {true, true, true};
    private final CharSequence[] mLegends = {"Consommation (l/100 km)", "Prix au litre (€)", "Dépenses carburant (€)"};
    private int mVoiture;
    /** Selected period, inclusive, as UTC midnight timestamps. */
    private long mFrom;
    private long mTo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_graphs);
        this.mVoiture = getIntent().getIntExtra("voiture", 0);
        if (savedInstanceState != null && savedInstanceState.containsKey("from")) {
            this.mFrom = savedInstanceState.getLong("from");
            this.mTo = savedInstanceState.getLong("to");
        } else {
            setCurrentYear();
        }
        findViewById(R.id.graph_from).setOnClickListener(this);
        findViewById(R.id.graph_to).setOnClickListener(this);
        findViewById(R.id.graph_period).setOnClickListener(this);
        findViewById(R.id.listicon).setOnClickListener(this);
        reload();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("from", this.mFrom);
        outState.putLong("to", this.mTo);
    }

    // ------------------------------------------------------------------ period

    private static Calendar utcCalendar() {
        Calendar c = Calendar.getInstance(UTC);
        c.clear();
        return c;
    }

    private static long utcDate(int year, int month, int day) {
        Calendar c = utcCalendar();
        c.set(year, month, day);
        return c.getTimeInMillis();
    }

    private void setCurrentYear() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        this.mFrom = utcDate(year, Calendar.JANUARY, 1);
        this.mTo = utcDate(year, Calendar.DECEMBER, 31);
    }

    private void setLast12Months() {
        Calendar now = Calendar.getInstance();
        this.mTo = utcDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        Calendar from = utcCalendar();
        from.setTimeInMillis(this.mTo);
        from.add(Calendar.YEAR, -1);
        from.add(Calendar.DAY_OF_MONTH, 1);
        this.mFrom = from.getTimeInMillis();
    }

    private void setWholeHistory() {
        SQLiteDatabase sql = Utils.getDatabase(this);
        Cursor c = sql.rawQuery("SELECT MIN(d), MAX(d) FROM (SELECT date AS d FROM conso WHERE voiture=" + this.mVoiture
                + " UNION ALL SELECT date AS d FROM depenses WHERE type=0 AND voiture=" + this.mVoiture + ")", null);
        try {
            if (c.moveToFirst() && c.getString(0) != null) {
                this.mFrom = parse(c.getString(0));
                this.mTo = parse(c.getString(1));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            c.close();
        }
    }

    private static SimpleDateFormat sqlFormat() {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        f.setTimeZone(UTC);
        return f;
    }

    private static long parse(String date) throws ParseException {
        return sqlFormat().parse(date).getTime();
    }

    private String display(long date) {
        java.text.DateFormat f = android.text.format.DateFormat.getDateFormat(this);
        f.setTimeZone(UTC);
        return f.format(date);
    }

    private void pickDate(final boolean from) {
        Calendar c = utcCalendar();
        c.setTimeInMillis(from ? this.mFrom : this.mTo);
        new DatePickerDialog(this, (view, year, month, day) -> {
            long d = utcDate(year, month, day);
            if (from) {
                this.mFrom = d;
                if (this.mTo < d) {
                    this.mTo = d;
                }
            } else {
                this.mTo = d;
                if (this.mFrom > d) {
                    this.mFrom = d;
                }
            }
            reload();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    // ------------------------------------------------------------------ data + charts

    private void reload() {
        ((Button) findViewById(R.id.graph_from)).setText(display(this.mFrom));
        ((Button) findViewById(R.id.graph_to)).setText(display(this.mTo));

        SimpleDateFormat f = sqlFormat();
        String[] range = {f.format(this.mFrom), f.format(this.mTo)};
        long dayZero = this.mFrom / DAY_MS;
        List<GraphView.GraphViewData> conso = new ArrayList<>();
        List<GraphView.GraphViewData> prix = new ArrayList<>();
        List<GraphView.GraphViewData> depenses = new ArrayList<>();
        SQLiteDatabase sql = Utils.getDatabase(this);
        Cursor c = sql.query("conso", new String[]{"date", "km", "volume", "prix"},
                Utils.SQL_PLEIN_COMPLET + " AND km>0 AND voiture=" + this.mVoiture + " AND date>=? AND date<=?",
                range, null, null, "date");
        while (c.moveToNext()) {
            try {
                double x = parse(c.getString(0)) / DAY_MS - dayZero;
                conso.add(new GraphView.GraphViewData(x, c.getDouble(2) / c.getDouble(1) * 100.0d));
                if (c.getDouble(3) > 0) {
                    prix.add(new GraphView.GraphViewData(x, c.getDouble(3)));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        c.close();
        Cursor c2 = sql.query("depenses", new String[]{"date", "sum(prix)"},
                "type=0 AND voiture=" + this.mVoiture + " AND date>=? AND date<=?", range, "date", null, "date");
        while (c2.moveToNext()) {
            try {
                depenses.add(new GraphView.GraphViewData(parse(c2.getString(0)) / DAY_MS - dayZero, c2.getDouble(1)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        c2.close();

        this.mSeries[0] = new GraphView.GraphViewSeries(null, Color.rgb(0xF2, 0x8C, 0x28), conso.toArray(new GraphView.GraphViewData[0]));
        this.mSeries[1] = new GraphView.GraphViewSeries(null, Color.rgb(0x3B, 0x82, 0xF6), prix.toArray(new GraphView.GraphViewData[0]));
        this.mSeries[2] = new GraphView.GraphViewSeries(null, getColor(R.color.cb_primary), depenses.toArray(new GraphView.GraphViewData[0]));
        buildCharts(dayZero, this.mTo / DAY_MS - dayZero);
    }

    private void buildCharts(long dayZero, long days) {
        LinearLayout charts = findViewById(R.id.graph_charts);
        charts.removeAllViews();
        boolean any = false;
        int margin = Utils.dipToPixel(4, this);
        GraphView.ViewportListener sync = (source, start, size) -> {
            for (GraphView g : this.mGraphViews) {
                if (g != null && g != source) {
                    g.syncViewport(start, size);
                }
            }
        };
        for (int i = 0; i < this.mSeries.length; i++) {
            if (this.mSeries[i].isEmpty()) {
                this.mGraphViews[i] = null;
                continue;
            }
            any = true;
            GraphView g = new LineGraphView(this, this.mLegends[i].toString());
            g.addSeries(this.mSeries[i]);
            g.baseDate = dayZero;
            g.setXBounds(0.0d, Math.max(days, 1));
            g.setViewPort(0.0d, Math.max(days, 1));
            g.setScrollable(true);
            g.setScalable(true);
            g.labelColor = getColor(R.color.cb_on_surface);
            g.gridColor = getColor(R.color.cb_outline);
            g.setViewportListener(sync);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, 0, 1.0f);
            lp.bottomMargin = margin;
            g.setLayoutParams(lp);
            g.setVisibility(this.mGraphState[i] ? View.VISIBLE : View.GONE);
            this.mGraphViews[i] = g;
            charts.addView(g);
        }
        findViewById(R.id.graph_empty).setVisibility(any ? View.GONE : View.VISIBLE);
    }

    private void updateVisibility(int position) {
        if (this.mGraphViews[position] != null) {
            this.mGraphViews[position].setVisibility(this.mGraphState[position] ? View.VISIBLE : View.GONE);
        }
    }

    // ------------------------------------------------------------------ clicks

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.graph_from) {
            pickDate(true);
        } else if (id == R.id.graph_to) {
            pickDate(false);
        } else if (id == R.id.graph_period) {
            CharSequence[] items = {getString(R.string.graph_current_year), getString(R.string.graph_last_12_months),
                    getString(R.string.graph_all)};
            new AlertDialog.Builder(this)
                    .setTitle(R.string.graph_period)
                    .setItems(items, (d, which) -> {
                        if (which == 0) {
                            setCurrentYear();
                        } else if (which == 1) {
                            setLast12Months();
                        } else {
                            setWholeHistory();
                        }
                        reload();
                    })
                    .show();
        } else if (id == R.id.listicon) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.graph_curves)
                    .setMultiChoiceItems(this.mLegends, this.mGraphState, (d, item, state) -> {
                        this.mGraphState[item] = state;
                        updateVisibility(item);
                    })
                    .show();
        }
    }
}
