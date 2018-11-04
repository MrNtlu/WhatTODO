package com.mrntlu.whattodo.Models;

import io.realm.RealmObject;


public class TodoItems extends RealmObject {

    private String todo;

    public String getTodo() {
        return todo;
    }

    public void setTodo(String todo) {
        this.todo = todo;
    }

}

