package com.mrntlu.whattodo.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.mrntlu.whattodo.MainActivity;
import com.mrntlu.whattodo.R;

/**
 * Implementation of App widget functionality.
 */
public class CategoryListsWidget extends AppWidgetProvider {
    public static String EXTRA_WORD="com.mrntlu.whattodo.widget.WORD";
    public static String UPDATE_LIST = "com.mrntlu.whattodo.widget.UPDATE_LIST";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {


        Intent svcIntent=new Intent(context,WidgetService.class);
        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,appWidgetId);

        RemoteViews widget=new RemoteViews(context.getPackageName(),R.layout.category_lists_widget);
        widget.setRemoteAdapter(appWidgetId,R.id.widgetRV,svcIntent);

        Intent clickIntent=new Intent(context,CategoryListsWidget.class);
        clickIntent.setAction(UPDATE_LIST);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,appWidgetId);
        PendingIntent clickPI=PendingIntent.getBroadcast(context,0,clickIntent,0);
        widget.setOnClickPendingIntent(R.id.widgetButton, clickPI );

        Intent intent = new Intent(UPDATE_LIST);
        PendingIntent piOpen = PendingIntent.getBroadcast(context, 0, intent, 0);
        widget.setPendingIntentTemplate(R.id.widgetRV,piOpen);

        appWidgetManager.updateAppWidget(appWidgetId,widget);
        //appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,R.id.widgetRV);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
//        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context,appWidgetManager,appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equalsIgnoreCase(UPDATE_LIST)){
            updateWidget(context);
        }
        super.onReceive(context, intent);
    }

    private void updateWidget(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, CategoryListsWidget.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widgetRV);
    }
}