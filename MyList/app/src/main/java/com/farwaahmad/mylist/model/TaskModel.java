package com.farwaahmad.mylist.model;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class TaskModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String task;
    private String due;
    private int status;

    public TaskModel() {
        // Required by Firestore.
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getDue() {
        return due;
    }

    public void setDue(String due) {
        this.due = due;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
