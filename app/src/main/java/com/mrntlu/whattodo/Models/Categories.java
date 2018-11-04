package com.mrntlu.whattodo.Models;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Required;

public class Categories extends RealmObject {

    private @Required String category;

    private int colorID;

    private RealmList<TodoItems> todoItems;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getColorID() {
        return colorID;
    }

    public void setColorID(int colorID) {
        this.colorID = colorID;
    }

    public RealmList<TodoItems> getTodoItems() {
        return todoItems;
    }

    public void setTodoItems(RealmList<TodoItems> todoItems) {
        this.todoItems = todoItems;
    }
}
