package com.mrntlu.whattodo.Models;

import io.realm.RealmObject;


public class TodoItems extends RealmObject {

    private String todo;

    private boolean checked;

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getTodo() {
        return todo;
    }

    public void setTodo(String todo) {
        this.todo = todo;
    }

}

