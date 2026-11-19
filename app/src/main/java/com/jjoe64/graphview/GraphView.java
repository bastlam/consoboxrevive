package com.jjoe64.graphview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import com.jjoe64.graphview.compatible.ScaleGestureDetector;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public abstract class GraphView extends LinearLayout {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$jjoe64$graphview$GraphView$LegendAlign;
    public long baseDate;
    private final List<GraphViewSeries> graphSeries;
    private String[] horlabels;
    private LegendAlign legendAlign;
    private float legendWidth;
    private double manualMaxYValue;
    private double manualMinYValue;
    private boolean manualYAxis;
    private NumberFormat numberformatter;
    protected final Paint paint;
    /** Text colour of labels and legend (follows the app theme). */
    public int labelColor = 0xFF161D1C;
    /** Colour of the grid lines. */
    public int gridColor = 0xFFBEC9C6;
    private boolean scalable;
    private ScaleGestureDetector scaleDetector;
    private boolean scrollable;
    private boolean showLegend;
    private String title;
    private String[] verlabels;
    private final View viewVerLabels;
    private double viewportSize;
    private double viewportStart;

    public enum LegendAlign {
        TOP,
        MIDDLE,
        BOTTOM;

        /* JADX INFO: renamed from: values, reason: to resolve conflict with enum method */
        public static LegendAlign[] valuesCustom() {
            LegendAlign[] legendAlignArrValuesCustom = values();
            int length = legendAlignArrValuesCustom.length;
            LegendAlign[] legendAlignArr = new LegendAlign[length];
            System.arraycopy(legendAlignArrValuesCustom, 0, legendAlignArr, 0, length);
            return legendAlignArr;
        }
    }

    public abstract void drawSeries(Canvas canvas, GraphViewData[] graphViewDataArr, float f, float f2, float f3, double d, double d2, double d3, double d4, float f4);

    private static final class GraphViewConfig {
        static final float BORDER = 20.0f;
        static final float HORIZONTAL_LABEL_HEIGHT = 80.0f;
        static final float VERTICAL_LABEL_WIDTH = 150.0f;

        private GraphViewConfig() {
        }
    }

    static /* synthetic */ int[] $SWITCH_TABLE$com$jjoe64$graphview$GraphView$LegendAlign() {
        int[] iArr = $SWITCH_TABLE$com$jjoe64$graphview$GraphView$LegendAlign;
        if (iArr == null) {
            iArr = new int[LegendAlign.valuesCustom().length];
            try {
                iArr[LegendAlign.BOTTOM.ordinal()] = 3;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[LegendAlign.MIDDLE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[LegendAlign.TOP.ordinal()] = 1;
            } catch (NoSuchFieldError e3) {
            }
            $SWITCH_TABLE$com$jjoe64$graphview$GraphView$LegendAlign = iArr;
        }
        return iArr;
    }

    private class GraphViewContentView extends View {
        private float graphwidth;
        private float lastTouchEventX;

        public GraphViewContentView(Context context) {
            super(context);
            setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            GraphView.this.paint.setStrokeWidth(0.0f);
            float height = getHeight();
            float width = getWidth() - 1;
            double maxY = GraphView.this.getMaxY();
            double minY = GraphView.this.getMinY();
            double diffY = maxY - minY;
            double maxX = GraphView.this.getMaxX(false);
            double minX = GraphView.this.getMinX(false);
            double diffX = maxX - minX;
            float graphheight = height - (2.0f * 20.0f);
            this.graphwidth = width;
            if (GraphView.this.horlabels == null) {
                GraphView.this.horlabels = GraphView.this.generateHorlabels(this.graphwidth);
            }
            if (GraphView.this.verlabels == null) {
                GraphView.this.verlabels = GraphView.this.generateVerlabels(graphheight);
            }
            GraphView.this.paint.setTextAlign(Paint.Align.LEFT);
            int vers = GraphView.this.verlabels.length - 1;
            for (int i = 0; i < GraphView.this.verlabels.length; i++) {
                GraphView.this.paint.setColor(GraphView.this.gridColor);
                float y = ((graphheight / vers) * i) + 20.0f;
                canvas.drawLine(0.0f, y, width, y, GraphView.this.paint);
            }
            int hors = GraphView.this.horlabels.length - 1;
            for (int i2 = 0; i2 < GraphView.this.horlabels.length; i2++) {
                GraphView.this.paint.setColor(GraphView.this.gridColor);
                float x = ((this.graphwidth / hors) * i2) + 0.0f;
                canvas.drawLine(x, height - 20.0f, x, 20.0f, GraphView.this.paint);
                GraphView.this.paint.setTextAlign(Paint.Align.CENTER);
                if (i2 == GraphView.this.horlabels.length - 1) {
                    GraphView.this.paint.setTextAlign(Paint.Align.RIGHT);
                }
                if (i2 == 0) {
                    GraphView.this.paint.setTextAlign(Paint.Align.LEFT);
                }
                GraphView.this.paint.setColor(GraphView.this.labelColor);
                canvas.drawText(GraphView.this.horlabels[i2], x, height - 4.0f, GraphView.this.paint);
            }
            GraphView.this.paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(GraphView.this.title, (this.graphwidth / 2.0f) + 0.0f, 20.0f - 4.0f, GraphView.this.paint);
            if (maxY != minY) {
                GraphView.this.paint.setStrokeCap(Paint.Cap.ROUND);
                GraphView.this.paint.setStrokeWidth(3.0f);
                for (int i3 = 0; i3 < GraphView.this.graphSeries.size(); i3++) {
                    GraphView.this.paint.setColor(((GraphViewSeries) GraphView.this.graphSeries.get(i3)).color);
                    GraphView.this.drawSeries(canvas, GraphView.this._values(i3), this.graphwidth, graphheight, 20.0f, minX, minY, diffX, diffY, 0.0f);
                }
                if (GraphView.this.showLegend) {
                    GraphView.this.drawLegend(canvas, height, width);
                }
            }
        }

        private void onMoveGesture(float f) {
            if (GraphView.this.viewportSize != 0.0d) {
                GraphView.this.viewportStart -= (((double) f) * GraphView.this.viewportSize) / ((double) this.graphwidth);
                double minX = GraphView.this.getMinX(true);
                double maxX = GraphView.this.getMaxX(true);
                if (GraphView.this.viewportStart < minX) {
                    GraphView.this.viewportStart = minX;
                } else if (GraphView.this.viewportStart + GraphView.this.viewportSize > maxX) {
                    GraphView.this.viewportStart = maxX - GraphView.this.viewportSize;
                }
                GraphView.this.horlabels = null;
                GraphView.this.verlabels = null;
                GraphView.this.viewVerLabels.invalidate();
            }
            invalidate();
        }

        @Override // android.view.View
        public boolean onTouchEvent(MotionEvent event) {
            if (!GraphView.this.isScrollable()) {
                return super.onTouchEvent(event);
            }
            boolean handled = false;
            if (GraphView.this.scalable && GraphView.this.scaleDetector != null) {
                GraphView.this.scaleDetector.onTouchEvent(event);
                handled = GraphView.this.scaleDetector.isInProgress();
            }
            if (!handled) {
                event.getAction();
                boolean handled2 = true;
                if ((event.getAction() & 1) == 1) {
                    this.lastTouchEventX = 0.0f;
                    handled2 = true;
                }
                if ((event.getAction() & 2) == 2) {
                    if (this.lastTouchEventX != 0.0f) {
                        onMoveGesture(event.getX() - this.lastTouchEventX);
                    }
                    this.lastTouchEventX = event.getX();
                    return true;
                }
                return handled2;
            }
            return handled;
        }
    }

    public static class GraphViewData {
        public final double valueX;
        public final double valueY;

        public GraphViewData(double valueX, double valueY) {
            this.valueX = valueX;
            this.valueY = valueY;
        }
    }

    public static class GraphViewSeries {
        final int color;
        final String description;
        final GraphViewData[] values;

        public GraphViewSeries(GraphViewData[] values) {
            this.description = null;
            this.color = -16746548;
            this.values = values;
        }

        public GraphViewSeries(String description, Integer color, GraphViewData[] values) {
            this.description = description;
            this.color = color == null ? -16746548 : color.intValue();
            this.values = values;
        }
    }

    private class VerLabelsView extends View {
        public VerLabelsView(Context context) {
            super(context);
            setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 10.0f));
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            GraphView.this.paint.setStrokeWidth(0.0f);
            float height = getHeight();
            float graphheight = height - (2.0f * 20.0f);
            if (GraphView.this.verlabels == null) {
                GraphView.this.verlabels = GraphView.this.generateVerlabels(graphheight);
            }
            GraphView.this.paint.setTextAlign(Paint.Align.LEFT);
            int vers = GraphView.this.verlabels.length - 1;
            for (int i = 0; i < GraphView.this.verlabels.length; i++) {
                float y = ((graphheight / vers) * i) + 20.0f;
                GraphView.this.paint.setColor(GraphView.this.labelColor);
                canvas.drawText(GraphView.this.verlabels[i], 0.0f, y, GraphView.this.paint);
            }
        }
    }

    public GraphView(Context context, String title) {
        super(context);
        this.showLegend = false;
        this.legendWidth = 120.0f;
        this.legendAlign = LegendAlign.MIDDLE;
        setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        if (title != null) {
            this.title = title;
        }
        this.paint = new Paint();
        this.graphSeries = new ArrayList();
        this.viewVerLabels = new VerLabelsView(context);
        addView(this.viewVerLabels);
        addView(new GraphViewContentView(context), new LinearLayout.LayoutParams(-1, -1, 1.0f));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public GraphViewData[] _values(int idxSeries) {
        GraphViewData[] values = this.graphSeries.get(idxSeries).values;
        if (this.viewportStart != 0.0d || this.viewportSize != 0.0d) {
            List<GraphViewData> listData = new ArrayList<>();
            for (int i = 0; i < values.length; i++) {
                if (values[i].valueX >= this.viewportStart) {
                    if (values[i].valueX > this.viewportStart + this.viewportSize) {
                        listData.add(values[i]);
                        break;
                    }
                    listData.add(values[i]);
                } else {
                    if (listData.isEmpty()) {
                        listData.add(values[i]);
                    }
                    listData.set(0, values[i]);
                }
            }
            return (GraphViewData[]) listData.toArray(new GraphViewData[listData.size()]);
        }
        return values;
    }

    public void addSeries(GraphViewSeries series) {
        this.graphSeries.add(series);
    }

    public void removeSeries(int index) {
        if (index < 0 || index >= this.graphSeries.size()) {
            throw new IndexOutOfBoundsException("No series at index " + index);
        }
        this.graphSeries.remove(index);
    }

    public void removeSeries(GraphViewSeries series) {
        this.graphSeries.remove(series);
    }

    protected void drawLegend(Canvas canvas, float height, float width) {
        float lTop;
        this.paint.setARGB(180, 100, 100, 100);
        float legendHeight = (this.graphSeries.size() * 20) + 5;
        float lLeft = (width - this.legendWidth) - 10.0f;
        switch ($SWITCH_TABLE$com$jjoe64$graphview$GraphView$LegendAlign()[this.legendAlign.ordinal()]) {
            case 1:
                lTop = 10.0f;
                break;
            case 2:
                lTop = (height / 2.0f) - (legendHeight / 2.0f);
                break;
            default:
                lTop = ((height - 20.0f) - legendHeight) - 10.0f;
                break;
        }
        float lRight = lLeft + this.legendWidth;
        float lBottom = lTop + legendHeight;
        canvas.drawRoundRect(new RectF(lLeft, lTop, lRight, lBottom), 8.0f, 8.0f, this.paint);
        for (int i = 0; i < this.graphSeries.size(); i++) {
            this.paint.setColor(this.graphSeries.get(i).color);
            canvas.drawRect(new RectF(5.0f + lLeft, 5.0f + lTop + (i * 20), 5.0f + lLeft + 15, ((i + 1) * 20) + lTop), this.paint);
            if (this.graphSeries.get(i).description != null) {
                this.paint.setColor(this.labelColor);
                this.paint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(this.graphSeries.get(i).description, 5.0f + lLeft + 15 + 5.0f, 15 + lTop + (i * 20), this.paint);
            }
        }
    }

    protected String formatLabel(double value, boolean isValueX) {
        if (this.numberformatter == null) {
            this.numberformatter = NumberFormat.getNumberInstance();
            double highestvalue = getMaxY();
            double lowestvalue = getMinY();
            if (highestvalue - lowestvalue < 0.1d) {
                this.numberformatter.setMaximumFractionDigits(6);
            } else if (highestvalue - lowestvalue < 1.0d) {
                this.numberformatter.setMaximumFractionDigits(4);
            } else if (highestvalue - lowestvalue < 20.0d) {
                this.numberformatter.setMaximumFractionDigits(3);
            } else if (highestvalue - lowestvalue < 100.0d) {
                this.numberformatter.setMaximumFractionDigits(1);
            } else {
                this.numberformatter.setMaximumFractionDigits(0);
            }
        }
        return this.numberformatter.format(value);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String[] generateHorlabels(float graphwidth) {
        int numLabels = (int) (graphwidth / 150.0f);
        String[] labels = new String[numLabels + 1];
        double min = getMinX(false);
        double max = getMaxX(false);
        for (int i = 0; i <= numLabels; i++) {
            long date = ((long) (this.baseDate + min + (((max - min) * ((double) i)) / ((double) numLabels)))) * 86400000;
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(date);
            labels[i] = String.valueOf(c.get(5) + 1) + "/" + (c.get(2) + 1) + "/" + c.get(1);
        }
        return labels;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized String[] generateVerlabels(float graphheight) {
        String[] labels;
        int numLabels = (int) (graphheight / 80.0f);
        labels = new String[numLabels + 1];
        double min = getMinY();
        double max = getMaxY();
        for (int i = 0; i <= numLabels; i++) {
            labels[numLabels - i] = formatLabel((((max - min) * ((double) i)) / ((double) numLabels)) + min, false);
        }
        return labels;
    }

    public LegendAlign getLegendAlign() {
        return this.legendAlign;
    }

    public float getLegendWidth() {
        return this.legendWidth;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public double getMaxX(boolean ignoreViewport) {
        if (!ignoreViewport && this.viewportSize != 0.0d) {
            return this.viewportStart + this.viewportSize;
        }
        if (this.graphSeries.size() <= 0) {
            return 0.0d;
        }
        GraphViewData[] values = this.graphSeries.get(0).values;
        double highest = values[values.length - 1].valueX;
        for (int i = 1; i < this.graphSeries.size(); i++) {
            GraphViewData[] values2 = this.graphSeries.get(i).values;
            highest = Math.max(highest, values2[values2.length - 1].valueX);
        }
        return highest;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public double getMaxY() {
        if (this.manualYAxis) {
            double largest = this.manualMaxYValue;
            return largest;
        }
        double largest2 = -2.147483648E9d;
        for (int i = 0; i < this.graphSeries.size(); i++) {
            GraphViewData[] values = _values(i);
            for (int ii = 0; ii < values.length; ii++) {
                if (values[ii].valueY > largest2) {
                    largest2 = values[ii].valueY;
                }
            }
        }
        return largest2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public double getMinX(boolean ignoreViewport) {
        if (!ignoreViewport && this.viewportSize != 0.0d) {
            return this.viewportStart;
        }
        if (this.graphSeries.size() <= 0) {
            return 0.0d;
        }
        GraphViewData[] values = this.graphSeries.get(0).values;
        double lowest = values[0].valueX;
        for (int i = 1; i < this.graphSeries.size(); i++) {
            GraphViewData[] values2 = this.graphSeries.get(i).values;
            lowest = Math.min(lowest, values2[0].valueX);
        }
        return lowest;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public double getMinY() {
        if (this.manualYAxis) {
            double smallest = this.manualMinYValue;
            return smallest;
        }
        double smallest2 = 2.147483647E9d;
        for (int i = 0; i < this.graphSeries.size(); i++) {
            GraphViewData[] values = _values(i);
            for (int ii = 0; ii < values.length; ii++) {
                if (values[ii].valueY < smallest2) {
                    smallest2 = values[ii].valueY;
                }
            }
        }
        return smallest2;
    }

    public boolean isScrollable() {
        return this.scrollable;
    }

    public boolean isShowLegend() {
        return this.showLegend;
    }

    public void setHorizontalLabels(String[] horlabels) {
        this.horlabels = horlabels;
    }

    public void setLegendAlign(LegendAlign legendAlign) {
        this.legendAlign = legendAlign;
    }

    public void setLegendWidth(float legendWidth) {
        this.legendWidth = legendWidth;
    }

    public void setManualYAxis(boolean manualYAxis) {
        this.manualYAxis = manualYAxis;
    }

    public void setManualYAxisBounds(double max, double min) {
        this.manualMaxYValue = max;
        this.manualMinYValue = min;
        this.manualYAxis = true;
    }

    public synchronized void setScalable(boolean scalable) {
        this.scalable = scalable;
        if (scalable && this.scaleDetector == null) {
            this.scrollable = true;
            this.scaleDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() { // from class: com.jjoe64.graphview.GraphView.1
                @Override // com.jjoe64.graphview.compatible.ScaleGestureDetector.SimpleOnScaleGestureListener
                public boolean onScale(ScaleGestureDetector detector) {
                    double newSize = GraphView.this.viewportSize * detector.getScaleFactor();
                    double diff = newSize - GraphView.this.viewportSize;
                    GraphView.this.viewportStart += diff / 2.0d;
                    GraphView.this.viewportSize -= diff;
                    if (diff < 0.0d) {
                        double minX = GraphView.this.getMinX(true);
                        if (GraphView.this.viewportStart < minX) {
                            GraphView.this.viewportStart = minX;
                        }
                        double maxX = GraphView.this.getMaxX(true);
                        double overlap = (GraphView.this.viewportStart + GraphView.this.viewportSize) - maxX;
                        if (overlap > 0.0d) {
                            if (GraphView.this.viewportStart - overlap <= minX) {
                                GraphView.this.viewportStart = minX;
                                GraphView.this.viewportSize = maxX - GraphView.this.viewportStart;
                            } else {
                                GraphView.this.viewportStart -= overlap;
                            }
                        }
                    }
                    GraphView.this.verlabels = null;
                    GraphView.this.horlabels = null;
                    GraphView.this.numberformatter = null;
                    GraphView.this.invalidate();
                    GraphView.this.viewVerLabels.invalidate();
                    return true;
                }
            });
        }
    }

    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }

    public void setShowLegend(boolean showLegend) {
        this.showLegend = showLegend;
    }

    public void setVerticalLabels(String[] verlabels) {
        this.verlabels = verlabels;
    }

    public void setViewPort(double start, double size) {
        this.viewportStart = start;
        this.viewportSize = size;
    }
}
