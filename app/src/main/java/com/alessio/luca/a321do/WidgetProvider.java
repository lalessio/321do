package com.alessio.luca.a321do;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Random;

/**
 * Created by Luca on 02/11/2016.
 */

public class WidgetProvider extends AppWidgetProvider {
    private static final String QUICK_NOTE_ACTION = "com.alessio.luca.a321do.QUICK_NOTE_ACTION";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            Intent intent = new Intent(QUICK_NOTE_ACTION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.buttonWidget, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (QUICK_NOTE_ACTION.equals(intent.getAction())) {
            Intent intent1 = new Intent(context, NewNoteActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
        }
    }
}
