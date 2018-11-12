package com.mrntlu.whattodo;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.android.material.snackbar.Snackbar;
import com.mrntlu.whattodo.Models.Categories;
import com.mrntlu.whattodo.Models.TodoItems;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class TodoActivity extends AppCompatActivity{

    @BindView(R.id.toolbar) Toolbar toolbar;

    @BindView(R.id.categories_recycler)RecyclerView todoRV;

    @BindView(R.id.searchView)SearchView searchView;

    @BindView(R.id.searchButton)FloatingActionButton floatingActionButton;

    Realm myRealm;
    RealmList<TodoItems> todoItems;

    String titleName;
    int colorHex;
    Menu menu;
    ActivityController activityCont;
    RecyclerViewAdapter recyclerViewAdapter;

    @Override
    public void onBackPressed() {
        if (searchView.getVisibility()==View.VISIBLE){
            activityCont.searchViewClosed(searchView,floatingActionButton);
            if (todoItems!=null && todoRV!=null){
                setRecyclerViewAdapter(todoItems);
            }
        }
        else{
            super.onBackPressed();
        }
    }

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

        recyclerViewAdapter=new RecyclerViewAdapter(this,todoItems,this,colorHex,R.layout.todo_layout,myRealm);
        activityCont=new ActivityController(TodoActivity.this,todoRV,myRealm);
        setSupportActionBar(toolbar);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        floatingActionButton.setSupportBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this,colorHex)));

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        todoRV.setLayoutManager(linearLayoutManager);


        todoRV.setAdapter(recyclerViewAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                if (todoItems.get(viewHolder.getAdapterPosition()).isChecked()) {
                    final String todoItem = todoItems.get(viewHolder.getAdapterPosition()).getTodo();
                    final int position = viewHolder.getAdapterPosition();
                    removeItemFromRealm(position);

                    Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), "Item deleted. Click UNDO to revert change.", Snackbar.LENGTH_LONG);
                    snackbar.setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addItemToRealm(todoItem, position);
                            recyclerViewAdapter.notifyItemInserted(position);
                        }
                    });
                    snackbar.show();
                }else {
                    recyclerViewAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                    Snackbar.make(findViewById(R.id.coordinatorLayout),"Cannot be deleted. Please check the box.",Snackbar.LENGTH_SHORT).show();
                }
            }
        }).attachToRecyclerView(todoRV);

        window.setStatusBarColor(ContextCompat.getColor(this,colorHex));

        setActionBar();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityCont.floatButtonClick(searchView,floatingActionButton);
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                activityCont.searchViewClosed(searchView,floatingActionButton);
                setRecyclerViewAdapter(todoItems);
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                RealmResults<TodoItems> tempResults=todoItems.where().contains("todo",s).findAll();
                setRecyclerViewAdapter(tempResults);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
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
                openAddDialog(0,0,TodoActivity.this,todoItems,colorHex,myRealm,recyclerViewAdapter);
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

    private void setActionBar(){
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this,colorHex)));
        getSupportActionBar().setTitle(titleName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setElevation(3);
    }

    void openAddDialog(final int addOrUpdate, final int position, final Context context, final OrderedRealmCollection<TodoItems> todoItems, int colorHex, final Realm myRealm, final RecyclerViewAdapter recyclerViewAdapter){
        final Dialog addDialog = new Dialog(context);
        addDialog.setContentView(R.layout.custom_todo_categories);
        Button addButton = addDialog.findViewById(R.id.addButton);
        final TextView whatTodoText = addDialog.findViewById(R.id.editText);
        ConstraintLayout constraintLayout=addDialog.findViewById(R.id.constraintLayout);
        constraintLayout.setBackground(ContextCompat.getDrawable(context,colorHex));

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
                            addItemToRealm(todoString,todoItems.size());
                            recyclerViewAdapter.notifyDataSetChanged();
                            addDialog.dismiss();
                        }
                        else {
                            Toasty.error(context, "Category name must be unique!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        myRealm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                TodoItems todoItem = todoItems.get(position);
                                todoItem.setTodo(todoString);
                                recyclerViewAdapter.notifyDataSetChanged();
                            }
                        });
                        addDialog.dismiss();
                    }
                }
                else {
                    Toasty.error(context, "Please enter a category!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        addDialog.show();
    }

    void setRecyclerViewAdapter(OrderedRealmCollection<TodoItems> todoItems){
        recyclerViewAdapter=new RecyclerViewAdapter(this,todoItems,this,colorHex,R.layout.todo_layout,myRealm);
        todoRV.setAdapter(recyclerViewAdapter);
    }

    private void addItemToRealm(final String todoItemString, final int position){
        myRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                try {
                    TodoItems todoItem = realm.createObject(TodoItems.class);
                    todoItem.setTodo(todoItemString);
                    todoItem.setChecked(false);
                    todoItems.add(position, todoItem);
                }catch (Exception e){
                    Toasty.error(TodoActivity.this,"Error! "+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void removeItemFromRealm(final int position){
        myRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                TodoItems todoItem=todoItems.get(position);
                todoItems.remove(position);
                todoItem.deleteFromRealm();
                recyclerViewAdapter.notifyItemRemoved(position);
            }
        });
    }
}
