package com.github.pkunk.progressquest.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
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
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Intent serviceIntent = new Intent(context, GameplayService.class);
        context.startService(serviceIntent);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(thisWidget, remoteViews);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Set OnClickListener
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        Intent intent = new Intent(context, PhoneGameplayActivity.class);
        intent.setPackage(context.getPackageName());
        intent.setAction(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.wg_status1, pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.wg_status2, pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.wg_status3, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    @Override
    public void onDisabled(Context context) {
        Intent intent = new Intent(context, GameplayService.class);
        context.stopService(intent);
        super.onDisabled(context);
    }
}
