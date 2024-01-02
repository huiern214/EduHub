package com.example.eduhub.model;

import com.google.firebase.Timestamp;

public class Comment {
    //variables
    String comment_id, resource_id, comment_details, user_id;
    Timestamp comment_timestamp;

    //constructor, empty required by firebase
    public Comment(){
    }

    //constructor with all params
    public Comment(String commentId, String noteId, Timestamp comment_timestamp, String comment_details, String user_id) {
        this.comment_id = commentId;
        this.resource_id = noteId;
        this.comment_timestamp = comment_timestamp;
        this.comment_details = comment_details;
        this.user_id = user_id;
    }

    //Getters & setters

    public String getCommentId() {
        return comment_id;
    }

    public void setCommentId(String commentId) {
        this.comment_id = commentId;
    }

    public String getResource_id() {
        return resource_id;
    }

    public void setResource_id(String resource_id) {
        this.resource_id = resource_id;
    }

    public Timestamp getComment_timestamp() {
        return comment_timestamp;
    }

    public void setComment_timestamp(Timestamp comment_timestamp) {
        this.comment_timestamp = comment_timestamp;
    }

    public String getComment_details() {
        return comment_details;
    }

    public void setComment_details(String comment_details) {
        this.comment_details = comment_details;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
