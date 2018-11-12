package com.mrntlu.whattodo.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.mrntlu.whattodo.Models.Categories;
import com.mrntlu.whattodo.R;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class WidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    private int appWidgetID;

    RealmResults<Categories> realmResults;
    Realm myRealm;
    List<Categories> list;

    public WidgetRemoteViewsFactory(Context context,Intent intent) {
        this.context = context;
        this.appWidgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
        Realm.init(context);

    }

    void getData(){
        myRealm=Realm.getDefaultInstance();
        list=myRealm.copyFromRealm(myRealm.where(Categories.class).findAll());
        myRealm.close();
    }

    @Override
    public void onCreate() {
        //realmResults=myRealm.where(Categories.class).findAll();
        getData();
    }

    @Override
    public void onDataSetChanged() {
        getData();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews row=new RemoteViews(context.getPackageName(), R.layout.widget_row);
        row.setTextViewText(R.id.rowText,list.get(i).getCategory());

        Intent intent=new Intent();
        Bundle extras=new Bundle();

        extras.putString(CategoryListsWidget.EXTRA_WORD,list.get(i).getCategory());
        intent.putExtras(extras);
        row.setOnClickFillInIntent(R.id.rowText,intent);

        return row;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
