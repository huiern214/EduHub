package com.example.eduhub.model;

import com.google.firebase.Timestamp;

public class Notes {
    String notes_id;
    String category_id;
    String resource_description;
    String resource_file;
    int resource_likes;
    int resource_views;
    int resource_downloads;
    String resource_name;
    Timestamp resource_upload_datetime;
    String user_id;

    //constructor
    public Notes() {
    }

    public Notes(String notes_id, String category_id, String resource_description, String resource_file, int resource_likes, String resource_name, Timestamp resource_upload_datetime, String user_id) {
        this.notes_id = notes_id;
        this.category_id = category_id;
        this.resource_description = resource_description;
        this.resource_file = resource_file;
        this.resource_likes = resource_likes;
        this.resource_name = resource_name;
        this.resource_upload_datetime = resource_upload_datetime;
        this.user_id = user_id;
    }

    public Notes(String notes_id, String category_id, String resource_description, String resource_file, int resource_likes, int resource_views, int resource_downloads, String resource_name, Timestamp resource_upload_datetime, String user_id) {
        this.notes_id = notes_id;
        this.category_id = category_id;
        this.resource_description = resource_description;
        this.resource_file = resource_file;
        this.resource_likes = resource_likes;
        this.resource_views = resource_views;
        this.resource_downloads = resource_downloads;
        this.resource_name = resource_name;
        this.resource_upload_datetime = resource_upload_datetime;
        this.user_id = user_id;
    }

    public String getNotes_id() {
        return notes_id;
    }

    public void setNotes_id(String notes_id) {
        this.notes_id = notes_id;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public String getResource_description() {
        return resource_description;
    }

    public void setResource_description(String resource_description) {
        this.resource_description = resource_description;
    }

    public String getResource_file() {
        return resource_file;
    }

    public void setResource_file(String resource_file) {
        this.resource_file = resource_file;
    }

    public int getResource_likes() {
        return resource_likes;
    }

    public void setResource_likes(int resource_likes) {
        this.resource_likes = resource_likes;
    }

    public String getResource_name() {
        return resource_name;
    }

    public void setResource_name(String resource_name) {
        this.resource_name = resource_name;
    }

    public Timestamp getResource_upload_datetime() {
        return resource_upload_datetime;
    }

    public void setResource_upload_datetime(Timestamp resource_upload_datetime) {
        this.resource_upload_datetime = resource_upload_datetime;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public int getResource_views() {
        return resource_views;
    }

    public void setResource_views(int resource_views) {
        this.resource_views = resource_views;
    }

    public int getResource_downloads() {
        return resource_downloads;
    }

    public void setResource_downloads(int resource_downloads) {
        this.resource_downloads = resource_downloads;
    }
}