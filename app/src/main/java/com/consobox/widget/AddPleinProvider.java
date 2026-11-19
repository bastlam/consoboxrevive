package com.consobox.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.consobox.R;

/* JADX INFO: loaded from: classes.dex */
public class AddPleinProvider extends AppWidgetProvider {
    @Override // android.appwidget.AppWidgetProvider
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, (Class<?>) AddPleinIntent.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_addplein);
            views.setOnClickPendingIntent(R.id.addplein, pendingIntent);
            views.setCharSequence(R.id.widgetaddplein_label, "setText", context.getString(R.string.app_name));
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override // android.appwidget.AppWidgetProvider
    public void onEnabled(Context context) {
        System.err.println("enable");
    }

    @Override // android.appwidget.AppWidgetProvider
    public void onDeleted(Context context, int[] appWidgetIds) {
        System.err.println("delete");
    }
}
