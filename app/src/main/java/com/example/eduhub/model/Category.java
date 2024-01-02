package com.example.eduhub.model;

public class Category {
    //make sure to user same spellings for model variables in firebase
    String category_id;
    String category_name;

    //constructor empty required for firebase
    public Category() {
    }

    public Category(String category_id, String category_name) {
        this.category_id = category_id;
        this.category_name = category_name;
    }

    //Getters and setters

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }
}