package com.nagyd.tidder.model;

public class Comment {
    public String text;
    public String author;
    public String id;

    public Comment() {}

    public Comment(String text, String author, String id) {
        this.text = text;
        this.author = author;
        this.id = id;
    }
}
