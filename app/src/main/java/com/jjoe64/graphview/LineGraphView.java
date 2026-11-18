package com.jjoe64.graphview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

/* JADX INFO: loaded from: classes.dex */
public class LineGraphView extends GraphView {
    private boolean drawBackground;
    private final Paint paintBackground;

    public LineGraphView(Context context, String title) {
        super(context, title);
        this.paintBackground = new Paint();
        this.paintBackground.setARGB(255, 20, 40, 60);
        this.paintBackground.setStrokeWidth(4.0f);
    }

    @Override // com.jjoe64.graphview.GraphView
    public void drawSeries(Canvas canvas, GraphView.GraphViewData[] values, float graphwidth, float graphheight, float border, double minX, double minY, double diffX, double diffY, float horstart) {
        double lastEndY = 0.0d;
        double lastEndX = 0.0d;
        if (this.drawBackground) {
            float startY = graphheight + border;
            for (int i = 0; i < values.length; i++) {
                double valY = values[i].valueY - minY;
                double ratY = valY / diffY;
                double valX = values[i].valueX - minX;
                double ratX = valX / diffX;
                float endX = ((float) (((double) graphwidth) * ratX)) + 1.0f + horstart;
                float endY = ((float) (((double) border) - (((double) graphheight) * ratY))) + graphheight + 2.0f;
                if (i > 0) {
                    int numSpace = ((int) ((((double) endX) - lastEndX) / 3.0d)) + 1;
                    for (int xi = 0; xi < numSpace; xi++) {
                        float spaceX = (float) ((((((double) endX) - lastEndX) * ((double) xi)) / ((double) (numSpace - 1))) + lastEndX);
                        float spaceY = (float) ((((((double) endY) - lastEndY) * ((double) xi)) / ((double) (numSpace - 1))) + lastEndY);
                        if (spaceX - horstart > 1.0f) {
                            canvas.drawLine(spaceX, startY, spaceX, spaceY, this.paintBackground);
                        }
                    }
                }
                lastEndY = endY;
                lastEndX = endX;
            }
        }
        double lastEndY2 = 0.0d;
        double lastEndX2 = 0.0d;
        for (int i2 = 0; i2 < values.length; i2++) {
            double valY2 = values[i2].valueY - minY;
            double ratY2 = valY2 / diffY;
            double y = ((double) graphheight) * ratY2;
            double valX2 = values[i2].valueX - minX;
            double ratX2 = valX2 / diffX;
            double x = ((double) graphwidth) * ratX2;
            if (i2 > 0) {
                float startX = ((float) lastEndX2) + 1.0f + horstart;
                float startY2 = ((float) (((double) border) - lastEndY2)) + graphheight;
                canvas.drawLine(startX, startY2, ((float) x) + 1.0f + horstart, ((float) (((double) border) - y)) + graphheight, this.paint);
            }
            lastEndY2 = y;
            lastEndX2 = x;
        }
    }

    public boolean getDrawBackground() {
        return this.drawBackground;
    }

    public void setDrawBackground(boolean drawBackground) {
        this.drawBackground = drawBackground;
    }
}
