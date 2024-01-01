package com.example.eduhub;

public class user_modelComment {
    //variables
    String commentId, noteId, timestamp, comment, uid;

    //constructor, empty required by firebase
    public user_modelComment(){
    }

    //constructor with all params
    public user_modelComment(String commentId, String noteId, String timestamp, String comment, String uid) {
        this.commentId = commentId;
        this.noteId = noteId;
        this.timestamp = timestamp;
        this.comment = comment;
        this.uid = uid;
    }

    //Getters & setters

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
