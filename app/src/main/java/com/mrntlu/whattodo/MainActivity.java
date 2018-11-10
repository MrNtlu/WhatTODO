package com.mrntlu.whattodo;

import android.app.Dialog;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mrntlu.whattodo.Models.Categories;
import com.mrntlu.whattodo.Models.TodoItems;
import androidx.constraintlayout.widget.ConstraintLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.prefs.Prefs;
import es.dmoral.toasty.Toasty;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;

    @BindView(R.id.categories_recycler) SwipeMenuListView categoriesRV;

    @BindView(R.id.constraintLayout)ConstraintLayout layout;

    @BindView(R.id.searchView)SearchView searchView;

    @BindView(R.id.searchButton)FloatingActionButton floatingActionButton;

    RealmResults<Categories> realmResults;

    Realm myRealm;
    CustomAdapter customAdapter;
    Menu menu;
    ActivityController activityCont;
    private final String sortKey="SORT_KEY";

    @Override
    public void onBackPressed() {
        if (searchView.getVisibility()==View.VISIBLE){
            activityCont.searchViewClosed(searchView,floatingActionButton);
            if (realmResults!=null && categoriesRV!=null){
                setListviewAdapter(realmResults);
            }
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Realm.init(this);
        myRealm=Realm.getDefaultInstance();

        ActivityController.initPref(MainActivity.this,sortKey);

        if (Prefs.with(this).readInt(sortKey)==1) realmResults=myRealm.where(Categories.class).sort("category",Sort.DESCENDING).findAll();
        else realmResults=myRealm.where(Categories.class).sort("category").findAll();

        customAdapter=new CustomAdapter(this,realmResults,this,R.layout.categories_layout);
        activityCont=new ActivityController(MainActivity.this,categoriesRV,customAdapter,myRealm);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Categories");
        getSupportActionBar().setElevation(3);

        categoriesRV.setAdapter(customAdapter);

        activityCont.setMenu();

        categoriesRV.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                final Categories categories = realmResults.get(position);
                switch (index){
                    case 0:
                        myRealm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                try{
                                    openAddDialog(1,position);
                                }catch (Exception e){
                                    Toasty.error(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
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
                                    RealmList<TodoItems> todoItems = categories.getTodoItems();
                                    todoItems.deleteAllFromRealm();
                                    categories.deleteFromRealm();
                                    customAdapter.notifyDataSetChanged();
                                }catch (Exception e){
                                    Toasty.error(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        });
                        break;
                }

                return true;
            }
        });

        categoriesRV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent=new Intent(MainActivity.this,TodoActivity.class);
                intent.putExtra("TITLE_NAME", customAdapter.categoriesRealm.get(i).getCategory());
                intent.putExtra("TOOLBAR_COLOR", customAdapter.categoriesRealm.get(i).getColorID());
                startActivity(intent);
            }
        });

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
                setListviewAdapter(realmResults);
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                RealmResults<Categories> tempResults=myRealm.where(Categories.class).contains("category",s, Case.INSENSITIVE).findAll();
                setListviewAdapter(tempResults);
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
        if (Prefs.with(this).readInt(sortKey)==0){
            activityCont.setSortMenuItemIcon(R.drawable.reverse_alp,menu);

        }else if (Prefs.with(this).readInt(sortKey)==1){
            activityCont.setSortMenuItemIcon(R.drawable.sort_alp,menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addButton:
                openAddDialog(0,0);
                break;
            case R.id.sort_alphabet:
                if (Prefs.with(this).readInt(sortKey)==0){
                    onOptionsController(1,R.drawable.sort_alp,Sort.DESCENDING);
                }else if(Prefs.with(this).readInt(sortKey) == 1){
                    onOptionsController(0,R.drawable.reverse_alp,Sort.ASCENDING);
                }
                customAdapter.notifyDataSetChanged();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(myRealm != null) {
            myRealm.close();
        }
        super.onDestroy();
    }

    void onOptionsController(int option,int drawableID,Sort sortingType){
        activityCont.setSortMenuItemIcon(drawableID,menu);
        realmResults=realmResults.sort("category",sortingType);
        customAdapter=new CustomAdapter(MainActivity.this,realmResults,MainActivity.this,R.layout.categories_layout);
        categoriesRV.setAdapter(customAdapter);
        Prefs.with(this).writeInt(sortKey,option);
    }

    void updateAndAdd(Categories categories, String category, int colorID){
        categories.setCategory(category);
        categories.setColorID(colorID);
        customAdapter.notifyDataSetChanged();
    }

    void setListviewAdapter(RealmResults<Categories> results){
        customAdapter=new CustomAdapter(MainActivity.this,results,MainActivity.this,R.layout.categories_layout);
        categoriesRV.setAdapter(customAdapter);
    }

    void openAddDialog(final int addOrUpdate, final int position) {
        final Dialog addDialog = new Dialog(this);
        addDialog.setContentView(R.layout.custom_categories_dialog);
        Button addButton = addDialog.findViewById(R.id.addButton);
        final TextView categoriesText = addDialog.findViewById(R.id.editText);
        final RadioGroup colorGroup = addDialog.findViewById(R.id.radioGroup);

        if (addOrUpdate == 1) {
            categoriesText.setText(realmResults.get(position).getCategory());
            colorGroup.check(realmResults.get(position).getColorID());
        }

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String categoryString = categoriesText.getText().toString();
                if (!categoryString.trim().isEmpty()) {

                    int radioButtonID = colorGroup.getCheckedRadioButtonId();
                    final int colorID;
                    switch (radioButtonID) {
                        case R.id.blackColor:
                            colorID = R.color.black900;
                            break;
                        case R.id.redColor:
                            colorID = R.color.red900;
                            break;
                        case R.id.yellowColor:
                            colorID = R.color.yellow700;
                            break;
                        case R.id.greenColor:
                            colorID = R.color.green900;
                            break;
                        case R.id.blueColor:
                            colorID = R.color.blue900;
                            break;
                        case R.id.orangeColor:
                            colorID = R.color.orange900;
                            break;
                        default:
                            colorID = R.color.colorPrimaryDark;
                            break;
                    }
                    if (addOrUpdate == 0) {
                        if (activityCont.controlUniqueCategoryName("category",categoryString)) {
                            myRealm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    try {
                                        Categories categories = realm.createObject(Categories.class);
                                        updateAndAdd(categories, categoryString, colorID);
                                    } catch (Exception e) {
                                        Toasty.error(MainActivity.this, "Error!"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }
                                }
                            });
                            addDialog.dismiss();
                        } else {
                            Toasty.error(MainActivity.this, "Category name must be unique!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (myRealm.where(Categories.class).equalTo("category", categoryString).findAll().size() < 1 ||
                                categoryString.equals(realmResults.get(position).getCategory())) {

                            myRealm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    Categories categories = realmResults.get(position);
                                    updateAndAdd(categories, categoryString, colorID);
                                }
                            });

                            addDialog.dismiss();
                        } else {
                            Toasty.error(MainActivity.this, "Please don't use the same category name.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toasty.error(MainActivity.this, "Please enter a category!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        addDialog.show();
    }
}