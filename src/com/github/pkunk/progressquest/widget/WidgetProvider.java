package com.github.pkunk.progressquest.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.github.pkunk.progressquest.R;
import com.github.pkunk.progressquest.service.GameplayService;
import com.github.pkunk.progressquest.ui.PhoneGameplayActivity;

/**
 * User: pkunk
 * Date: 2012-02-16
 */
public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Start service
        Intent serviceIntent = new Intent(context, GameplayService.class);
        context.startService(serviceIntent);

        // Set OnClickListener
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        Intent activityIntent = new Intent(context, PhoneGameplayActivity.class);
        activityIntent.setAction(Intent.ACTION_MAIN);
        PendingIntent actionPendingIntent = PendingIntent.getActivity(context, 0, activityIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.wg_main, actionPendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        final Intent intent = new Intent(context, GameplayService.class);
        context.stopService(intent);
        super.onDeleted(context, appWidgetIds);
    }
}
