package com.example.eduhub.model;

public class Task {
    String task_id, task_date, task_description, task_event, task_time, task_title, task_userId, task_status;

    public Task(){

    }

    public Task(String task_id, String task_date, String task_description, String task_event, String task_time, String task_title, String task_userId, String task_status) {
        this.task_id = task_id;
        this.task_date = task_date;
        this.task_description = task_description;
        this.task_event = task_event;
        this.task_time = task_time;
        this.task_title = task_title;
        this.task_userId = task_userId;
        this.task_status = task_status;
    }

    public String getTask_id(){
        return task_id;
    }

    public void setTask_id(String taskId){
        this.task_id = taskId;
    }

    public String getTask_date() {
        return task_date;
    }

    public void setTask_date(String task_date) {
        this.task_date = task_date;
    }

    public String getTask_description() {
        return task_description;
    }

    public void setTask_description(String task_description) {
        this.task_description = task_description;
    }

    public String getTask_event() {
        return task_event;
    }

    public void setTask_event(String task_event) {
        this.task_event = task_event;
    }

    public String getTask_time() {
        return task_time;
    }

    public void setTask_time(String task_time) {
        this.task_time = task_time;
    }

    public String getTask_title() {
        return task_title;
    }

    public void setTask_title(String task_title) {
        this.task_title = task_title;
    }

    public String getTask_userId() {
        return task_userId;
    }

    public void setTask_userId(String task_userId) {
        this.task_userId = task_userId;
    }

    public String getTask_status(){
        return task_status;
    }

    public void setTask_status(String task_status){
        this.task_status = task_status;
    }
}
