package com.mrntlu.whattodo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.mrntlu.whattodo.Models.TodoItems;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    Context context;
    OrderedRealmCollection<TodoItems> todoItems;
    Activity activity;
    int colorHex;
    int resource;
    Realm myRealm;
    TodoActivity todoActivity;

    public RecyclerViewAdapter(Context context, OrderedRealmCollection<TodoItems> todoItems, Activity activity, int colorHex, int resource, Realm myRealm) {
        this.context = context;
        this.todoItems = todoItems;
        this.activity = activity;
        this.colorHex = colorHex;
        this.resource = resource;
        this.myRealm = myRealm;
        todoActivity=new TodoActivity();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v=LayoutInflater.from(context).inflate(R.layout.todo_layout,parent,false);
        MyViewHolder viewHolder=new MyViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.textView.setText(todoItems.get(position).getTodo());
        holder.constraintLayout.setBackgroundColor(ContextCompat.getColor(context, colorHex));
        if (todoItems.get(position).isChecked()){
            holder.checkBox.setChecked(true);
            overlineController(true,holder.textView);
        }else{
            holder.checkBox.setChecked(false);
        }
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                overlineController(b,holder.textView);
                setTodoChecked(b,position);
            }
        });

        holder.constraintLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                todoActivity.openAddDialog(1,position,context,todoItems,colorHex,myRealm,RecyclerViewAdapter.this);
                return true;
            }
        });
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
    public int getItemCount() {
        return todoItems.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        ConstraintLayout constraintLayout;
        CheckBox checkBox;
        TextView textView;

        public MyViewHolder(View itemView) {
            super(itemView);
            constraintLayout=itemView.findViewById(R.id.constraintLayout);
            checkBox=itemView.findViewById(R.id.checkBox);
            textView=itemView.findViewById(R.id.myText);
        }
    }
}
