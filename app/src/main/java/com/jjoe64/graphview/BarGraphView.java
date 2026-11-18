package com.jjoe64.graphview;

import android.content.Context;
import android.graphics.Canvas;

/* JADX INFO: loaded from: classes.dex */
public class BarGraphView extends GraphView {
    public BarGraphView(Context context, String title) {
        super(context, title);
    }

    @Override // com.jjoe64.graphview.GraphView
    public void drawSeries(Canvas canvas, GraphView.GraphViewData[] values, float graphwidth, float graphheight, float border, double minX, double minY, double diffX, double diffY, float horstart) {
        float colwidth = (graphwidth - (2.0f * border)) / values.length;
        for (int i = 0; i < values.length; i++) {
            float valY = (float) (values[i].valueY - minY);
            float ratY = (float) (((double) valY) / diffY);
            float y = graphheight * ratY;
            canvas.drawRect((i * colwidth) + horstart, (border - y) + graphheight, (colwidth - 1.0f) + (i * colwidth) + horstart, (graphheight + border) - 1.0f, this.paint);
        }
    }
}
