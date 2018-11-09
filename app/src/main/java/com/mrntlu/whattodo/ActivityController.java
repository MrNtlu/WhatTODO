package com.mrntlu.whattodo;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.mrntlu.whattodo.Models.Categories;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import es.dmoral.prefs.Prefs;
import es.dmoral.toasty.Toasty;
import io.realm.Realm;

public class ActivityController{

    Context context;
    SwipeMenuListView listView;
    CustomAdapter customAdapter;
    Realm myRealm;

    public ActivityController(Context context, SwipeMenuListView listView, CustomAdapter customAdapter, Realm myRealm) {
        this.context = context;
        this.listView = listView;
        this.customAdapter = customAdapter;
        this.myRealm = myRealm;
    }

    public static void initPref(Context context,String key){
        if (Prefs.with(context).readInt(key)==-1) Prefs.with(context).writeInt(key,0);

    }

    public void setMenu(){
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem editItem = new SwipeMenuItem(
                        context.getApplicationContext());
                editItem.setBackground(new ColorDrawable(Color.parseColor("#FDD835")));
                editItem.setWidth(175);
                editItem.setIcon(R.drawable.ic_edit_black_24dp);
                menu.addMenuItem(editItem);

                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        context.getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.parseColor("#F4511E")));
                deleteItem.setWidth(175);
                deleteItem.setIcon(R.drawable.ic_delete_black_24dp);
                menu.addMenuItem(deleteItem);
            }
        };

        listView.setMenuCreator(creator);
        listView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
    }

    void setSortMenuItemIcon(int drawableID,Menu menu){
        menu.getItem(0).setIcon(ContextCompat.getDrawable(context,drawableID));
    }

    boolean controlUniqueCategoryName(String category,String categoryName){
        if(myRealm.where(Categories.class).equalTo(category,categoryName).findFirst()!=null){
            return false;
        }
        return true;
    }
}
