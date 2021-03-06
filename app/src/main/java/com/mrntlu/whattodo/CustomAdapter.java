package com.mrntlu.whattodo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.mrntlu.whattodo.Models.Categories;
import com.mrntlu.whattodo.Models.TodoItems;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;

public class CustomAdapter extends BaseAdapter {
    Context context;
    OrderedRealmCollection<Categories> categoriesRealm;
    OrderedRealmCollection<TodoItems> todoItems;
    Activity activity;
    int colorHex;
    int resource;
    Realm myRealm;

    public CustomAdapter(Context context, OrderedRealmCollection<Categories> categoriesRealm, Activity activity,int resource) {
        this.context = context;
        this.categoriesRealm = categoriesRealm;
        this.activity = activity;
        this.resource=resource;
    }

    public CustomAdapter(Context context, OrderedRealmCollection<TodoItems> todoItems, Activity activity, int colorHex,int resource,Realm myRealm) {
        this.context = context;
        this.todoItems = todoItems;
        this.activity = activity;
        this.colorHex = colorHex;
        this.resource=resource;
        this.myRealm=myRealm;
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
            View myView = activity.getLayoutInflater().inflate(resource, null);
            setViewLayouts(myView,i);

            return myView;
        }else{
            setViewLayouts(view,i);
            return view;
        }
    }

    private void setViewLayouts(View view, final int position){
        ConstraintLayout constraintLayout = view.findViewById(R.id.constraintLayout);
        if (todoItems==null) {
            constraintLayout.setBackgroundColor(ContextCompat.getColor(context, categoriesRealm.get(position).getColorID()));

            TextView textView = (TextView) view.findViewById(R.id.myText);
            textView.setText(categoriesRealm.get(position).getCategory());
        }else if (categoriesRealm==null){
            constraintLayout.setBackgroundColor(ContextCompat.getColor(context, colorHex));
            CheckBox checkBox=view.findViewById(R.id.checkBox);
            final TextView textView = (TextView) view.findViewById(R.id.myText);

            if (todoItems.get(position).isChecked()){
                checkBox.setChecked(true);
                overlineController(true,textView);
            }

            textView.setText(todoItems.get(position).getTodo());
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    overlineController(b,textView);
                    setTodoChecked(b,position);

                }
            });
        }
    }

    private void overlineController(boolean b,TextView textView){
        if (b){
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            textView.setTextColor(ContextCompat.getColor(context,R.color.darkwhite));
        }else{
            textView.setPaintFlags(textView.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
            textView.setTextColor(ContextCompat.getColor(context,R.color.white));
        }
    }

    private void setTodoChecked(final boolean checked, final int position){
        myRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                TodoItems todoItem = todoItems.get(position);
                todoItem.setChecked(checked);
            }
        });
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
