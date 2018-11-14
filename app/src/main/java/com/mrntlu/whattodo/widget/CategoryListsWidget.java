package com.mrntlu.whattodo.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.mrntlu.whattodo.MainActivity;
import com.mrntlu.whattodo.R;
import com.mrntlu.whattodo.TodoActivity;

import es.dmoral.toasty.Toasty;

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

        Intent updateIntent=new Intent(context,CategoryListsWidget.class);
        updateIntent.setAction(UPDATE_LIST);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,appWidgetId);
        PendingIntent updatePI=PendingIntent.getBroadcast(context,0,updateIntent,0);
        widget.setOnClickPendingIntent(R.id.widgetButton, updatePI );

        Intent toastIntent = new Intent(context, CategoryListsWidget.class);
        toastIntent.setAction(EXTRA_WORD);
        toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        widget.setPendingIntentTemplate(R.id.widgetRV, toastPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId,widget);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context,appWidgetManager,appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equalsIgnoreCase(UPDATE_LIST)){
            Toasty.success(context,"Refreshed", Toast.LENGTH_SHORT).show();
            updateWidget(context);
        }
        else if (intent.getAction().equals(EXTRA_WORD)){
            String title=intent.getStringExtra("TITLE_NAME");
            int toolbarColor=intent.getIntExtra("TOOLBAR_COLOR",2131034159);
            Intent clickIntent = new Intent(context, TodoActivity.class);
            clickIntent.putExtra("TITLE_NAME", title);
            clickIntent.putExtra("TOOLBAR_COLOR", toolbarColor);
            context.startActivity(clickIntent);
        }
        super.onReceive(context, intent);
    }

    public void updateWidget(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, CategoryListsWidget.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widgetRV);
    }

    public static void updateWidgetFromActivity(Context context){
        new CategoryListsWidget().updateWidget(context);
    }
}