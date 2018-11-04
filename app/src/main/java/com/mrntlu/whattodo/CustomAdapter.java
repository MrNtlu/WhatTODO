package com.mrntlu.whattodo;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mrntlu.whattodo.Models.Categories;
import com.mrntlu.whattodo.Models.TodoItems;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import io.realm.OrderedRealmCollection;

public class CustomAdapter extends BaseAdapter {
    Context context;
    OrderedRealmCollection<Categories> categoriesRealm;
    OrderedRealmCollection<TodoItems> todoItems;
    Activity activity;
    int colorHex;

    public CustomAdapter(Context context, OrderedRealmCollection<Categories> categoriesRealm, Activity activity) {
        this.context = context;
        this.categoriesRealm = categoriesRealm;
        this.activity = activity;
    }

    public CustomAdapter(Context context, OrderedRealmCollection<TodoItems> todoItems, Activity activity, int colorHex) {
        this.context = context;
        this.todoItems = todoItems;
        this.activity = activity;
        this.colorHex = colorHex;
    }

    @Override
    public int getCount() {
        if (categoriesRealm==null){
            return todoItems.size();
        }else if(todoItems==null){
            return categoriesRealm.size();
        }
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view==null) {
            View myView = activity.getLayoutInflater().inflate(R.layout.categories_layout, null);
            setViewLayouts(myView,i);

            return myView;
        }else{
            setViewLayouts(view,i);
            return view;
        }
    }

    private void setViewLayouts(View view,int position){
        ConstraintLayout constraintLayout = view.findViewById(R.id.constraintLayout);
        if (todoItems==null) {
            constraintLayout.setBackgroundColor(ContextCompat.getColor(context, categoriesRealm.get(position).getColorID()));

            TextView textView = (TextView) view.findViewById(R.id.myText);
            textView.setText(categoriesRealm.get(position).getCategory());
        }else if (categoriesRealm==null){
            constraintLayout.setBackgroundColor(ContextCompat.getColor(context, colorHex));

            TextView textView = (TextView) view.findViewById(R.id.myText);
            textView.setText(todoItems.get(position).getTodo());
        }
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }
}
