package com.github.pkunk.progressquest.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import com.github.pkunk.progressquest.service.GameplayService;

/**
 * User: pkunk
 * Date: 2012-02-16
 */
public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final Intent intent = new Intent(context, GameplayService.class);
        context.startService(intent);
    }
}
