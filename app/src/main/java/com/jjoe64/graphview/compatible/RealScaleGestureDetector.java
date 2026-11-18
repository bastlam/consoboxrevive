package com.jjoe64.graphview.compatible;

import android.content.Context;

/* JADX INFO: loaded from: classes.dex */
public class RealScaleGestureDetector extends android.view.ScaleGestureDetector {
    public RealScaleGestureDetector(Context context, final ScaleGestureDetector fakeScaleGestureDetector, final ScaleGestureDetector.SimpleOnScaleGestureListener fakeListener) {
        super(context, new android.view.ScaleGestureDetector.SimpleOnScaleGestureListener() { // from class: com.jjoe64.graphview.compatible.RealScaleGestureDetector.1
            @Override // android.view.ScaleGestureDetector.SimpleOnScaleGestureListener, android.view.ScaleGestureDetector.OnScaleGestureListener
            public boolean onScale(android.view.ScaleGestureDetector detector) {
                return fakeListener.onScale(fakeScaleGestureDetector);
            }
        });
    }
}
