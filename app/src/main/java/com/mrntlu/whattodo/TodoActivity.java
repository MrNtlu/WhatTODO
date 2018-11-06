package com.mrntlu.whattodo;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.mrntlu.whattodo.Models.Categories;
import com.mrntlu.whattodo.Models.TodoItems;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.prefs.Prefs;
import es.dmoral.toasty.Toasty;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

public class TodoActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)Toolbar toolbar;
    @BindView(R.id.todo_recycler)SwipeMenuListView todoRV;

    Realm myRealm;
    RealmList<TodoItems> todoItems;

    String titleName;
    int colorHex;
    CustomAdapter customAdapter;
    Menu menu;
    ActivityController activityCont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);
        ButterKnife.bind(this);
        Realm.init(this);
        myRealm=Realm.getDefaultInstance();
        titleName=getIntent().getStringExtra("TITLE_NAME");
        colorHex=getIntent().getIntExtra("TOOLBAR_COLOR",R.color.colorPrimaryDark);

        todoItems=myRealm.where(Categories.class).equalTo("category",titleName).findFirst().getTodoItems();

        customAdapter=new CustomAdapter(this,todoItems,this,colorHex,R.layout.todo_layout);
        activityCont=new ActivityController(TodoActivity.this,todoRV,customAdapter,myRealm);
        setSupportActionBar(toolbar);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        activityCont.setMenu();

        todoRV.setAdapter(customAdapter);

        window.setStatusBarColor(ContextCompat.getColor(this,colorHex));

        setActionBar();

        todoRV.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                final TodoItems todoItem = todoItems.get(position);
                switch (index){
                    case 0:
                        myRealm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                try{
                                    addDialog(1,position);
                                }catch (Exception e){
                                    Toasty.error(TodoActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        });
                        break;
                    case 1:
                        myRealm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                try {
                                    todoItem.deleteFromRealm();
                                    customAdapter.notifyDataSetChanged();
                                    Toasty.success(TodoActivity.this,"Succesfully Deleted.",Toast.LENGTH_SHORT).show();
                                }catch (Exception e){
                                    Toasty.error(TodoActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        });
                        break;
                }
                return false;
            }
        });
    }

    private void setActionBar(){
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this,colorHex)));
        getSupportActionBar().setTitle(titleName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setElevation(3);
    }

    void addDialog(final int addOrUpdate, final int position){
        final Dialog addDialog = new Dialog(this);
        addDialog.setContentView(R.layout.custom_todo);
        Button addButton = addDialog.findViewById(R.id.addButton);
        final TextView whatTodoText = addDialog.findViewById(R.id.whatToDo);

        if (addOrUpdate == 1) {
            whatTodoText.setText(todoItems.get(position).getTodo());
        }

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String todoString = whatTodoText.getText().toString();
                if (!todoString.trim().isEmpty()) {
                    if (addOrUpdate == 0) {
                        if (activityCont.controlUniqueCategoryName("category",todoString)) {
                            myRealm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    try {
                                        TodoItems todoItem = realm.createObject(TodoItems.class);
                                        todoItem.setTodo(todoString);
                                        todoItems.add(todoItem);
                                        customAdapter.notifyDataSetChanged();
                                        Toasty.success(TodoActivity.this, "Succesfully Added.", Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        Toasty.error(TodoActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }
                                }
                            });
                            addDialog.dismiss();
                        }
                        else {
                            Toasty.error(TodoActivity.this, "Category name must be unique!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        myRealm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                TodoItems todoItem = todoItems.get(position);
                                todoItem.setTodo(todoString);
                                customAdapter.notifyDataSetChanged();
                            }
                        });
                        addDialog.dismiss();
                    }
                }
                else {
                    Toasty.error(TodoActivity.this, "Please enter a category!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        addDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);
        this.menu=menu;
        menu.getItem(0).setEnabled(false);
        menu.getItem(0).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addButton:
                addDialog(0,0);
                break;
            case android.R.id.home:
                startActivity(new Intent(this,MainActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        if(myRealm != null) {
            myRealm.close();
        }
        super.onDestroy();
    }
}
